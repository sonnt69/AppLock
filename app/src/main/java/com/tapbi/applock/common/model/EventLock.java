package com.tapbi.applock.common.model;

public class EventLock {
    private String type;

    public EventLock(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
