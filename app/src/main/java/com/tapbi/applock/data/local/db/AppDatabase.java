package com.tapbi.applock.data.local.db;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.tapbi.applock.App;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.data.model.ImageThief;


@Database(entities = {AppLock.class, ImageThief.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static AppDatabase instance;
    public static AppDatabase getInstance() {
        if (instance == null) {
            instance = Room.databaseBuilder(App.getInstance(), AppDatabase.class, Constant.DATA_NAME)
                    .addMigrations(MIGRATION_1_2)
                    .allowMainThreadQueries().build();
        }

        return instance;
    }

    public abstract AppDAO getAppDAO();

    public abstract ImageDAO getImageDAO();

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };
}
