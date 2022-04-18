package com.tapbi.applock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

public class LocaleUtils {
    public static void applyLocale(Context context) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);

    }

    public static class Intent {

    }

    public static class MyFocusChangeListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View view, boolean b) {

        }

    }
}