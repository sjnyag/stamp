package com.sjn.stamp.utils;

import android.content.res.Resources;
import android.os.Build;

public class CompatibleHelper {
    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasHoneycomb() {
        return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasOreo() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static Integer getColor(Resources resources, int resourceId, Resources.Theme theme) {
        if (hasMarshmallow()) {
            resources.getColor(resourceId, theme);
        } else if (CompatibleHelper.hasLollipop()) {
            resources.getColor(resourceId);
        }
        return null;
    }
}
