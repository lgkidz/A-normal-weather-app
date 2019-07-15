package com.OdiousPanda.thefweather.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtil {
    public static final String TEMPERATURE_UNIT = "tempUnit";
    public static final String DISTANCE_UNIT = "distanceUnit";
    public static final String SPEED_UNIT = "speedUnit";
    public static final String PRESSURE_UNIT = "pressureUnit";
    public static final String EXPLICIT_SETTING = "explicitOrNot";
    // Shared preferences file name
    private static final String PREF_NAME = "RainbowPreferencesName";
    private static final String IS_FIRST_TIME_LAUNCH = "IsAppFirstTimeLaunch";
    private static final String WIDGET_ACTION_TAP = "widgetTap";
    private static final String DEFAULT_TEMPERATURE_UNIT = "°C";
    private static final String DEFAULT_DISTANCE_UNIT = "km";
    private static final String DEFAULT_SPEED_UNIT = "kmph";
    private static final String DEFAULT_PRESSURE_UNIT = "psi";
    private static final boolean DEFAULT_EXPLICIT_SETTING = true;
    private static final boolean DEFAULT_FIRST_TIME_LAUNCH_VALUE = true;

    public static boolean isNotFirstTimeLaunch(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean firstTime = pref.getBoolean(IS_FIRST_TIME_LAUNCH, DEFAULT_FIRST_TIME_LAUNCH_VALUE);
        return !firstTime;
    }

    public static void setFirstTimeLaunch(Context context, boolean isFirstTime) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.apply();
    }

    public static String getTemperatureUnit(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(TEMPERATURE_UNIT, DEFAULT_TEMPERATURE_UNIT);
    }

    public static String getDistanceUnit(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(DISTANCE_UNIT, DEFAULT_DISTANCE_UNIT);
    }

    public static String getSpeedUnit(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(SPEED_UNIT, DEFAULT_SPEED_UNIT);
    }

    public static String getPressureUnit(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(PRESSURE_UNIT, DEFAULT_PRESSURE_UNIT);
    }

    public static boolean isExplicit(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(EXPLICIT_SETTING, DEFAULT_EXPLICIT_SETTING);
    }

    public static void setUnitSetting(Context context, String preferenceName, String value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(preferenceName, value);
        editor.apply();
    }

    public static void setExplicitSetting(Context context, boolean value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(EXPLICIT_SETTING, value);
        editor.apply();
    }

    public static int getWidgetTapCount(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(WIDGET_ACTION_TAP, 0);
    }

    public static void setWidgetTapCount(Context context, int count) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(WIDGET_ACTION_TAP, count);
        editor.apply();
    }
}
