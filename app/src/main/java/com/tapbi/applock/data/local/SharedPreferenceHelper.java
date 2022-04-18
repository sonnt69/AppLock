package com.tapbi.applock.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import javax.inject.Inject;

public class SharedPreferenceHelper {
    private static final int DEFAULT_NUM = 0;
    private static final String DEFAULT_STRING = "";
    public static SharedPreferences sharedPreferences;

    @Inject
    public SharedPreferenceHelper(SharedPreferences sharedPreferences) {
        SharedPreferenceHelper.sharedPreferences = sharedPreferences;
    }


    public static void storeStringLanguage(Context context, String key, String value) {
        if(sharedPreferences != null) {
            sharedPreferences.edit().putString(key, value).apply();
        }
    }


    public static String getString(String key) {
        if(sharedPreferences != null){
            return sharedPreferences.getString(key, DEFAULT_STRING);
        }
        return "";
    }

    public static void storeString(String key, String value) {
        if(sharedPreferences != null) {
            sharedPreferences.edit().putString(key, value).apply();
        }
    }

    public static String getStringWithDefault(String key, String defaultValue) {
        if(sharedPreferences != null) {
            return sharedPreferences.getString(key, defaultValue);
        }
        return "";
    }

    public static void storeInt(String key, int value) {
        if(sharedPreferences != null) {
            sharedPreferences.edit().putInt(key, value).apply();
        }
    }

    public static int getInt(String key) {
        if(sharedPreferences != null) {
            return sharedPreferences.getInt(key, DEFAULT_NUM);
        }
        return 0;
    }

    public static int getIntWithDefault(String key, int defaultValue) {
        if(sharedPreferences != null) {
            return sharedPreferences.getInt(key, defaultValue);
        }
        return 0;
    }

    public static void storeLong(String key, long value) {
        if(sharedPreferences != null) {
            sharedPreferences.edit().putLong(key, value).apply();
        }
    }

    public static long getLong(String key) {
        if(sharedPreferences != null) {
            return sharedPreferences.getLong(key, DEFAULT_NUM);
        }
        return 0;
    }


    public static void storeBoolean(String key, boolean value) {
        if(sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(key, value).apply();
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        if(sharedPreferences != null) {
            return sharedPreferences.getBoolean(key, defaultValue);
        }
        return false;
    }


    public static void storeFloat(String key, float value) {
        if(sharedPreferences != null) {
            sharedPreferences.edit().putFloat(key, value).apply();
        }
    }

    public static float getFloat(String key) {
        if(sharedPreferences != null) {
            return sharedPreferences.getFloat(key, 0f);
        }
        return 0;
    }


}



