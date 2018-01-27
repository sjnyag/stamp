package com.sjn.stamp.media.provider.single

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.google.common.collect.Lists
import com.sjn.stamp.R
import com.sjn.stamp.utils.MediaIDHelper
import java.util.*

class QueueProvider(context: Context) : SingleListProvider(context) {

    var queueListener: QueueListener? = null

    override val providerMediaId: String = MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE

    override val titleId: Int = R.string.media_item_label_queue

    interface QueueListener {
        val playingQueueMetadata: Iterable<MediaMetadataCompat>
    }

    override fun createTrackList(musicListById: Map<String, MediaMetadataCompat>): List<MediaMetadataCompat> {
        queueListener?.let {
            return Lists.newArrayList(it.playingQueueMetadata)
        }
        return ArrayList()
    }
}
