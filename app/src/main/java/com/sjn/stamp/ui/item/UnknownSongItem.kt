package com.sjn.stamp.ui.item

import android.app.Activity
import android.view.View
import com.sjn.stamp.R
import com.sjn.stamp.model.Song
import com.sjn.stamp.ui.item.holder.UnknownSongViewHolder
import com.sjn.stamp.utils.TimeHelper
import com.sjn.stamp.utils.ViewHelper
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFilterable
import eu.davidea.flexibleadapter.utils.FlexibleUtils
import java.io.Serializable

class UnknownSongItem(id: String, song: Song) : AbstractItem<UnknownSongViewHolder>(id), IFilterable, Serializable {

    val songId = song.id
    private val lastPlayed = if (song.songHistoryList != null && !song.songHistoryList.isEmpty()) song.songHistoryList.last().recordedAt else null
    private val mediaId = song.mediaId
    override val title = song.title
    override val subtitle = song.album
    private val albumArt = song.albumArtUri

    init {
        isDraggable = true
        isSwipeable = true
    }

    override fun getLayoutRes(): Int = R.layout.recycler_unknown_song_item

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<*>): UnknownSongViewHolder =
            UnknownSongViewHolder(view, adapter)

    override fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: UnknownSongViewHolder, position: Int, payloads: List<*>) {
        val context = holder.itemView.context
        if (adapter.hasSearchText()) {
            FlexibleUtils.highlightText(holder.title, title, adapter.searchText)
            FlexibleUtils.highlightText(holder.subtitle, subtitle, adapter.searchText)
        } else {
            holder.title.text = title
            holder.subtitle.text = subtitle
            holder.date.text = TimeHelper.getDateText(lastPlayed!!, context.resources)
        }
        holder.mediaId = mediaId
        if (albumArt.isNotEmpty()) ViewHelper.updateAlbumArt(context as Activity, holder.albumArtView, albumArt, title)

    }

    override fun filter(constraint: String): Boolean = title.toLowerCase().trim { it <= ' ' }.contains(constraint) || subtitle.toLowerCase().trim { it <= ' ' }.contains(constraint)

}