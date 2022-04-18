package com.tapbi.applock.data.local.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tapbi.applock.data.model.AppLock;

import java.util.List;

@Dao
public interface AppDAO {
    @Query("SELECT * FROM AppLock")
    List<AppLock> getAppLock();

    @Query("SELECT * FROM AppLock")
    LiveData<List<AppLock>> getLockedApp();

    @Query("SELECT COUNT(id) FROM AppLock")
    LiveData<Integer> getRowCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAppLock(AppLock... appLocks);

    @Delete
    void deleteAppLock(AppLock appLock);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAppLock(AppLock appLock);

    @Query("DELETE FROM AppLock")
    int deleteAll();

    @Query("DELETE FROM AppLock WHERE appPackage = :appPackage")
    void deleteByPackage(String appPackage);

    @Query("SELECT EXISTS(SELECT * FROM AppLock WHERE appPackage = :packageName)")
    boolean isExistPackage(String packageName);
}
