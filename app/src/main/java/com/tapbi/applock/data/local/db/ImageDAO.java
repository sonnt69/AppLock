package com.tapbi.applock.data.local.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.data.model.ImageThief;

import java.util.List;

@Dao
public interface ImageDAO {
    @Query("SELECT * FROM ImageThief")
    List<ImageThief> getImageThief();

    @Query("SELECT * FROM ImageThief")
    LiveData<List<ImageThief>> getImageThiefInDB();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertImageThief(ImageThief... imageThiefs);

    @Delete
    void deleteImageThief(ImageThief imageThief);

    @Query("DELETE FROM ImageThief WHERE id = :id")
    void deleteImageByID(Integer id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateImageThief(ImageThief imageThief);

    @Query("DELETE FROM ImageThief")
    int deleteAll();



}
