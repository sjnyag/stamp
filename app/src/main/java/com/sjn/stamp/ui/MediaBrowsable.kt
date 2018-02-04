package com.sjn.stamp.ui

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat

interface MediaBrowsable {
    val mediaBrowser: MediaBrowserCompat?

    fun search(query: String, extras: Bundle?, callback: MediaBrowserCompat.SearchCallback)

    fun playByCategory(mediaId: String)

    fun onMediaItemSelected(musicId: String)

    fun onMediaItemSelected(mediaId: String?, isPlayable: Boolean, isBrowsable: Boolean)

    fun onMediaItemSelected(item: MediaBrowserCompat.MediaItem)

    fun sendCustomAction(action: String, extras: Bundle?, callback: MediaBrowserCompat.CustomActionCallback?)
}
