package com.sjn.stamp.ui.item

import android.app.Activity
import android.support.v4.media.MediaBrowserCompat
import android.view.View
import com.sjn.stamp.R
import com.sjn.stamp.ui.MediaBrowsable
import com.sjn.stamp.ui.item.holder.SongViewHolder
import com.sjn.stamp.utils.AlbumArtHelper
import com.sjn.stamp.utils.CompatibleHelper
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFilterable
import eu.davidea.flexibleadapter.utils.FlexibleUtils
import java.io.Serializable

class SongItem(
        mediaItem: MediaBrowserCompat.MediaItem,
        private val mediaBrowsable: MediaBrowsable,
        private val activity: Activity)
    : AbstractItem<SongViewHolder>(mediaItem.mediaId ?: ""), IFilterable, Serializable {

    val mediaId = mediaItem.mediaId ?: ""
    override val title = mediaItem.description.title?.toString() ?: ""
    override val subtitle = mediaItem.description.subtitle?.toString() ?: ""
    private val albumArt = mediaItem.description.iconUri?.toString() ?: ""
    val isPlayable = mediaItem.isPlayable
    val isBrowsable = mediaItem.isBrowsable

    init {
        isDraggable = true
        isSwipeable = true
    }

    override fun getLayoutRes(): Int = R.layout.recycler_song_item

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<*>): SongViewHolder = SongViewHolder(view, adapter, activity)

    override fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: SongViewHolder, position: Int, payloads: List<*>) {
        if (adapter.hasSearchText()) {
            FlexibleUtils.highlightText(holder.title, title, adapter.searchText)
            FlexibleUtils.highlightText(holder.subtitle, subtitle, adapter.searchText)
        } else {
            holder.title.text = title
            holder.subtitle.text = subtitle
        }

        holder.mediaId = mediaId
        if (albumArt.isNotEmpty()) {
            AlbumArtHelper.update(activity, holder.albumArtView, albumArt, title)
        } else {
            AlbumArtHelper.searchAndUpdate(activity, holder.albumArtView, title, mediaId, mediaBrowsable)
        }
        holder.update(holder.imageView, mediaId, isPlayable)
        holder.updateStampList(mediaId)

        if (CompatibleHelper.hasLollipop()) {
            holder.title.transitionName = "trans_text_$mediaId$position"
            holder.albumArtView.transitionName = "trans_image_$mediaId$position"
        }
    }

    override fun filter(constraint: String): Boolean =
            title.toLowerCase().trim { it <= ' ' }.contains(constraint) || subtitle.toLowerCase().trim { it <= ' ' }.contains(constraint)

}