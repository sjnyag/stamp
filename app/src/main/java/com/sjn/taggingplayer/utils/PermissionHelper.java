package com.sjn.taggingplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;

public class PermissionHelper {

    public static void requestPermissions(Activity activity, String[] permissionList, int requestCode) {
        if (activity == null || permissionList == null || checkPermission(activity, permissionList)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(activity, permissionList, requestCode);
        }
    }

    public static boolean checkPermission(Context context, String[] permissionList) {
        if (context == null || permissionList == null) {
            return false;
        }
        for (String permission : permissionList) {
            if (PermissionChecker.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity, String[] permissionList) {
        for (String permission : permissionList) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return false;
            }
        }
        return true;
    }
}
