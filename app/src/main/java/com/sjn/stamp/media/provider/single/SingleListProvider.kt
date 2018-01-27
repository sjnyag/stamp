package com.sjn.stamp.media.provider.single

import android.content.Context
import android.content.res.Resources
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.media.provider.ListProvider
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.MediaItemHelper
import java.util.*

abstract class SingleListProvider(protected var context: Context) : ListProvider() {

    protected abstract fun createTrackList(musicListById: Map<String, MediaMetadataCompat>): List<MediaMetadataCompat>

    override fun reset() {}

    override fun getListItems(mediaId: String, resources: Resources, state: ListProvider.ProviderState, musicListById: MutableMap<String, MediaMetadataCompat>): List<MediaBrowserCompat.MediaItem> {
        val items = ArrayList<MediaBrowserCompat.MediaItem>()
        if (MediaIDHelper.isTrack(mediaId)) {
            return items
        }
        if (providerMediaId == mediaId) {
            createTrackList(musicListById).mapTo(items) { createMediaItem(it) }
        } else {
            LogHelper.w(TAG, "Skipping unmatched mediaId: ", mediaId)
        }
        return items
    }

    override fun getListByKey(key: String?, state: ListProvider.ProviderState, musicListById: MutableMap<String, MediaMetadataCompat>): List<MediaMetadataCompat> =
            createTrackList(musicListById)

    override fun createMediaItem(metadata: MediaMetadataCompat, key: String): MediaBrowserCompat.MediaItem =
            createMediaItem(metadata)

    private fun createMediaItem(metadata: MediaMetadataCompat): MediaBrowserCompat.MediaItem =
            MediaItemHelper.createPlayableItem(MediaItemHelper.updateMediaId(metadata, MediaIDHelper.createMediaID(metadata.description.mediaId, providerMediaId)))

    companion object {

        private val TAG = LogHelper.makeLogTag(SingleListProvider::class.java)
    }
}
