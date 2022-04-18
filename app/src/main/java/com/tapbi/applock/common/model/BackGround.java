package com.tapbi.applock.common.model;

import java.io.Serializable;

public class BackGround implements Serializable {
    private boolean isInstalled;
    private String uri;

    public BackGround(boolean isInstalled, String uri) {
        this.isInstalled = isInstalled;
        this.uri = uri;
    }

    public BackGround() {

    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public void setInstalled(boolean installed) {
        isInstalled = installed;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
