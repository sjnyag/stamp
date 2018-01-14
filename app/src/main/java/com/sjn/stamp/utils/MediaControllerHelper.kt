package com.sjn.stamp.utils

import android.app.Activity
import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat

object MediaControllerHelper {
    private val TAG = LogHelper.makeLogTag(MediaControllerHelper::class.java)

    fun getController(activity: Activity): MediaControllerCompat? {
        return MediaControllerCompat.getMediaController(activity)
    }

    fun getController(context: Context, token: MediaSessionCompat.Token?): MediaControllerCompat? {
        token?.let {
            return MediaControllerCompat(context, it)
        }
        return null
    }

}