package com.tapbi.applock.interfaces;

import com.tapbi.applock.common.model.AppInfo;
import com.tapbi.applock.data.model.AppLock;

public interface ClickAppLock {
    void locked(AppLock appLock);
    void unLock(AppLock appLock);
}
