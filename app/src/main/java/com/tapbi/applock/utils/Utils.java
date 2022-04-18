package com.tapbi.applock.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.common.model.BackGround;
import com.tapbi.applock.common.model.Theme;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.data.model.ImageThief;
import com.tapbi.applock.interfaces.ResultDialog;
import com.tapbi.applock.service.Accessibility;
import com.tapbi.applock.ui.dialog.DialogAlert;
import com.tapbi.applock.ui.dialog.DialogPermissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import timber.log.Timber;

public class Utils {
    public static boolean BACKGROUND_DEFAULT = false;
    public static List<String> listAppName = new ArrayList<>();

    public static void shareBrowser(Activity activity, String s) {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, s);
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            activity.startActivity(shareIntent);
        } catch (Exception e) {
        }
    }

    public static void shareImage(Activity activity, ArrayList<Uri> uriList){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
        shareIntent.setType("image/*");
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        activity.startActivity(Intent.createChooser(shareIntent, "Choose an app"));
    }

    public static void sendFeedback(Context context, String supportEmail, String text) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/email");
        emailIntent.setPackage("com.google.android.gm");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{supportEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "App Report (" + context.getPackageName() + ")");
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(
                Intent.createChooser(
                        emailIntent,
                        context.getString(R.string.send_email_report_app)
                )
        );
    }

    public static Bitmap viewToBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }


    public static void saveImage(Context context, List<ImageThief> list){
        for (ImageThief imageThief : list) {
            if (imageThief.isSelected()) {
                File fileCache = new File(imageThief.getCachePath());
                String root = Environment.getExternalStorageDirectory().toString() + File.separator + Constant.APP_LOCK;
                File folder = null;
                try {
                    folder = new File(root);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                String pathFile = root + File.separator + imageThief.getTime() + ".jpg";
                File fileNew = new File(pathFile);

                if (!fileNew.exists()) {
                    try {
                        copy(context, fileCache, fileNew);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void copy(Context context, File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                scanMedia(context, dst.getAbsolutePath());
            }
        }
    }

    public static ArrayList<Theme> listTheme(Context context, String folder) {
        ArrayList<String> listPath = Utils.getListFromAssets(context, folder);
        ArrayList<Theme> list = new ArrayList<>();
        for (int i = 0; i < listPath.size(); i++) {
            list.add(new Theme(false, listPath.get(i)));
            Timber.e(listPath.get(i));
        }
        return list;
    }

    public static ArrayList<BackGround> listBackground(Context context, String folder) {
        ArrayList<BackGround> list = new ArrayList<>();
        BackGround ic_plus = new BackGround(false, R.drawable.ic_add_back_ground + "");
        List<BackGround> list1 = Utils.getListFileCache(context);

        list.add(ic_plus);
        list.add(new BackGround(false, ""));
        list.addAll(list1);
        ArrayList<String> listPath = new Utils().getListBGFromAssets(context, folder);
        for (int i = 0; i < listPath.size(); i++) {
            list.add(new BackGround(false, listPath.get(i)));
            Timber.e(listPath.get(i));
        }

        return list;
    }


    public static void scanMedia(Context context, String path) {
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        Intent scanFileIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        context.sendBroadcast(scanFileIntent);
    }

    @SuppressLint("ResourceType")
    public void selectTabLayout(Context context, TextView textViewLeft, TextView textViewRight, View viewLeft, View viewRight, TextView textViewSelect) {
        textViewLeft.setTextColor(context.getResources().getColor(R.color.colorWhite));
        textViewRight.setTextColor(context.getResources().getColor(R.color.colorWhite));

        textViewLeft.setShadowLayer(0, 0, 0, 0);
        textViewRight.setShadowLayer(0, 0, 0, 0);

        viewLeft.setVisibility(View.INVISIBLE);
        viewRight.setVisibility(View.INVISIBLE);

        if (textViewSelect == textViewLeft) {
            viewLeft.setVisibility(View.VISIBLE);
        } else {
            viewRight.setVisibility(View.VISIBLE);
        }
        textViewSelect.setTextColor(context.getResources().getColor(R.color.color_1DB854));
        textViewSelect.setShadowLayer(20, 0, 0, context.getResources().getColor(R.color.color_C81DB854));

    }


    public void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }


    public static ArrayList<String> getListFromAssets(Context context, String folder) {
        ArrayList<String> path = new ArrayList<>();
//        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.SharedPreferences_NAME, MODE_PRIVATE);
        String type = (SharedPreferenceHelper.getStringWithDefault(Constant.TYPE_LOCK, Constant.PASS_CODE));

        // Timber.e("type: "+ type);
        try {
            String[] list = context.getAssets().list(folder);
            if (list != null)
                for (String s : list) {

                    if (type.contains(Constant.PASS_CODE)) {
                        if (s.contains(Constant.THEME_PASS_CODE)) {
                            path.add("file:///android_asset/" + folder + File.separator + s);
                        }

                    } else {
                        if (!s.contains(Constant.THEME_PASS_CODE)) {
                            path.add("file:///android_asset/" + folder + File.separator + s);
                        }
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //       Timber.e("path: "+ path);
        return path;
    }

    public ArrayList<String> getListBGFromAssets(Context context, String folder) {
        ArrayList<String> path = new ArrayList<>();
//        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.SharedPreferences_NAME, MODE_PRIVATE);
        String type = (SharedPreferenceHelper.getStringWithDefault(Constant.TYPE_LOCK, Constant.PASS_CODE));

        // Timber.e("type: "+ type);
        try {
            String[] list = context.getAssets().list(folder);
            if (list != null)
                for (String s : list) {
                    path.add("file:///android_asset/" + folder + File.separator + s);
                }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //       Timber.e("path: "+ path);
        return path;
    }

    public static String setTheme(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.SharedPreferences_NAME, MODE_PRIVATE);
        String theme = (SharedPreferenceHelper.getStringWithDefault(Constant.THEME_NAME, Constant.THEME_DEFAULT));

        if (theme.equals(Constant.THEME_DEFAULT) || theme.isEmpty()) {
            context.setTheme(R.style.Theme_Default);
        } else if (theme.equals(Constant.THEME_01)) {
            context.setTheme(R.style.Theme_01);
        } else if (theme.contains(Constant.THEME_02)) {
            context.setTheme(R.style.Theme_02);
        } else if (theme.contains(Constant.THEME_03)) {
            context.setTheme(R.style.Theme_03);
        } else if (theme.contains(Constant.THEME_04)) {
            context.setTheme(R.style.Theme_04);
        }
        return theme;
    }

    public boolean setBackGround(Context context, ViewGroup view) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.SharedPreferences_NAME, MODE_PRIVATE);
        String linkBackGround = (SharedPreferenceHelper.getStringWithDefault(Constant.LINK_BACKGROUND, ""));
        BACKGROUND_DEFAULT = false;
        if (!linkBackGround.isEmpty()) {
            //Timber.e("setBackGround: " + "1");
            if (linkBackGround.contains(Constant.BACKGROUND_DEFAULT)) {
                //Timber.e("setBackGround: " + "2");
                view.setBackground(new ColorDrawable(context.getResources().getColor(R.color.color_2C3030)));
                BACKGROUND_DEFAULT = true;
            } else {
                //Timber.e("setBackGround: " + "3");
                int height = SharedPreferenceHelper.getIntWithDefault(Constant.HEIGHT_SCREEN, 0);
                int width = SharedPreferenceHelper.getIntWithDefault(Constant.WIDTH_SCREEN, 0);

                Glide.with(context)
                        .asBitmap()
                        .load(linkBackGround)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .override(width, height)
                        .centerCrop()
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                //Timber.e("setBackGround: resource " + resource);
                                BitmapDrawable drawable = new BitmapDrawable(context.getResources(), resource);
                                //Timber.e("setBackGround: drawable " + drawable);
                                view.setBackground(drawable);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                //Timber.e("setBackGround: resource 2 " + placeholder);
                            }
                        });
                return true;
            }

        }
        return false;
    }


    public static  boolean isEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    public static void closeKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static Intent mSeviceIntent;
    //private BackgroundServices mYourService;
    public static Accessibility mYourService;

    public static boolean showDiaLogPermission = true;

    public static boolean isMIUI(Context ctx) {
        return isIntentResolved(ctx, new Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT))
                || isIntentResolved(ctx, new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")))
                || isIntentResolved(ctx, new Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").addCategory(Intent.CATEGORY_DEFAULT))
                || isIntentResolved(ctx, new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings")));
    }
    private static boolean isIntentResolved(Context ctx, Intent intent ){
        return (intent!=null && ctx.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null);
    }

    public static boolean showDiaLogPermission(Context context) {
        boolean allow = false;
        //Timber.e("showDiaLogPermission: abc  "+ showDiaLogPermission);
        if (showDiaLogPermission) {
            showDiaLogPermission = false;
            AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context.getPackageName());
            Log.d("NGOCANH", "android:system_alert_window: mode=" + mode);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                DialogPermissions dialogPermissions = new DialogPermissions(context, context.getString(R.string.cancel), context.getString(R.string.allow), context.getString(R.string.permissionOverlay), R.drawable.ic_app_finger, new ResultDialog() {
                    @Override
                    public void result(boolean b) {
                        Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION",Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(intent);
                    }
                });
                dialogPermissions.showDialog();
                Log.e("NGOCANH", "show dialog1");
            } else if (!isAccessibilitySettingsOn(context)) {
                //Timber.e("isAccessibilitySettingsOn");
                DialogPermissions dialogPermissions = new DialogPermissions(context, context.getString(R.string.cancel), context.getString(R.string.allow), context.getString(R.string.permissionACCESSIBILITY), R.drawable.ic_app_finger, new ResultDialog() {
                    @Override
                    public void result(boolean b) {
                            Intent intent = new Intent(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                            Bundle bundle = new Bundle();
                            String str = context.getPackageName() + "/" + Accessibility.class.getName();
                            bundle.putString(":settings:fragment_args_key", str);
                            intent.putExtra(":settings:fragment_args_key", str);
                            intent.putExtra(":settings:show_fragment_args", bundle);
                            context.startActivity(intent);
                    }
                });
                dialogPermissions.showDialog();
            } else {
                isAccessibilitySettingsOn(context);
                showDiaLogPermission = true;
                mYourService = new Accessibility();
                mSeviceIntent = new Intent(context, mYourService.getClass());
                //Service not running already
                //start service

                if (!isMyServiceRunning(context, mYourService.getClass())) {
                    context.startService(mSeviceIntent);
                }
                allow = true;
                //Timber.e("showDiaLogPermission allow 3: "+ allow);
                return true;
            }
        }
        return allow;
    }

    // To check if service is enabled
    public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + Accessibility.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            //Log.v("TAG", "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
//            Log.e("TAG", "Error finding setting, default accessibility to not found: "
//                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            //Log.v("TAG", "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    //Log.v("TAG", "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        // Log.v("TAG", "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isAccessGranted(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isMyServiceRunning(Context context, Class<?> seClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (seClass.getName().equals(serviceInfo.service.getClassName())) {
                //Log.e("sevice status", "Running");
                return true;
            }
        }
        //Log.e("sevice status", "Not Running");
        return false;
    }

    @SuppressLint("PrivateApi")
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 38;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    public static String saveImgToCache(Context context, Bitmap bitmap) {
        File cachePath = null;
        String fileName = "img";
        String path = null;
        String name = String.valueOf(System.currentTimeMillis());
        if (!TextUtils.isEmpty(name)) {
            fileName = name;
        }
        try {
            cachePath = new File(context.getCacheDir(), Constant.CHILD_PATH_CACHE_BACKGROUND);
            cachePath.mkdirs();
            File file = new File(cachePath + "/" + fileName + ".jpg");
            if (!file.exists()) file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            path = file.getAbsolutePath();
        } catch (IOException e) {
            //Log.e("ds", "saveImgToCache error: " + bitmap, e);
        }
        //Log.e("ds", "cachePath: " + path);
        return path;
    }



    public static List<BackGround> getListFileCache(Context context) {
        List<BackGround> listFile = new ArrayList<>();
        File dir = new File(context.getCacheDir(), Constant.CHILD_PATH_CACHE_BACKGROUND);
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                //perform here your operation
                listFile.add(new BackGround(false, f.getAbsolutePath()));
            }
        }

        Collections.reverse(listFile);

        return listFile;
    }



    public static void showDialogSuccess(boolean isForgot, Activity activity) {
        DialogAlert dialogAlert = new DialogAlert(activity);

        if(isForgot){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialogAlert.dismissDialog();
                    activity.finish();
                }
            }, 1000);
        }
        else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialogAlert.dismissDialog();
                }
            }, 1000);
        }

    }


    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static String removeLastChar(String s) {
        //returns the string after removing the last character
        return s.substring(0, s.length() - 1);
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
//    public static void showDialogPermission(Context context){
//        new AlertDialog.Builder(context).setMessage("need set up")
//                .setPositiveButton("yes", (dialogInterface, i) -> {
//                    Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
//                    intent.setClassName("com.miui.securitycenter",
//                            "com.miui.permcenter.permissions.PermissionsEditorActivity");
//                    intent.putExtra("extra_pkgname", context.getPackageName());
//                    context.startActivity(intent);
//                }).setNegativeButton(context.getString(R.string.no), null)
//                .show();
//    }

    public static void setScreenSize(Context context) {
        int x, y, orientation = context.getResources().getConfiguration().orientation;
        WindowManager wm = ((WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        Point screenSize = new Point();
        display.getRealSize(screenSize);
        x = screenSize.x;
        y = screenSize.y;

        int width = getWidth(x, y, orientation);
        int height = getHeight(x, y, orientation);
        SharedPreferenceHelper.storeInt(Constant.WIDTH_SCREEN, width);
        SharedPreferenceHelper.storeInt(Constant.HEIGHT_SCREEN, height);
    }

    public static int getWidth(int x, int y, int orientation) {
        return orientation == Configuration.ORIENTATION_PORTRAIT ? x : y;
    }

    private static int getHeight(int x, int y, int orientation) {
        return orientation == Configuration.ORIENTATION_PORTRAIT ? y : x;
    }

    public static void showDialogPermission(Context context) {
        new AlertDialog.Builder(context).setMessage(
                context.getResources().getString(R.string.You_need_to_enable_permissions_to_use_this_feature)).setPositiveButton(
                context.getResources().getString(R.string.go_to_setting), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // navigate to settings
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                        intent.setData(uri);

                        context.startActivity(intent);
                    }
                }).setNegativeButton(context.getResources().getString(R.string.go_back), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // leave?
                dialog.dismiss();
            }
        }).show();
    }

    public static List<BackGround> getBackGround() {
        List<BackGround> backGroundList = new ArrayList<>();

//        Guideline background = App.getInstance().getResources()

        return backGroundList;
    }

    public static List<Theme>  getTheme() {
        List<Theme> themeList = new ArrayList<>();
        return themeList;
    }

    public static List<ImageThief>  getImageThief() {
        List<ImageThief> imageThiefList = new ArrayList<>();
        return imageThiefList;
    }

    public static List<ApplicationInfo> getApplication() {
        List<ApplicationInfo> applicationInfoList = new ArrayList<>();
        return applicationInfoList;
    }

    public static List<AppLock> getAppLock() {
        List<AppLock> appLockList = new ArrayList<>();
        return appLockList;
    }

    public static ArrayList<String> getName() {
        ArrayList<String> nameArrayList = new ArrayList<>();
        return nameArrayList;
    }

//    public static List<BackGround> getListAppLock() {
//        List<AppLock> list = new ArrayList<>();
//        String[] appLock = App.getInstance().getResources().getStringArray(R.array.);
//        return  list;
//    }
}
