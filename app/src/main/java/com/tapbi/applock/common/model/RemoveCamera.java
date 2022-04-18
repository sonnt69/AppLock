package com.tapbi.applock.common.model;

public class RemoveCamera {
    private boolean remove;

    public RemoveCamera(boolean remove) {
        this.remove = remove;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }
}
