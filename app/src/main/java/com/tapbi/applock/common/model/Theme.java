package com.tapbi.applock.common.model;

public class Theme {
    private boolean isInstalled;
    private String uri;

    public Theme(boolean isInstalled, String uri) {
        this.isInstalled = isInstalled;
        this.uri = uri;
    }

    public Theme() {

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
