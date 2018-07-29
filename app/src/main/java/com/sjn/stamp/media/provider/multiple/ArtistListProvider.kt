package com.sjn.stamp.media.provider.multiple

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.R
import com.sjn.stamp.utils.MediaIDHelper

class ArtistListProvider(context: Context) : MultipleListProvider(context) {

    override val mediaKey: String = MediaMetadataCompat.METADATA_KEY_ARTIST

    override val providerMediaId: String = MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST

    override val titleId: Int = R.string.media_item_label_artist

    override fun compareMediaList(lhs: MediaMetadataCompat, rhs: MediaMetadataCompat): Int =
            (lhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                    ?: "").compareTo(rhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: "")

}
