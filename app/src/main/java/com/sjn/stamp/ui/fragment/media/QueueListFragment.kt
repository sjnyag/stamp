package com.sjn.stamp.ui.fragment.media

import android.support.v4.media.MediaBrowserCompat
import com.sjn.stamp.R
import com.sjn.stamp.ui.item.QueueTitleItem
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.MediaItemHelper

class QueueListFragment : SongListFragment() {
    init {
        mediaId = MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE
    }

    override fun emptyMessage(): String {
        return getString(R.string.empty_message_queue)
    }

    override fun onMediaBrowserChildrenLoaded(parentId: String,
                                              children: List<MediaBrowserCompat.MediaItem>) {

        if (!children.isEmpty()) {
            children[0].description.extras?.getString(MediaItemHelper.META_DATA_KEY_BASE_MEDIA_ID)?.let { baseMediaId ->
                MediaIDHelper.getProviderType(baseMediaId)?.let {
                    adapter?.showLayoutInfo(QueueTitleItem(it, MediaIDHelper.extractBrowseCategoryValueFromMediaID(baseMediaId)), true)
                }
            }
        }
        super.onMediaBrowserChildrenLoaded(parentId, children)
    }

}