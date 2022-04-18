package com.tapbi.applock.service;


import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.tapbi.applock.ui.main.camera.CameraPreview.openCamera;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.common.model.EventLock;
import com.tapbi.applock.common.model.RemoveCamera;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.data.local.db.AppDatabase;
import com.tapbi.applock.data.model.ImageThief;
import com.tapbi.applock.databinding.LayoutLockAppBinding;
import com.tapbi.applock.ui.locksetting.LockSettingActivity;
import com.tapbi.applock.ui.main.MainActivity;
import com.tapbi.applock.ui.main.camera.CameraPreview;
import com.tapbi.applock.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class InitWindowsManager extends Service {
    private static final String CHILD_DIR = "images";
    private static final String TEMP_FILE_NAME = "img";
    private static final String FILE_EXTENSION = ".png";
    private static final int COMPRESS_QUALITY = 50;
    final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private final Handler handler = new Handler();
    HomeAppReceiver homeAppReceiver = new HomeAppReceiver();
    boolean stop = false;

    private boolean isSettingPermission;
    private boolean isAppLockRun;
    private boolean isSettingApp;
    private WindowManager windowManager;
    private WindowManager.LayoutParams mIconViewParams;
    private boolean takePhotoTheif;
    private int numberWrong = 0;
    private Camera camera;
    private Drawable ic_dot;
    private Drawable ic_dot_error;
    private Drawable bg_path;
    private Drawable bg_path_error;
    private boolean allowClick = true;
    private String pass1 = "";
    private int ordinal = 0;
    private String packageName = "";
    //    private SharedPreferences sharedPreferences;
    //   private SharedPreferences question;
    private String questionEmail;
    private String type;
    private LayoutLockAppBinding binding;
    private boolean stopWhite = false;
    //private PatternLockView mPatternLockView;
    private String passDraw = "";
    private boolean vibrate;
    private String passcode;
    private String path;
    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            //Log.d(getClass().getName(), "Pattern drawing started");
            stop = true;
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            if (passDraw.equalsIgnoreCase(PatternLockUtils.patternToString(binding.layoutPattern.ChangeDrawPatternLock, pattern))) {

                removeWindowsManager();
                Intent intent = new Intent(getApplicationContext(), Accessibility.class);
                intent.putExtra(Constant.LOCK, false);
                startService(intent);

                if (isSettingApp) {
                    Log.d("NgocAnh", "onComplete: ");
                    EventBus.getDefault().post(new EventLock(Constant.GO_TO_SETTING));
                    intent = new Intent(Settings.ACTION_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } else {
                stop = false;
                setClearPattern();
                binding.layoutPattern.ChangeDrawPatternLock.setViewMode(PatternLockView.PatternViewMode.WRONG);
                numberWrong++;
                //Log.e("numberWrong", numberWrong + ".");
                if (numberWrong > 0 && numberWrong % 3 == 0) {
                    if (takePhotoTheif) {
                        saveImage();
                    }
                }
            }
        }

        @Override
        public void onCleared() {
            //Log.d(getClass().getName(), "Pattern has been cleared");
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Utils.setTheme(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(Constant.PACKAGE_NAME)) {
            packageName = intent.getExtras().getString(Constant.PACKAGE_NAME);
            Log.e("NGOCANH", "packageName: " + packageName);
            initView();
        }
        registerReceiver(homeAppReceiver, HomeAppFilter());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @NonNull
    private IntentFilter HomeAppFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        return filter;
    }

    private void initView() {

        AppDatabase.getInstance();

        isSettingPermission = SharedPreferenceHelper.getBoolean(Constant.IS_SETTING_APP_PERMISSION, false);
        if (packageName.equals(Constant.PACKAGE_SETTING) && isSettingPermission) {
            isSettingApp = true;
        } else isSettingApp = false;


        binding = LayoutLockAppBinding.inflate(LayoutInflater.from(getBaseContext()));
        binding.imgBack.setVisibility(View.GONE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        sharedPreferences = getSharedPreferences(Constant.SharedPreferences_NAME, MODE_PRIVATE);
        questionEmail = SharedPreferenceHelper.getStringWithDefault(Constant.EMAIL, "");
        type = SharedPreferenceHelper.getStringWithDefault(Constant.TYPE_LOCK, Constant.PATTERN_LOCK);

//        String device_name = (sharedPreferences.getString(Constant.DEVICE_NAME, ""));

        boolean fingerPrint = SharedPreferenceHelper.getBoolean(Constant.FINGER, false);

        if (fingerPrint) {
            binding.layoutFinger.getRoot().setVisibility(View.VISIBLE);
            binding.tvShowErrorFingerPrint.setVisibility(View.GONE);
            binding.layoutTypeAction.tvTypeAction.setText(getBaseContext().getString(R.string.identityVerification));
            binding.layoutTypeAction.tvDesTypeAction.setText(getBaseContext().getString(R.string.useFingerprintsToVerifyYourIdentity));
            useFinger();
        } else {
            initLock();
        }
        initWindowsManager();

        takePhotoTheif = SharedPreferenceHelper.getBoolean(Constant.CATCH_INTRUDER, false);
        //Log.e("takePhotoTheif", takePhotoTheif + ".");
        if (takePhotoTheif) {
            try {
                initCamera();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void initWindowsManager() {
        Log.e("NGOCANH", "InitWindowsManager");

        new Utils().setBackGround(this, binding.layoutLock);
        addWindowsManager();
        evenClickFinger();

        if (isSettingApp) {
            Log.e("NGOCANH", "isSetting APP");
            Intent intent = new Intent(getBaseContext(), LockSettingActivity.class);
            intent.putExtra(Constant.IS_SETTING_APP, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    private void initLock() {
//        if (questionEmail.isEmpty()) {
//            binding.layoutTypeAction.tvDesTypeAction.setVisibility(View.INVISIBLE);
//        }

        vibrate = SharedPreferenceHelper.getBoolean(Constant.VIBRATE, true);
        binding.layoutTypeAction.tvDesTypeAction.setText(getBaseContext().getString(R.string.forgetPass));
        binding.tvShowErrorFingerPrint.setVisibility(View.GONE);

        if (type.equalsIgnoreCase(Constant.PASS_CODE)) {
            binding.layoutTypeAction.tvTypeAction.setText(getBaseContext().getString(R.string.enterYourPassCode));
            binding.inInputPass.getRoot().setVisibility(View.VISIBLE);
            binding.inKeyBoardPassCode.getRoot().setVisibility(View.VISIBLE);

            eventClickPasscode();
        } else {

            createViewDraw();
            binding.layoutTypeAction.tvTypeAction.setText(getBaseContext().getString(R.string.enterYourPatternLock));
            binding.layoutPattern.getRoot().setVisibility(View.VISIBLE);
        }

        binding.layoutTypeAction.tvDesTypeAction.setOnClickListener(view1 -> {
            //forget pass
            if (packageName.equals("com.tapbi.applock")) {
                isAppLockRun = true;
            }
            Log.e("NGOCANH", "isAppLockRun: " + isAppLockRun);

            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra(Constant.FORGET_PASS, Constant.FORGET_PASS);
            intent.putExtra(Constant.IS_LOCK_APP, isAppLockRun);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);


            removeWindowsManager();
            Intent intent2 = new Intent(getApplicationContext(), Accessibility.class);
            intent2.putExtra(Constant.LOCK, false);
            startService(intent2);
        });
    }

    private void evenClickFinger() {
        if (type.equalsIgnoreCase(Constant.PASS_CODE)) {
            binding.layoutFinger.tvTypeUnlock.setText(getText(R.string.usePassCode));
        } else {
            binding.layoutFinger.tvTypeUnlock.setText(getText(R.string.usePatternLock));
        }

        binding.layoutFinger.tvTypeUnlock.setOnClickListener(view1 -> {
            binding.layoutFinger.getRoot().setVisibility(View.GONE);
            initLock();
        });

        binding.imgBack.setOnClickListener(view1 -> {
            Intent startMain = new Intent(Intent.ACTION_MAIN, null);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            //killAppBypackage(current_app);
            removeWindowsManager();
        });
    }


    private void setWhiteFinger() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!stopWhite) {

                    handler.postDelayed(this, 200);
                    binding.layoutFinger.imgIcFinger.setImageDrawable(getDrawable(R.drawable.ic_finger_white));

                }
            }
        }, 200);
    }

    private void useFinger() {
        numberWrong = 0;
        Log.d("HaiPd", "useFinger = " + numberWrong);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            final FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);

            FingerprintManager.AuthenticationCallback authenticationCallback = new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);

                    binding.tvShowErrorFingerPrint.setVisibility(View.VISIBLE);

//                    binding.layoutFinger.getRoot().setVisibility(View.GONE);
//                    initLock();

                    //                    binding.layoutFinger.imgIcFinger.setImageDrawable(getDrawable(R.drawable.ic_finger_error));
//
//                    setWhiteFinger();
//
//                    ++numberWrong;
                    Log.d("HaiPd", "onAuthenticationError numberWrong = " + numberWrong);
//                    if (numberWrong > 0 && numberWrong % 3 == 0) {
//                        if (takePhotoTheif) {
//                            saveImage();
//                        }
//                    }

//                    if (errorCode == 7) {
//                        stopWhite = true;
//                        binding.layoutFinger.imgIcFinger.setImageDrawable(getDrawable(R.drawable.ic_finger_error));
//                    } else {
//                        setWhiteFinger();
//                    }

                }

                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
//                    binding.tvShowErrorFingerPrint.setVisibility(View.GONE);


//                    ++numberWrong;
                    Log.d("HaiPd", "onAuthenticationHelp numberWrong = " + numberWrong);
//
//                    if (numberWrong > 0 && numberWrong % 3 == 0) {
//                        if (takePhotoTheif) {
//                            saveImage();
//                        }
//                    }
//                    binding.layoutFinger.imgIcFinger.setImageDrawable(getDrawable(R.drawable.ic_finger_error));
//                    setWhiteFinger();

                    super.onAuthenticationHelp(helpCode, helpString);
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    //Log.e("onAuthenticationSucce", result + ".");

                    binding.layoutFinger.imgIcFinger.setImageDrawable(getDrawable(R.drawable.ic_finger_success));
                    try {
                        numberWrong = 0;
                        removeWindowsManager();

                        Intent intent2 = new Intent(getApplicationContext(), Accessibility.class);
                        intent2.putExtra(Constant.LOCK, false);
                        startService(intent2);

                        if (isSettingApp) {
                            Log.d("HaiPd", "is setting app ok");

                            EventBus.getDefault().post(new EventLock(Constant.GO_TO_SETTING));
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                    }
                    super.onAuthenticationSucceeded(result);
                }

                @Override
                public void onAuthenticationFailed() {
                    ++numberWrong;
                    Log.d("HaiPd", "onAuthenticationFailed numberWrong = " + numberWrong);

                    if (numberWrong == 3) {
                        if (takePhotoTheif) {
                            saveImage();
                        }
                    }

                    //Log.e("onAuthenticationFailed", ".");
                    binding.layoutFinger.imgIcFinger.setImageDrawable(getDrawable(R.drawable.ic_finger_error));
                    setWhiteFinger();
                    super.onAuthenticationFailed();
                }

            };
            fingerprintManager.authenticate(null, null, 0, authenticationCallback, null);
        }
    }

    private void createViewDraw() {
        Log.e("NGOCANH", "createViewDraw");
        passDraw = (SharedPreferenceHelper.getStringWithDefault(Constant.PATTERN_LOCK, Constant.PASS_DEFAULT_DRAW));
        boolean showDraw = SharedPreferenceHelper.getBoolean(Constant.SHOW_DRAWER, true);

        binding.layoutPattern.ChangeDrawPatternLock.setTactileFeedbackEnabled(vibrate);
        binding.layoutPattern.ChangeDrawPatternLock.setInStealthMode(!showDraw);
        binding.layoutPattern.ChangeDrawPatternLock.addPatternLockListener(mPatternLockViewListener);

    }

    private void setClearPattern() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!stop) {
                    handler.postDelayed(this, 500);
                    binding.layoutPattern.ChangeDrawPatternLock.clearPattern();
                }
            }
        }, 500);
    }

    private void eventClickPasscode() {
        passcode = SharedPreferenceHelper.getStringWithDefault(Constant.PASS_CODE, Constant.PASS_DEFAULT);
        TypedArray typedArray = obtainStyledAttributes(0, R.styleable.theme);
        ic_dot = typedArray.getDrawable(R.styleable.theme_dot_input_passCode);
        bg_path = typedArray.getDrawable(R.styleable.theme_bg_path_input_passCode);
        ic_dot_error = typedArray.getDrawable(R.styleable.theme_dot_input_passCode_error);
        bg_path_error = typedArray.getDrawable(R.styleable.theme_bg_path_input_passCode_error);


        binding.inKeyBoardPassCode.btnBack.setOnClickListener(view1 -> {
            if (ordinal > 0) {
                --ordinal;

                switch (ordinal) {
                    case 0:
                        setTextClearPass();
                        break;
                    case 1:
                        setTextClear2();
                        break;
                    case 2:
                        setTextClear3();
                        break;
                    case 3:
                        setTextClear4();
                        break;
                }
                pass1 = Utils.removeLastChar(pass1);

            } else {
                setTextClearPass();
                ordinal = 0;
                pass1 = "";
            }

//            Intent startMain = new Intent(Intent.ACTION_MAIN, null);
//            startMain.addCategory(Intent.CATEGORY_HOME);
//            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(startMain);
//            //killAppBypackage(current_app);
//            removeWindowsManager();
        });

        binding.inKeyBoardPassCode.btnX.setOnClickListener(view1 -> {
            setTextClearPass();
            ordinal = 0;
            pass1 = "";
        });

        binding.inKeyBoardPassCode.btnNumberZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPass("0");
            }
        });
        binding.inKeyBoardPassCode.btnNumberOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPass("1");
            }
        });
        binding.inKeyBoardPassCode.btnNumberTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPass("2");
            }
        });
        binding.inKeyBoardPassCode.btnNumberThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPass("3");
            }
        });
        binding.inKeyBoardPassCode.btnNumberFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPass("4");
            }
        });
        binding.inKeyBoardPassCode.btnNumberFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPass("5");
            }
        });
        binding.inKeyBoardPassCode.btnNumberSix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPass("6");
            }
        });
        binding.inKeyBoardPassCode.btnNumberSeven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPass("7");
            }
        });
        binding.inKeyBoardPassCode.btnNumberEight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPass("8");
            }
        });
        binding.inKeyBoardPassCode.btnNumberNine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPass("9");
            }
        });
    }

    private void setPass(String number) {
        if (allowClick) {
            if (vibrate) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                assert v != null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(100);
                }
            }
            pass1 = pass1 + number;

            Log.e("NGOCANH", "ordinal: " + ordinal);

            ++ordinal;

            if (ordinal == 1) {
                binding.inInputPass.imgPassOne.setVisibility(View.VISIBLE);
                setBackGroundInputPass(binding.inInputPass.bgPassOne);
            } else if (ordinal == 2) {
                binding.inInputPass.imgPassTwo.setVisibility(View.VISIBLE);
                setBackGroundInputPass(binding.inInputPass.bgPassTwo);
            } else if (ordinal == 3) {
                binding.inInputPass.imgPassThree.setVisibility(View.VISIBLE);
                setBackGroundInputPass(binding.inInputPass.bgPassThree);
            } else if (ordinal == 4) {
                binding.inInputPass.imgPassFour.setVisibility(View.VISIBLE);
                setBackGroundInputPass(binding.inInputPass.bgPassFour);

                allowClick = false;
                if (passcode.equals(pass1)) {
                    removeWindowsManager();
                    Intent intent = new Intent(getApplicationContext(), Accessibility.class);
                    intent.putExtra(Constant.LOCK, false);
                    startService(intent);

                    if (isSettingApp) {
                        EventBus.getDefault().post(new EventLock(Constant.GO_TO_SETTING));
                        Intent intent2 = new Intent(Settings.ACTION_SETTINGS);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent2.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent2);
                    }
                } else {
                    setErrorPass();
                    checkChangePass();
                    numberWrong++;
                    if (numberWrong > 0 && numberWrong % 3 == 0) {
                        if (takePhotoTheif) {
                            saveImage();
                        }
                    }
                }
            }
        }
    }

    private void checkChangePass() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                allowClick = true;
                setBackGroundInputPass(binding.inInputPass.bgPassOne);
                if (ordinal == 4) {
                    ordinal = 0;
                    setTextClearPass();
                }
            }
        }, 300);
    }

    private void setErrorPass() {
        pass1 = "";
        binding.inInputPass.imgPassOne.setImageDrawable(ic_dot_error);
        binding.inInputPass.imgPassTwo.setImageDrawable(ic_dot_error);
        binding.inInputPass.imgPassThree.setImageDrawable(ic_dot_error);
        binding.inInputPass.imgPassFour.setImageDrawable(ic_dot_error);
        binding.inInputPass.bgPassOne.setImageDrawable(bg_path_error);
        binding.inInputPass.bgPassTwo.setImageDrawable(bg_path_error);
        binding.inInputPass.bgPassThree.setImageDrawable(bg_path_error);
        binding.inInputPass.bgPassFour.setImageDrawable(bg_path_error);
    }


    private void setTextClear2() {
        binding.inInputPass.imgPassTwo.setImageDrawable(ic_dot);
        binding.inInputPass.imgPassTwo.setVisibility(View.INVISIBLE);
        binding.inInputPass.bgPassTwo.setImageResource(R.drawable.bg_path_white);
    }

    private void setTextClear3() {
        binding.inInputPass.imgPassThree.setImageDrawable(ic_dot);
        binding.inInputPass.imgPassThree.setVisibility(View.INVISIBLE);
        binding.inInputPass.bgPassThree.setImageResource(R.drawable.bg_path_white);
    }

    private void setTextClear4() {
        binding.inInputPass.imgPassFour.setImageDrawable(ic_dot);
        binding.inInputPass.imgPassFour.setVisibility(View.INVISIBLE);
        binding.inInputPass.bgPassFour.setImageResource(R.drawable.bg_path_white);
    }

    private void setTextClearPass() {
        binding.inInputPass.imgPassOne.setImageDrawable(ic_dot);
        binding.inInputPass.imgPassTwo.setImageDrawable(ic_dot);
        binding.inInputPass.imgPassThree.setImageDrawable(ic_dot);
        binding.inInputPass.imgPassFour.setImageDrawable(ic_dot);

        binding.inInputPass.imgPassOne.setVisibility(View.INVISIBLE);
        binding.inInputPass.imgPassTwo.setVisibility(View.INVISIBLE);
        binding.inInputPass.imgPassThree.setVisibility(View.INVISIBLE);
        binding.inInputPass.imgPassFour.setVisibility(View.INVISIBLE);

//        binding.inInputPass.bgPassOne.setImageDrawable(bg_path);

        binding.inInputPass.bgPassOne.setImageResource(R.drawable.bg_path_white);
        binding.inInputPass.bgPassTwo.setImageResource(R.drawable.bg_path_white);
        binding.inInputPass.bgPassThree.setImageResource(R.drawable.bg_path_white);
        binding.inInputPass.bgPassFour.setImageResource(R.drawable.bg_path_white);
    }

    private void setBackGroundInputPass(ImageView imageView) {
        imageView.setImageDrawable(bg_path);
    }

    private void addWindowsManager() {
        mIconViewParams = new WindowManager.LayoutParams();
//        mIconViewParams.width = width;
//        mIconViewParams.height = height;
        int height = SharedPreferenceHelper.getIntWithDefault(Constant.HEIGHT_SCREEN, 0);
        int width = SharedPreferenceHelper.getIntWithDefault(Constant.WIDTH_SCREEN, 0);

        if (height == 0) {
            mIconViewParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        } else {
            mIconViewParams.height = height + 50;
            //Log.e("height",height+".");
        }
        if (width == 0) {
            mIconViewParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        } else {
            //Log.e("width",width+".");
            mIconViewParams.width = width;
        }

        mIconViewParams.gravity = Gravity.TOP | Gravity.CENTER;
        mIconViewParams.format = PixelFormat.TRANSPARENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mIconViewParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mIconViewParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }
        mIconViewParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        mIconViewParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        int flag = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        binding.getRoot().setSystemUiVisibility(flag);

        windowManager.addView(binding.getRoot(), mIconViewParams);
    }

    private void removeWindowsManager() {
        Log.e("NGOCANH", "removeWindowsManager");
        if (camera != null) {
            camera.release();
        }
        EventBus.getDefault().post(new RemoveCamera(true));
        windowManager.removeView(binding.getRoot());
        unregisterReceiver(homeAppReceiver);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        this.stopSelf();
    }

    private void initCamera() throws Exception {
        camera = Camera.open(1);
        //Timber.e("camrera 1: " + camera);
        CameraPreview cameraPreview = new CameraPreview(getBaseContext(), camera, true);
        //Timber.e("openCamera 1: " + openCamera);
        if (openCamera) {
            binding.flCam.addView(cameraPreview);
            camera.startPreview();
        } else {
            if (camera != null) {
                camera.release();
            }
        }

    }

    public Uri saveToCacheAndGetUri(Bitmap bitmap, @Nullable String name, Context context) {
        File file = saveImgToCache(bitmap, name);

        return getImageUri(file, name);

//        return BitmapUtils.saveImageBitmap(context,bitmap);
    }

    private Uri getImageUri(File fileDir, @Nullable String name) {
        Context context = getBaseContext();
        String fileName = TEMP_FILE_NAME;
        if (!TextUtils.isEmpty(name)) {
            fileName = name;
        }
        File newFile = new File(fileDir, fileName + FILE_EXTENSION);
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", newFile);
    }

    public File saveImgToCache(Bitmap bitmap, @Nullable String name) {
        File cachePath = null;
        String fileName = TEMP_FILE_NAME;
        if (!TextUtils.isEmpty(name)) {
            fileName = name;
        }
        try {
            cachePath = new File(getBaseContext().getCacheDir(), CHILD_DIR);
            cachePath.mkdirs();

            FileOutputStream stream = new FileOutputStream(cachePath + "/" + fileName + FILE_EXTENSION);
            bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, stream);
            stream.close();
            path = cachePath + "/" + fileName + FILE_EXTENSION;
        } catch (IOException e) {
            //Log.e("ds", "saveImgToCache error: " + bitmap, e);
        }
        return cachePath;
    }

    @Subscribe(sticky = true)
    public void onEventLock(EventLock event) {
        Log.e("NGOCANH", "onEventLock EXIT");
        Intent startMain = new Intent(Intent.ACTION_MAIN, null);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startMain.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);

        removeWindowsManager();
    }

    private void saveImage() {
        if (camera != null) {
            //Timber.e("openCamera 2: " + openCamera);
            if (openCamera) {
                try {
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(final byte[] bytes, final Camera camera) {
                            new Thread() {
                                @Override
                                public void run() {
                                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(-90);
                                    Bitmap bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

                                    String title = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                                    Uri uri = (saveToCacheAndGetUri(bitmap, title, getBaseContext()));

                                    ImageThief imageThief = new ImageThief();
                                    imageThief.setUri(String.valueOf(uri));
                                    imageThief.setTime(title);
                                    imageThief.setCachePath(path);

                                    AppDatabase.instance.getImageDAO().insertImageThief(imageThief);
                                    camera.release();
                                }
                            }.start();
                        }
                    });
                } catch (Exception e) {
                    Timber.e("Exception saveImage: " + e);
                }
            }
        }
    }

    private class HomeAppReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                isSettingApp = false;

//                Log.e("NGOCANH", "HomeAppReceiver");
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    removeWindowsManager();
                }
            }
        }
    }
}
