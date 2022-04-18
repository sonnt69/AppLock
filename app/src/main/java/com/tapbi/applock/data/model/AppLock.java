package com.tapbi.applock.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "AppLock")

public class AppLock implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "appPackage")
    private String appPackage;

    @ColumnInfo(name = "appPass")
    private String appPass;

    public AppLock(){
    }

    public AppLock(@NonNull Integer id, String appPackage, String appPass) {
        this.id = id;
        this.appPackage = appPackage;
        this.appPass = appPass;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public String getAppPass() {
        return appPass;
    }

    public void setAppPass(String appPass) {
        this.appPass = appPass;
    }
}
