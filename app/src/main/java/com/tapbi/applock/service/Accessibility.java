package com.tapbi.applock.service;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.common.model.EventLock;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.data.local.db.AppDatabase;
import com.tapbi.applock.common.model.AppInfo;
import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.databinding.DialogLockNewappBinding;
import com.tapbi.applock.ui.main.MainActivity;
import com.tapbi.applock.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class Accessibility extends AccessibilityService {
    private String packageName = "";
    //    private AppDatabase db;

    private List<ApplicationInfo> packages = new ArrayList<>();
    private List<AppLock> list = new ArrayList<>();
    private ArrayList<String> name = new ArrayList<>();
    private Intent serviceIntent;
    private InitWindowsManager service = new InitWindowsManager();
    private String getPackageName1 = "";
    private String getPackageName2 = "";
    private String getPackageName3 = "";
    final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private boolean lock = true;
    private SharedPreferences typeLock;
    private String namePakeSelect = "";
    private HomeAppReceiver homeAppReceiver = new HomeAppReceiver();
    private BackAppReceiver backAppReceiver = new BackAppReceiver();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.e("NGOCANH", "onStartCommand Accessibility");

        //Log.e("onStartCommand", "onStartCommand" + ".");
        try {
            if (intent.hasExtra(Constant.LOCK)) {
                lock = intent.getBooleanExtra(Constant.LOCK, true);
                Log.e("NGOCANH", "onStartCommand: lock = " + lock);
            }
        } catch (Exception e) {
        }

        initSQL();
        setListLock();
        if (Utils.isMIUI(getBaseContext()))
            setUpNotification();

        return START_STICKY;
    }

    private void setUpNotification() {
            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? getNotificationChannel(Objects.requireNonNull(notificationManager)) : "";
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);

            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_notifi)
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentTitle(getString(R.string.app_name))
                    .setTicker(getString(R.string.app_name))
                    .build();
        startForeground(123, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String getNotificationChannel(NotificationManager notificationManager) {
        String channelId = "app_active_channel";
        String channelName = "App Active";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

    @Override
    public void onCreate() {
        Log.e("NGOCANH", "onCreate Accessibility");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        super.onCreate();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        //Log.e("onServiceConnected", "onServiceConnected" + ".");
        registerReceiver(backAppReceiver, BackAppFilter());
        //registerReceiver(blueToothReceiver, blueToothFilter());
        registerReceiver(homeAppReceiver, HomeAppFilter());
        // registerReceiver(wifiReceiver, WifiFilter());
        registerReceiver(installAppReceiver, InstallAppFilter());

        registerReceiver(unInstallAppReceiver, UnInstallAppFilter());


        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.WINDOWS_CHANGE_ACTIVE;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16)
            //Just in case this helps
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        config.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        setServiceInfo(config);

    }

    InstallAppReceiver installAppReceiver = new InstallAppReceiver();
    UnInstallAppReceiver unInstallAppReceiver = new UnInstallAppReceiver();

    private void initSQL() {
        final PackageManager pm = getPackageManager();
        packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        AppDatabase.getInstance();

//        db = Room.databaseBuilder(getBaseContext(), AppDatabase.class, Constant.DATA_NAME).allowMainThreadQueries().build();
        Log.e("NGOCANH", "init Accessibility to InitWindowsManager");
        serviceIntent = new Intent(Accessibility.this, InitWindowsManager.class);
    }

    private int i = 1;
    private boolean enableBackPress;
    private boolean checkQuestion;

    @Subscribe(sticky = true)
    public void onEventLock(EventLock event) {
        if(event.getType().equals(Constant.CHECK_QUESTION)){
            Log.e("NGOCANH", "onEventLock CHECK_QUESTION");
            checkQuestion = true;
        }
        else{
            checkQuestion = false;
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e("NGOCANH", "onAccessibilityEvent " + event);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.getContentChangeTypes() == 0) {

            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );

                if (event.getPackageName().toString().contains("biometrics")) {
                    return;
                }

                //check
                packageName = componentName.getPackageName();
                if (packageName.contains("launcher") || packageName.contains("home") || packageName.contains("recents") || packageName.contains("systemui")) {
                    lock = true;
                }

                if (packageName.contains("permissioncontroller") || packageName.contains("packageinstaller")) {
                    lock = false;
                }

                Log.d("NGOCANH", "onAccessibilityEvent: " + packageName + "-- " + name + "-- " + namePakeSelect);

                packageName = componentName.getPackageName();

                if (lock) {
                    if (!isMyServiceRunning(service.getClass())) {
                        Log.e("NGOCANH", "!isMyServiceRunning");
                        checkShowLock(componentName.getPackageName());

                    } else {
                        if ((packageName.contains("launcher") || packageName.contains("home")) && !name.contains(packageName) && !name.contains(namePakeSelect) && !packageName.equals(namePakeSelect)) {
                            Log.e("NGOCANH", "exit 1");
                          EventBus.getDefault().post(new EventLock(Constant.EXIT));
                        }
                    }
                    namePakeSelect = packageName;
                }
            }
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {
    }

    private void checkShowLock(String packageName) {
        Log.e("NGOCANH", "checkShowLock " + packageName);
        //Log.e("namelock123", name + ".");
        if (name.contains(packageName)) {
            final PackageManager pm = getPackageManager();
            String icon = null;
            String appName = null;
            for (ApplicationInfo packageInfo : packages) {
                try {
                    if (packageInfo.packageName.equalsIgnoreCase(packageName)) {
                        appName = packageInfo.loadLabel(pm) + "";
                        Bitmap bm = drawableToBitmap(packageInfo.loadIcon(pm));
                        icon = saveImgToCache(bm);
                        break;
                    }
                } catch (Exception e) {
                }
            }

            if (icon != null) {
                serviceIntent.putExtra(Constant.ICON, icon);
            }
            if (appName != null) {
                serviceIntent.putExtra(Constant.APP_NAME, appName);
            }

            serviceIntent.putExtra(Constant.PACKAGE_NAME, packageName);

            startService(serviceIntent);

        } else {
            //Log.e("contains", "no");
        }
    }

    public String saveImgToCache(Bitmap bitmap) {
        File cachePath = null;
        String fileName = "img";
        String path = null;
        Date currentDate = new Date(System.currentTimeMillis());
        String name = String.valueOf(currentDate);
        if (!TextUtils.isEmpty(name)) {
            fileName = name;
        }
        try {
            cachePath = new File(getBaseContext().getCacheDir(), "images");
            cachePath.mkdirs();

            FileOutputStream stream = new FileOutputStream(cachePath + "/" + fileName + "jpg");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            path = cachePath + "/" + fileName + "jpg";
        } catch (IOException e) {
            //Log.e("ds", "saveImgToCache error: " + bitmap, e);
        }
        //Log.e("ds", "cachePath: " + path);
        return path;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private class InstallAppReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) || intent.getAction().equals(Intent.ACTION_PACKAGE_INSTALL)) {
                //   Toast.makeText(context, "App Installed!!!!.", Toast.LENGTH_LONG).show();
//                SharedPreferences typeLock = getSharedPreferences(Constant.SharedPreferences_NAME, MODE_PRIVATE);
                boolean lockNewApp = SharedPreferenceHelper.getBoolean(Constant.LOCK_NEW_APP, false);
                //Log.e("lockNewApp", lockNewApp + ".");
                if (lockNewApp) {
                    String packageName = intent.getData().getEncodedSchemeSpecificPart();

                    if (!packageName.contains(Constant.PACKAGE_NAME) && !packageName.toLowerCase().contains(Constant.PACKAGE_NAME_APP_INPUTMETHOD.toLowerCase()) && !packageName.toLowerCase().contains(Constant.PACKAGE_NAME_APP_KEYBOARD.toLowerCase()) && !packageName.toLowerCase().contains(Constant.PACKAGE_NAME_APP_SEARCH_BOX.toLowerCase())) {
                        if (!Utils.listAppName.contains(packageName)) {
                            inforNewApp(packageName);
                        }
                    }
                    //Timber.e("list App Name s: "+packageName);
                }
            }
        }
    }

    @NonNull
    private IntentFilter InstallAppFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_INSTALL);
        filter.addDataScheme("package");
        return filter;
    }


    private class UnInstallAppReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_CHANGED) || intent.getAction().equals(Intent.ACTION_UNINSTALL_PACKAGE) || intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                String packageName = intent.getData().getEncodedSchemeSpecificPart();
                Log.e("NGOCANH", "uninstall " + packageName);

                //check app in db
                Utils.listAppName.remove(packageName);

                if (AppDatabase.getInstance().getAppDAO().isExistPackage(packageName)) {
                    AppDatabase.getInstance().getAppDAO().deleteByPackage(packageName);
                }
            }
        }
    }

    @NonNull
    private IntentFilter UnInstallAppFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_UNINSTALL_PACKAGE);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        return filter;
    }

    private class HomeAppReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                //Log.e("HomeAppReceiver", "HomeAppReceiver");
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    lock = true;
//                    lock = false;
                }
            }
            Log.e("NGOCANH", "Accessibility HomeAppReceiver : lock = " + lock);
        }
    }

    @NonNull
    private IntentFilter WifiFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        return filter;
    }

    @NonNull
    private IntentFilter blueToothFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    private class blueToothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                //Log.e("lockBluetooth", mBluetoothAdapter.isEnabled()+".");
//                typeLock = getSharedPreferences(Constant.SharedPreferences_NAME, MODE_PRIVATE);
//                if (typeLock.getBoolean(Constant.LOCK_BLUETOOTH, false)) {
//                }
            }
        }
    }

    @NonNull
    private IntentFilter HomeAppFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        return filter;
    }

    private void inforNewApp(String packageName) {
        //Log.e("inforNewApp",   "inforNewApp.");
        final PackageManager pm = getPackageManager();
        //get a list of installed ap
        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                if (packageInfo.packageName.equalsIgnoreCase(packageName)) {
                    AppInfo appInfo = new AppInfo();
                    appInfo.setAppIcon(packageInfo.loadIcon(pm));
                    appInfo.setAppName((String) packageInfo.loadLabel(pm));
                    appInfo.setAppPackage(packageInfo.packageName);

                    if (appInfo.getAppPackage() != null) {
                        AlertDialogLockNewApp(appInfo);
                    }

                    break;
                }
            }
        }
    }

    private void AlertDialogLockNewApp(final AppInfo appInfo) {
        lock = false;
        DialogLockNewappBinding binding = DialogLockNewappBinding.inflate(LayoutInflater.from(getBaseContext()));
        final AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
        builder.setView(binding.getRoot());
        final AlertDialog alertDialog = builder.create();


        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        binding.inBtn.btnRight.setText(getBaseContext().getString(R.string.lock));
        binding.tvAppName.setText(appInfo.getAppName());
        binding.imgIcApp.setImageDrawable(appInfo.getAppIcon());
        binding.inBtn.btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAppListLock(appInfo.getAppPackage());
                alertDialog.dismiss();
            }
        });

        binding.inBtn.btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lock = true;

                alertDialog.dismiss();
                AppLock appLock = new AppLock();
                appLock.setAppPackage(appInfo.getAppPackage());
                appLock.setAppPass("");
//                long[] kq = db.getAppDAO().insertAppLock(appLock);
//                if (kq[0] > 0) {
//                    Toast.makeText(getBaseContext(), getText(R.string.lockSuccess) + ":" + appInfo.getAppName(), Toast.LENGTH_SHORT).show();
//                }
                AppDatabase.instance.getAppDAO().insertAppLock(appLock);

                list.add(appLock);
                name.add(appInfo.getAppPackage());
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        alertDialog.show();
    }

    private boolean isMyServiceRunning(Class<?> seClass) {
        ActivityManager manager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (seClass.getName().equals(serviceInfo.service.getClassName())) {
                //Log.e("sevice status", "Running");
                return true;
            }
        }
        return false;
    }

    private void checkBackHome() {
        getPackageName1 = "";
        getPackageName2 = "";
        getPackageName3 = "";
        i = 1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void setListLock() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    list = AppDatabase.instance.getAppDAO().getAppLock();
                    name.clear();
                    for (int i = 0; i < list.size(); i++) {
                        name.add(list.get(i).getAppPackage());
                    }
                    if (name.size() == 0) {
                        String passDraw = (SharedPreferenceHelper.getStringWithDefault(Constant.PATTERN_LOCK, Constant.PASS_DEFAULT_DRAW));
                        boolean setQuestion = SharedPreferenceHelper.getBoolean(Constant.SET_QUESTION_SETTING, false);
                        if(!setQuestion)
                            passDraw = "";

                        if (!passDraw.equals("")) {
                            name.add(Constant.PACKAGE_NAME);
                        }
                    }
                } catch (Exception e) {

                }
            }
        }).start();

    }

    private class BackAppReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.e("sdsd111", "dsds111");
            Log.e("NGOCANH", "Accessibility BackAppReceiver");
            if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    Intent intent2 = new Intent(getApplicationContext(), Accessibility.class);
                    intent2.putExtra(Constant.LOCK, true);
                    Log.e("NGOCANH", "Accessibility BackAppReceiver : lock");

                    startService(intent2);
                }
            }

        }
    }

    @NonNull
    private IntentFilter BackAppFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_BUTTON);
        return filter;
    }

    private void checkAppListLock(String packageName) {
        for (AppLock appLock : list) {

            if (appLock.getAppPackage() != null && appLock.getAppPackage().equalsIgnoreCase(packageName)) {
                AppDatabase.instance.getAppDAO().deleteAppLock(appLock);
                break;
            }
        }
    }
}



