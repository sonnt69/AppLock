package com.tapbi.applock.interfaces;

import com.tapbi.applock.data.model.ImageThief;

public interface ClickCatchIntruder {
    void multipleSelect();

    void select();

    void unSelect();

    void clickItem(ImageThief imageThief);
}
