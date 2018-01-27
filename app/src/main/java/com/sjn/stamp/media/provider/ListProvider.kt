package com.sjn.stamp.media.provider

import android.content.res.Resources
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.utils.MediaItemHelper


abstract class ListProvider {

    protected abstract val providerMediaId: String

    protected abstract val titleId: Int

    enum class ProviderState {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    fun getRootMenu(resources: Resources): MediaBrowserCompat.MediaItem = MediaItemHelper.createBrowsableItem(providerMediaId, resources.getString(titleId))

    abstract fun reset()

    abstract fun getListItems(mediaId: String, resources: Resources, state: ProviderState,
                              musicListById: MutableMap<String, MediaMetadataCompat>): List<MediaBrowserCompat.MediaItem>

    abstract fun getListByKey(key: String?, state: ProviderState, musicListById: MutableMap<String, MediaMetadataCompat>): List<MediaMetadataCompat>

    protected abstract fun createMediaItem(metadata: MediaMetadataCompat, key: String): MediaBrowserCompat.MediaItem
}
