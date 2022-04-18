package com.tapbi.applock.common.model;

public class DeleteIntruder {
    private boolean isDelete;

    public DeleteIntruder(boolean isDelete) {
        this.isDelete = isDelete;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public DeleteIntruder() {

    }
}
