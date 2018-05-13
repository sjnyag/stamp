package com.sjn.stamp.utils

import android.app.Activity
import android.content.Context
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat

object MediaControllerHelper {
    private val TAG = LogHelper.makeLogTag(MediaControllerHelper::class.java)

    fun getController(activity: Activity?): MediaControllerCompat? {
        activity ?: return null
        return MediaControllerCompat.getMediaController(activity)
    }

    fun getController(service: MediaBrowserServiceCompat?): MediaControllerCompat? {
        service?.sessionToken?.let {
            return MediaControllerCompat(service, it)
        }
        return null
    }

    fun getController(context: Context, token: MediaSessionCompat.Token?): MediaControllerCompat? {
        token?.let {
            return MediaControllerCompat(context, it)
        }
        return null
    }

}