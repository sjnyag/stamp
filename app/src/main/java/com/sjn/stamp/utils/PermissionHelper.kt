package com.sjn.stamp.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.PermissionChecker

@Suppress("unused")
object PermissionHelper {

    fun requestPermissions(activity: Activity?, permissionList: Array<String>?, requestCode: Int): Boolean {
        if (activity == null || permissionList == null || hasPermission(activity, permissionList)) {
            return false
        }
        ActivityCompat.requestPermissions(activity, permissionList, requestCode)
        return true
    }

    fun hasPermission(context: Context?, permissionList: Array<String>?): Boolean {
        if (context == null || permissionList == null) {
            return false
        }
        return permissionList.none { PermissionChecker.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
    }

    private fun getShouldShowRequestPermissionList(activity: Activity, permissionList: Array<String>): Array<String> {
        val shouldShowRequestPermissionList = permissionList.filter { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
        return shouldShowRequestPermissionList.toTypedArray()
    }

}
