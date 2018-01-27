package com.sjn.stamp.media.provider.single

import android.content.Context
import android.support.v4.media.MediaMetadataCompat

import com.sjn.stamp.R
import com.sjn.stamp.controller.SongHistoryController
import com.sjn.stamp.utils.MediaIDHelper

class TopSongProvider(context: Context) : SingleListProvider(context) {

    override val providerMediaId: String = MediaIDHelper.MEDIA_ID_MUSICS_BY_MOST_PLAYED

    override val titleId: Int = R.string.media_item_label_most_played

    override fun createTrackList(musicListById: Map<String, MediaMetadataCompat>): List<MediaMetadataCompat> =
            SongHistoryController(context).topSongList

}
