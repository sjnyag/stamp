package com.sjn.stamp.utils

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.preference.PreferenceManager

@Suppress("unused")
object PreferenceHelper {
    private val TAG = LogHelper.makeLogTag(PreferenceHelper::class.java)

    object Key {

        const val HIDE_ALBUM_ART_ON_LOCK_SCREEN = "HIDE_ALBUM_ART_ON_LOCK_SCREEN"

        object PlayMode {
            const val REPEAT_MODE = "REPEAT_MODE"
            const val SHUFFLE_MODE = "SHUFFLE_MODE"
        }

        object Spotlight {
            const val PREFIX = "shown_"
            const val STAMP_ADD = "stamp_add"
        }

    }

    fun isHideAlbumArtOnLockScreen(context: Context?): Boolean {
        context ?: return false
        return try {
            PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Key.HIDE_ALBUM_ART_ON_LOCK_SCREEN + context.versionCode(), false)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    fun setHideAlbumArtOnLockScreen(context: Context?, value: Boolean) {
        context ?: return
        try {
            PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
                putBoolean(Key.HIDE_ALBUM_ART_ON_LOCK_SCREEN + context.versionCode(), value)
            }.apply()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    fun isSpotlightShown(context: Context?, key: String): Boolean {
        context ?: return false
        return try {
            PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Key.Spotlight.PREFIX + key + context.versionCode(), false)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    fun setSpotlightShown(context: Context?, key: String) {
        context ?: return
        try {
            PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
                putBoolean(Key.Spotlight.PREFIX + key + context.versionCode(), true)
            }.apply()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    fun saveShuffle(context: Context, @PlaybackStateCompat.ShuffleMode shuffleMode: Int) {
        saveInt(context, Key.PlayMode.SHUFFLE_MODE, shuffleMode)
    }

    fun loadShuffle(context: Context, @PlaybackStateCompat.ShuffleMode default: Int): Int =
            PreferenceManager.getDefaultSharedPreferences(context).getInt(Key.PlayMode.SHUFFLE_MODE, default)

    fun saveRepeat(context: Context, @PlaybackStateCompat.RepeatMode repeatMode: Int) {
        saveInt(context, Key.PlayMode.REPEAT_MODE, repeatMode)
    }

    fun loadRepeat(context: Context, @PlaybackStateCompat.RepeatMode default: Int): Int =
            PreferenceManager.getDefaultSharedPreferences(context).getInt(Key.PlayMode.REPEAT_MODE, default)

    private fun Context.versionCode() = packageManager.getPackageInfo(packageName, 0).versionCode

    private fun saveString(context: Context, key: String, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply()
    }

    private fun saveInt(context: Context, key: String, value: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).apply()
    }

}