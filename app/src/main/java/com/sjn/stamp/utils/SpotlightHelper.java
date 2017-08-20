package com.sjn.stamp.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import static android.content.Context.MODE_PRIVATE;

public class SpotlightHelper {
    public final static String KEY_STAMP_ADD = "stamp_add";

    private final static String PREFIX = "shown_";

    public static boolean isShown(Activity activity, String key) {
        SharedPreferences sharedPreferences = activity.getPreferences(MODE_PRIVATE);
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            int version = packageInfo.versionCode;
            return sharedPreferences.getBoolean(PREFIX + key + version, false);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setShown(Activity activity, String key) {
        SharedPreferences.Editor editor = activity.getPreferences(MODE_PRIVATE).edit();
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            int version = packageInfo.versionCode;
            editor.putBoolean(PREFIX + key + version, true);
            editor.apply();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
