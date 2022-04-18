package com.tapbi.applock.common.model;


public class CatchIntruder {
    private boolean catchIntruder;

    public CatchIntruder(){

    }

    public CatchIntruder(boolean catchIntruder) {
        this.catchIntruder = catchIntruder;
    }

    public boolean isCatchIntruder() {
        return catchIntruder;
    }

    public void setCatchIntruder(boolean catchIntruder) {
        this.catchIntruder = catchIntruder;
    }
}