package com.sjn.stamp.media.provider.multiple

import android.content.Context
import android.support.v4.media.MediaMetadataCompat

import com.sjn.stamp.R
import com.sjn.stamp.controller.PlaylistController
import com.sjn.stamp.utils.MediaIDHelper

class PlaylistProvider(context: Context) : MultipleListProvider(context) {

    override val mediaKey: String = MediaMetadataCompat.METADATA_KEY_GENRE

    override val providerMediaId: String = MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST

    override val titleId: Int = R.string.media_item_label_playlist

    override fun updateTrackListMap(musicListById: MutableMap<String, MediaMetadataCompat>) {
        PlaylistController(context).loadAllPlaylist(trackMap)
    }

    override fun compareMediaList(lhs: MediaMetadataCompat, rhs: MediaMetadataCompat): Int =
            compareByTitle(lhs, rhs)

}
