package com.sjn.stamp.media.provider.multiple

import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat

import com.sjn.stamp.R
import com.sjn.stamp.controller.StampController
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.MediaItemHelper

class MyStampListProvider(context: Context) : MultipleListProvider(context) {

    override val mediaKey: String = MediaMetadataCompat.METADATA_KEY_GENRE

    override val providerMediaId: String = MediaIDHelper.MEDIA_ID_MUSICS_BY_MY_STAMP

    override val titleId: Int = R.string.media_item_label_stamp

    override fun compareMediaList(lhs: MediaMetadataCompat, rhs: MediaMetadataCompat): Int =
            (lhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE)?: "").compareTo(rhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE)?: "")

    override fun updateTrackListMap(musicListById: MutableMap<String, MediaMetadataCompat>) {
        StampController(context).createStampMap(musicListById, trackMap, false)
    }

    override fun createMediaItem(metadata: MediaMetadataCompat, key: String): MediaBrowserCompat.MediaItem =
            MediaItemHelper.createPlayableItem(MediaItemHelper.updateMediaId(metadata, createHierarchyAwareMediaID(metadata, key)))

    override fun getTrackListMap(musicListById: MutableMap<String, MediaMetadataCompat>): MutableMap<String, MutableList<MediaMetadataCompat>> {
        updateTrackListMap(musicListById)
        return trackMap
    }

    private fun createHierarchyAwareMediaID(metadata: MediaMetadataCompat, key: String): String =
            MediaIDHelper.createMediaID(metadata.description.mediaId, providerMediaId, key)

}
