package com.tapbi.applock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.room.Room;

import com.tapbi.applock.common.Constant;
import com.tapbi.applock.data.local.db.AppDatabase;
import com.tapbi.applock.utils.LocaleUtils;

import dagger.hilt.android.HiltAndroidApp;
import timber.log.Timber;


@HiltAndroidApp
public class App extends MultiDexApplication {
    @Override
    public void onCreate() {
        initLog();
        super.onCreate();
        instance = this;
        initRoom();

    }

    private void initRoom() {
        Room.databaseBuilder(this, AppDatabase.class, Constant.DATA_NAME)
                .allowMainThreadQueries().build();
    }

    private void initLog(){
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }
    }


    public static Context appContext;
    private static App instance;
    public SharedPreferences mPrefs;

    public static Context getContext() {
        return appContext;
    }

    public static App getInstance() {
        return instance;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleUtils.applyLocale(this);
    }
}


