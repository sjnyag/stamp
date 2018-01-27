package com.sjn.stamp.utils

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build

object CompatibleHelper {
    private fun hasMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun hasLollipop(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    @SuppressLint("ObsoleteSdkInt")
    fun hasHoneycomb(): Boolean {
        return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB
    }

    fun hasOreo(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun getColor(resources: Resources, resourceId: Int, theme: Resources.Theme): Int? {
        if (hasMarshmallow()) {
            resources.getColor(resourceId, theme)
        } else if (CompatibleHelper.hasLollipop()) {
            resources.getColor(resourceId)
        }
        return null
    }
}
