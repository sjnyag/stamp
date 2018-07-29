package com.sjn.stamp.ui.item

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.view.View
import com.sjn.stamp.R
import com.sjn.stamp.controller.SongHistoryController
import com.sjn.stamp.model.SongHistory
import com.sjn.stamp.ui.item.holder.SongHistoryViewHolder
import com.sjn.stamp.utils.AlbumArtHelper
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.TimeHelper
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFilterable
import eu.davidea.flexibleadapter.items.ISectionable
import eu.davidea.flexibleadapter.utils.FlexibleUtils
import java.io.Serializable

class SongHistoryItem constructor(songHistory: SongHistory, private var header: DateHeaderItem?, resources: Resources, private val activity: Activity) : AbstractItem<SongHistoryViewHolder>(songHistory.id.toString()), ISectionable<SongHistoryViewHolder, DateHeaderItem>, IFilterable, Serializable {

    val mediaId = MediaIDHelper.createMediaID(songHistory.song.mediaId, MediaIDHelper.MEDIA_ID_MUSICS_BY_TIMELINE)
    override val title = songHistory.song.title
    override val subtitle = songHistory.song.artist.name
    private val recordedAt = songHistory.recordedAt
    private val albumArtUri = songHistory.song.albumArtUri
    private val label = songHistory.toLabel(resources)
    private val songHistoryId = songHistory.id

    init {
        isDraggable = true
        isSwipeable = true
    }

    override fun delete(context: Context) {
        SongHistoryController(context).delete(songHistoryId)
    }

    override fun getHeader(): DateHeaderItem? = header

    override fun setHeader(header: DateHeaderItem) {
        this.header = header
    }

    override fun getLayoutRes(): Int = R.layout.recycler_song_history_item

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<*>): SongHistoryViewHolder = SongHistoryViewHolder(view, adapter, activity)

    override fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: SongHistoryViewHolder, position: Int, payloads: List<*>) {
        val context = holder.itemView.context
        if (adapter.hasSearchText()) {
            FlexibleUtils.highlightText(holder.title, title, adapter.searchText)
            FlexibleUtils.highlightText(holder.subtitle, subtitle, adapter.searchText)
        } else {
            holder.title.text = title
            holder.subtitle.text = subtitle
        }
        holder.date.text = TimeHelper.getDateText(recordedAt, context.resources)
        if (albumArtUri.isNotEmpty()) AlbumArtHelper.update(context, holder.albumArtView, albumArtUri, title)
        holder.updateStampList(mediaId)
    }

    override fun filter(constraint: String): Boolean =
            title.toLowerCase().trim { it <= ' ' }.contains(constraint) || subtitle.toLowerCase().trim { it <= ' ' }.contains(constraint)

    override fun toString(): String = label

}