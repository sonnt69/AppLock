package com.tapbi.applock.common.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;


public class AppInfo implements Serializable {

    private String appPackage;

    private String appName;

    private Drawable appIcon;

    private String appType;

    private boolean locked;


    public AppInfo() {

    }

    public AppInfo(String appPackage, String appName, Drawable appIcon, String appType, boolean locked) {
        this.appPackage = appPackage;
        this.appName = appName;
        this.appIcon = appIcon;
        this.appType = appType;
        this.locked = locked;
    }

    public AppInfo(String appName, String appType) {
        this.appName = appName;
        this.appType = appType;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getAppType() {
        return appType;
    }
}
