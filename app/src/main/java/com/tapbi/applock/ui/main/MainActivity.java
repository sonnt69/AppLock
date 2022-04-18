package com.tapbi.applock.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.jaeger.library.StatusBarUtil;
import com.tapbi.applock.R;
import com.tapbi.applock.common.model.EventLock;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.data.local.db.AppDatabase;
import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.databinding.ActivityMainBinding;
import com.tapbi.applock.interfaces.OnBackPressed;
import com.tapbi.applock.service.Accessibility;
import com.tapbi.applock.ui.base.BaseBindingActivity;
import com.tapbi.applock.utils.Utils;

import timber.log.Timber;

import com.tapbi.applock.common.Constant;

import org.greenrobot.eventbus.EventBus;

import java.util.logging.Logger;


@SuppressWarnings("ALL")
public class MainActivity extends BaseBindingActivity<ActivityMainBinding, MainViewModel> {
    private String passDraw = "";
    private String type = "";
    private FragmentManager fragmentManager;
    private ImageView img_ic_home;
    private ImageView img_ic_search;
    private int heightSttBar = 0;
    public static int heightNavigationBar = 0;

    public NavController navControllerMain;
    public NavHostFragment navHostFragmentMain;
    private NavGraph graph;

    public void changeMainScreen(int idScreen, Bundle bundle) {
        if (graph == null) {
            graph = navControllerMain.getNavInflater().inflate(R.navigation.nav_main);
        }
        graph.setStartDestination(idScreen);
        navControllerMain.setGraph(graph, bundle);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d("NGOCANH", "onCreate");

//        Utils.setTheme(this);

        type = SharedPreferenceHelper.getStringWithDefault(Constant.TYPE_LOCK, Constant.PATTERN_LOCK);

        Intent intent = getIntent();

        if(intent != null) {
            Log.d("NGOCANH", "intent != null");
             if (intent.hasExtra(Constant.FORGET_PASS)) {
                 intent.removeExtra(Constant.FORGET_PASS);
                 Log.d("NGOCANH", "intent FORGET_PASS");

                Bundle bundle = new Bundle();
                bundle.putBoolean(Constant.FORGET_PASS, true);

                if(intent.hasExtra(Constant.IS_LOCK_APP)){
                    bundle.putBoolean(Constant.IS_LOCK_APP, intent.getExtras().getBoolean(Constant.IS_LOCK_APP));
                    intent.removeExtra(Constant.IS_LOCK_APP);
                }

                changeMainScreen(R.id.settingQuestionFragment, bundle);
            }
        }

    }

    @Override
    public void setupView(Bundle savedInstanceState) {
        Log.d("NGOCANH", "setupView");

        navHostFragmentMain = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if (navHostFragmentMain != null) {
            navControllerMain = navHostFragmentMain.getNavController();
        }

        viewModel.getAppsInfo(this);
        passDraw = SharedPreferenceHelper.getStringWithDefault(Constant.PATTERN_LOCK, "");
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public Class<MainViewModel> getViewModel() {
        return MainViewModel.class;
    }

    @Override
    public void setupData() {

    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.mobile_navigation).navigateUp();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.e("onPause", "onPause");
        try {
            stopService(new Intent(this, Accessibility.class));
            startService(new Intent(this, Accessibility.class));
            //Log.e("onPause1", "onPause1");
        } catch (Exception e) {
        }

    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (!(fragment instanceof OnBackPressed) || !((OnBackPressed) fragment).onBackPressed()) {
            super.onBackPressed();
        }
        EventBus.getDefault().post(new EventLock(Constant.EXIT));
        //finishAffinity();
    }


//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (getCurrentFocus() != null) {
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    public static class WindowManager extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            finish();
        }
    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.canDrawOverlays(this)) {
//                //TODO show non cancellable dialog
//                new CountDownTimer(15000, 1000) {
//                    @Override
//                    public void onTick(long millisUntilFinished) {
//                        if (Settings.canDrawOverlays(getApplicationContext())) {
//                            this.cancel(); // cancel the timer
//                            // Overlay permission granted
//                            //TODO dismiss dialog and continue
//                        }
//                    }
//
//                    @Override
//                    public void onFinish() {
//                        //TODO dismiss dialog
//                        if (Settings.canDrawOverlays(getApplicationContext())) {
//                            //TODO Overlay permission granted
//                        } else {
//                            //TODO user may have denied it.
//                        }
//                    }
//                }.start();
//            }
//        }
//    }
}
