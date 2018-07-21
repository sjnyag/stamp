package com.sjn.stamp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
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

    fun hasKitkat(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
    }

    fun hasJellyBean(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
    }

    fun getColor(resources: Resources, resourceId: Int, theme: Resources.Theme): Int? {
        if (hasMarshmallow()) {
            resources.getColor(resourceId, theme)
        } else if (CompatibleHelper.hasLollipop()) {
            resources.getColor(resourceId)
        }
        return null
    }

    fun saveLayer(canvas: Canvas, bounds: RectF?, paint: Paint): Int = if (hasLollipop()) {
        canvas.saveLayer(bounds, paint)
    } else {
        canvas.saveLayer(bounds, paint, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG or Canvas.FULL_COLOR_LAYER_SAVE_FLAG)
    }
}

fun Context.startForegroundServiceCompatible(intent: Intent) {
    if (CompatibleHelper.hasOreo()) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}