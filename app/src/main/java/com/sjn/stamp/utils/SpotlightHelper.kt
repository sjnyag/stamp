package com.sjn.stamp.utils

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager

object SpotlightHelper {
    const val KEY_STAMP_ADD = "stamp_add"

    private const val PREFIX = "shown_"

    fun isShown(activity: Activity?, key: String): Boolean {
        activity ?: return false
        return try {
            activity.getPreferences(MODE_PRIVATE).getBoolean(PREFIX + key + activity.packageManager.getPackageInfo(activity.packageName, 0).versionCode, false)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    fun setShown(activity: Activity?, key: String) {
        activity ?: return
        try {
            activity.getPreferences(MODE_PRIVATE).edit().apply {
                putBoolean(PREFIX + key + activity.packageManager.getPackageInfo(activity.packageName, 0).versionCode, true)
            }.apply()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

    }
}
