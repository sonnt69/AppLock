package com.tapbi.applock.common.model;

public class ShowDrawer {
    boolean isShow;

    public ShowDrawer(){

    }

    public ShowDrawer(boolean isShow) {
        this.isShow = isShow;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }
}
