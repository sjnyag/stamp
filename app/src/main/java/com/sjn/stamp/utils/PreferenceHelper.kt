package com.sjn.stamp.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.preference.PreferenceManager

@Suppress("unused")
object PreferenceHelper {
    private val TAG = LogHelper.makeLogTag(PreferenceHelper::class.java)

    object PlayModeKey {
        const val REPEAT_MODE = "REPEAT_MODE"
        const val SHUFFLE_MODE = "SHUFFLE_MODE"
    }

    private const val SPOTLIGHT_PREFIX = "shown_"

    object SpotlightKey {
        const val STAMP_ADD = "stamp_add"
    }


    fun isSpotlightShown(activity: Activity?, key: String): Boolean {
        activity ?: return false
        return try {
            activity.getPreferences(Context.MODE_PRIVATE).getBoolean(SPOTLIGHT_PREFIX + key + activity.packageManager.getPackageInfo(activity.packageName, 0).versionCode, false)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    fun setSpotlightShown(activity: Activity?, key: String) {
        activity ?: return
        try {
            activity.getPreferences(Context.MODE_PRIVATE).edit().apply {
                putBoolean(SPOTLIGHT_PREFIX + key + activity.packageManager.getPackageInfo(activity.packageName, 0).versionCode, true)
            }.apply()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    fun saveShuffle(context: Context, @PlaybackStateCompat.ShuffleMode shuffleMode: Int) {
        saveInt(context, PlayModeKey.SHUFFLE_MODE, shuffleMode)
    }

    fun loadShuffle(context: Context, @PlaybackStateCompat.ShuffleMode default: Int): Int =
            PreferenceManager.getDefaultSharedPreferences(context).getInt(PlayModeKey.SHUFFLE_MODE, default)

    fun saveRepeat(context: Context, @PlaybackStateCompat.RepeatMode repeatMode: Int) {
        saveInt(context, PlayModeKey.REPEAT_MODE, repeatMode)
    }

    fun loadRepeat(context: Context, @PlaybackStateCompat.RepeatMode default: Int): Int =
            PreferenceManager.getDefaultSharedPreferences(context).getInt(PlayModeKey.REPEAT_MODE, default)

    private fun saveString(context: Context, key: String, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply()
    }

    private fun saveInt(context: Context, key: String, value: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).apply()
    }

}