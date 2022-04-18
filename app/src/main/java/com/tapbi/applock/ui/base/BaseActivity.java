package com.tapbi.applock.ui.base;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tapbi.applock.utils.LocaleUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LocaleUtils.applyLocale(this);
        super.onCreate(savedInstanceState);
        changeSystemUIColor(Color.TRANSPARENT, Color.TRANSPARENT, true, true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void changeSystemUIColor(int colorStatusBar,int colorNavigationBar,boolean darkStatusBar, boolean darkNavigation) {
        int systemUiScrim = Color.parseColor("#40000000");
        int systemUiVisibility = 0;
        // Use a dark scrim by default since light status is API 23+
        WindowManager.LayoutParams winParams = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            if(darkNavigation){
                getWindow().setNavigationBarColor(colorNavigationBar);
            }else{
                getWindow().setNavigationBarColor(systemUiScrim);
            }
            if(darkStatusBar){
                getWindow().setStatusBarColor(colorStatusBar);
            }else{
                getWindow().setStatusBarColor(systemUiScrim);
            }

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!darkStatusBar) {
                systemUiVisibility = systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().setStatusBarColor(colorStatusBar);
            if(darkNavigation){
                getWindow().setNavigationBarColor(colorNavigationBar);
            }else{
                getWindow().setNavigationBarColor(systemUiScrim);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!darkNavigation) {
                systemUiVisibility = systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            getWindow().setNavigationBarColor(colorNavigationBar);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getWindow().setNavigationBarContrastEnforced(false);
            }

        }
        systemUiVisibility = systemUiVisibility |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
        getWindow().setAttributes(winParams);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

}
