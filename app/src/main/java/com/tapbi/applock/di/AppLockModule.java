package com.tapbi.applock.di;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tapbi.applock.data.local.db.AppDAO;
import com.tapbi.applock.data.local.db.AppDatabase;
import com.tapbi.applock.data.local.db.ImageDAO;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppLockModule {
    @Provides
    @Singleton
    public SharedPreferences provideSharedPreference(Application context) {
        return PreferenceManager.getDefaultSharedPreferences(context);

    }

//    @Provides
//    @Singleton
//    public AppDatabase provideRoomDb(Application context) {
//        return Room.databaseBuilder(context, AppDatabase.class, Constant.DATA_NAME)
//                .fallbackToDestructiveMigration()
//                .addMigrations(AppDatabase.getInstance()).allowMainThreadQueries().build();
//    }

    @Provides
    @Singleton
    public AppDAO pAppDao(AppDatabase db) {
        return db.getAppDAO();
    }
    @Provides
    @Singleton
    public ImageDAO pImageDAO(AppDatabase db){
        return db.getImageDAO();
    }


}
