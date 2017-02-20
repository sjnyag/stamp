package com.sjn.taggingplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {

    public static boolean requestPermissions(Activity activity, String[] permissionList, int requestCode) {
        if (activity == null || permissionList == null || hasPermission(activity, permissionList)) {
            return false;
        }
        ActivityCompat.requestPermissions(activity, permissionList, requestCode);
        return true;
    }

    public static boolean hasPermission(Context context, String[] permissionList) {
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

    private static String[] getShouldShowRequestPermissionList(Activity activity, String[] permissionList) {
        List<String> shouldShowRequestPermissionList = new ArrayList<>();
        for (String permission : permissionList) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                shouldShowRequestPermissionList.add(permission);
            }
        }
        return shouldShowRequestPermissionList.toArray(new String[0]);
    }

}
