package com.sjn.stamp.media.source

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.utils.MediaRetrieveHelper

class LocalMediaSource(private val context: Context, private val callback: MediaRetrieveHelper.PermissionRequiredCallback) : MusicProviderSource {

    override fun iterator(): Iterator<MediaMetadataCompat> {
        if (!MediaRetrieveHelper.hasPermission(context)) {
            callback.onPermissionRequired()
            return emptyList<MediaMetadataCompat>().iterator()
        }
        return MediaRetrieveHelper.createIterator(MediaRetrieveHelper.retrieveAllMedia(context, callback))
    }
}
