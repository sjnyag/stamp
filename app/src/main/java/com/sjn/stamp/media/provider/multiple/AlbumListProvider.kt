package com.sjn.stamp.media.provider.multiple

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.R
import com.sjn.stamp.utils.MediaIDHelper

class AlbumListProvider(context: Context) : MultipleListProvider(context) {

    override val mediaKey: String = MediaMetadataCompat.METADATA_KEY_ALBUM

    override val providerMediaId: String = MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM

    override val titleId: Int = R.string.media_item_label_album

    override fun compareMediaList(lhs: MediaMetadataCompat, rhs: MediaMetadataCompat): Int =
            when {
                lhs.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER) < rhs.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER) -> -1
                lhs.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER) == rhs.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER) -> 0
                else -> 1
            }

}
