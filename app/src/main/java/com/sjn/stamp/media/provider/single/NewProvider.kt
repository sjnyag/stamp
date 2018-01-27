package com.sjn.stamp.media.provider.single

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.R
import com.sjn.stamp.controller.UserSettingController
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.TimeHelper
import java.util.*

class NewProvider(context: Context) : AllProvider(context) {

    override val providerMediaId: String = MediaIDHelper.MEDIA_ID_MUSICS_BY_NEW

    override val titleId: Int = R.string.media_item_label_new

    override fun createTrackList(musicListById: Map<String, MediaMetadataCompat>): List<MediaMetadataCompat> =
            subList(super.createTrackList(musicListById), UserSettingController().newSongDays)

    override fun compareMediaList(lhs: MediaMetadataCompat, rhs: MediaMetadataCompat): Int =
            rhs.getString(MediaMetadataCompat.METADATA_KEY_DATE).compareTo(lhs.getString(MediaMetadataCompat.METADATA_KEY_DATE))

    private fun subList(list: List<MediaMetadataCompat>, days: Int): List<MediaMetadataCompat> =
            list.indices
                    .firstOrNull {
                        TimeHelper.toDateTime(list[it].getString(MediaMetadataCompat.METADATA_KEY_DATE))
                                .isBefore(TimeHelper.japanNow.minusDays(days))
                    }?.let { if (it == 0) ArrayList() else list.subList(0, it) } ?: list
}
