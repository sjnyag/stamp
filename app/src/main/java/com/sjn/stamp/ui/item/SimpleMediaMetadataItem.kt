package com.sjn.stamp.ui.item

import android.app.Activity
import android.support.v4.media.MediaMetadataCompat
import android.view.View
import com.sjn.stamp.R
import com.sjn.stamp.ui.item.holder.SimpleMediaViewHolder
import com.sjn.stamp.utils.AlbumArtHelper
import com.sjn.stamp.utils.MediaItemHelper
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFilterable
import eu.davidea.flexibleadapter.utils.FlexibleUtils
import java.io.Serializable

class SimpleMediaMetadataItem(metadata: MediaMetadataCompat)
    : AbstractItem<SimpleMediaViewHolder>(metadata.description.mediaId
        ?: ""), IFilterable, Serializable {

    val mediaId = metadata.description.mediaId ?: ""
    override val title = MediaItemHelper.getTitle(metadata) ?: ""
    override val subtitle = MediaItemHelper.getArtist(metadata) ?: ""
    private val albumArt = MediaItemHelper.getAlbumArtUri(metadata) ?: ""

    init {
        isDraggable = true
        isSwipeable = true
    }

    override fun getLayoutRes(): Int = R.layout.recycler_simple_media_metadata_item

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<*>): SimpleMediaViewHolder = SimpleMediaViewHolder(view, adapter)

    override fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: SimpleMediaViewHolder, position: Int, payloads: List<*>) {
        val context = holder.itemView.context
        if (adapter.hasSearchText()) {
            FlexibleUtils.highlightText(holder.title, title, adapter.searchText)
            FlexibleUtils.highlightText(holder.subtitle, subtitle, adapter.searchText)
        } else {
            holder.title.text = title
            holder.subtitle.text = subtitle
        }
        if (albumArt.isNotEmpty()) AlbumArtHelper.update(context as Activity, holder.albumArtView, albumArt, title)
    }

    override fun filter(constraint: String): Boolean {
        return title.toLowerCase().trim { it <= ' ' }.contains(constraint) || subtitle.toLowerCase().trim { it <= ' ' }.contains(constraint)
    }
}