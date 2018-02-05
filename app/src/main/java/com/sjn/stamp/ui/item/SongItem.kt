package com.sjn.stamp.ui.item

import android.app.Activity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.View
import com.sjn.stamp.R
import com.sjn.stamp.ui.MediaBrowsable
import com.sjn.stamp.ui.item.holder.SongViewHolder
import com.sjn.stamp.utils.ViewHelper
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
        val context = holder.itemView.context
        if (adapter.hasSearchText()) {
            FlexibleUtils.highlightText(holder.title, title, adapter.searchText)
            FlexibleUtils.highlightText(holder.subtitle, subtitle, adapter.searchText)
        } else {
            holder.title.text = title
            holder.subtitle.text = subtitle
        }

        holder.mediaId = mediaId
        if (albumArt.isNotEmpty()) {
            ViewHelper.updateAlbumArt(context as Activity, holder.albumArtView, albumArt, title)
        } else {
            mediaBrowsable.search(mediaId, null, object : MediaBrowserCompat.SearchCallback() {
                override fun onSearchResult(query: String, extras: Bundle?, items: List<MediaBrowserCompat.MediaItem>) {
                    for (metadata in items) {
                        if (metadata.description.iconUri == null) {
                            continue
                        }
                        if (query == holder.mediaId && metadata.description.iconUri != null) {
                            ViewHelper.updateAlbumArt(context as Activity, holder.albumArtView, metadata.description.iconUri.toString(), title)
                        }
                        break
                    }
                }

            })
        }
        holder.update(holder.imageView, mediaId, isPlayable)
        holder.updateStampList(mediaId)
    }

    override fun filter(constraint: String): Boolean =
            title.toLowerCase().trim { it <= ' ' }.contains(constraint) || subtitle.toLowerCase().trim { it <= ' ' }.contains(constraint)

}