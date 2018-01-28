package com.sjn.stamp.media.provider.single

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.R
import com.sjn.stamp.utils.MediaIDHelper
import java.util.*

open class AllProvider(context: Context) : SingleListProvider(context) {

    override val providerMediaId: String = MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL

    override val titleId: Int = R.string.media_item_label_all_song

    override fun createTrackList(musicListById: Map<String, MediaMetadataCompat>): List<MediaMetadataCompat> {
        return ArrayList(musicListById.values).apply {
            Collections.sort(this) { lhs, rhs -> compareMediaList(lhs, rhs) }
        }
    }

    protected open fun compareMediaList(lhs: MediaMetadataCompat, rhs: MediaMetadataCompat): Int =
            lhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE).compareTo(rhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
}