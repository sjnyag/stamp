package com.sjn.stamp.media.provider.multiple

import android.content.Context
import android.content.res.Resources
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.google.common.collect.Lists
import com.sjn.stamp.media.provider.ListProvider
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.MediaItemHelper
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class MultipleListProvider(protected var context: Context) : ListProvider() {

    protected val trackMap: MutableMap<String, MutableList<MediaMetadataCompat>> = ConcurrentHashMap()

    protected abstract val mediaKey: String

    protected abstract fun compareMediaList(lhs: MediaMetadataCompat, rhs: MediaMetadataCompat): Int

    override fun reset() = trackMap.clear()

    protected open fun updateTrackListMap(musicListById: MutableMap<String, MediaMetadataCompat>) {
        for (music in musicListById.values) {
            val key = MediaIDHelper.escape(music.getString(mediaKey) ?: "")
            if (!key.isEmpty()) {
                var list = trackMap[key]
                if (list == null) {
                    list = ArrayList()
                    trackMap[key] = list
                }
                list.add(music)
            }
        }
    }

    override fun getListItems(mediaId: String, resources: Resources, state: ListProvider.ProviderState, musicListById: MutableMap<String, MediaMetadataCompat>): MutableList<MediaBrowserCompat.MediaItem> {
        val items = ArrayList<MediaBrowserCompat.MediaItem>()
        if (MediaIDHelper.isTrack(mediaId)) {
            return items
        }
        when {
            providerMediaId == mediaId -> getKeys(state, musicListById).mapTo(items) { createBrowsableMediaItemForKey(it) }
            mediaId.startsWith(providerMediaId) -> {
                MediaIDHelper.getHierarchy(mediaId)[1].let { key ->
                    getListByKey(key, state, musicListById).mapTo(items) { createMediaItem(it, key) }
                }
            }
            else -> LogHelper.w(TAG, "Skipping unmatched mediaId: ", mediaId)
        }
        return items
    }

    /**
     * Get music tracks of the given key
     */
    override fun getListByKey(key: String?, state: ListProvider.ProviderState, musicListById: MutableMap<String, MediaMetadataCompat>): List<MediaMetadataCompat> {
        if (state !== ListProvider.ProviderState.INITIALIZED || !getTrackListMap(musicListById).containsKey(key)) {
            return emptyList()
        }
        return ArrayList(getTrackListMap(musicListById)[key]).apply {
            this.sortWith(Comparator { lhs, rhs -> compareMediaList(lhs, rhs) })
        }
    }

    override fun createMediaItem(metadata: MediaMetadataCompat, key: String): MediaBrowserCompat.MediaItem = MediaItemHelper.createPlayableItem(MediaItemHelper.updateMediaId(metadata, createHierarchyAwareMediaID(metadata)))

    private fun getKeys(state: ListProvider.ProviderState, musicListById: MutableMap<String, MediaMetadataCompat>): List<String> {
        if (state !== ListProvider.ProviderState.INITIALIZED) {
            return emptyList()
        }
        val trackListMap = getTrackListMap(musicListById)
        if (trackListMap.isEmpty()) {
            return ArrayList()
        }
        val list = Lists.newArrayList(trackListMap.keys)
        list.sort()
        return list
    }

    private fun createHierarchyAwareMediaID(metadata: MediaMetadataCompat): String = MediaIDHelper.createMediaID(metadata.description.mediaId, providerMediaId, metadata.getString(mediaKey)
            ?: "")

    private fun createBrowsableMediaItemForKey(key: String): MediaBrowserCompat.MediaItem = MediaItemHelper.createBrowsableItem(MediaIDHelper.createMediaID(null, providerMediaId, key), MediaIDHelper.unescape(key))

    protected open fun getTrackListMap(musicListById: MutableMap<String, MediaMetadataCompat>): MutableMap<String, MutableList<MediaMetadataCompat>> {
        if (trackMap.isEmpty()) {
            updateTrackListMap(musicListById)
        }
        return trackMap
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(MultipleListProvider::class.java)
    }
}
