package com.sjn.stamp.utils

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.preference.PreferenceManager

object PreferenceHelper {
    private val TAG = LogHelper.makeLogTag(PreferenceHelper::class.java)

    object KEY {
        const val REPEAT_MODE = "REPEAT_MODE"
        const val SHUFFLE_MODE = "SHUFFLE_MODE"
    }

    fun saveShuffle(context: Context, @PlaybackStateCompat.ShuffleMode shuffleMode: Int) {
        saveInt(context, KEY.SHUFFLE_MODE, shuffleMode)
    }

    fun loadShuffle(context: Context, @PlaybackStateCompat.ShuffleMode default: Int): Int =
            PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY.SHUFFLE_MODE, default)

    fun saveRepeat(context: Context, @PlaybackStateCompat.RepeatMode repeatMode: Int) {
        saveInt(context, KEY.REPEAT_MODE, repeatMode)
    }

    fun loadRepeat(context: Context, @PlaybackStateCompat.RepeatMode default: Int): Int =
            PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY.REPEAT_MODE, default)

    private fun saveString(context: Context, key: String, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply()
    }

    private fun saveInt(context: Context, key: String, value: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).apply()
    }

}