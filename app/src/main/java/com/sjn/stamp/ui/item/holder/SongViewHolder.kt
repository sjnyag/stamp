package com.sjn.stamp.ui.item.holder

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sjn.stamp.R
import com.sjn.stamp.utils.SongStateHelper
import com.sjn.stamp.utils.ViewHelper
import eu.davidea.flexibleadapter.FlexibleAdapter

class SongViewHolder constructor(view: View, adapter: FlexibleAdapter<*>, activity: Activity) : StampContainsViewHolder(view, adapter, activity) {

    internal var mMediaId: String? = null
    internal var albumArtView: ImageView = view.findViewById(R.id.image)
    internal var title: TextView = view.findViewById(R.id.title)
    internal var subtitle: TextView = view.findViewById(R.id.subtitle)
    internal var date: TextView = view.findViewById(R.id.date)
    internal var imageView: ImageView = view.findViewById(R.id.play_eq)
    private var _frontView: View = view.findViewById(R.id.front_view)

    init {
        this.imageView.setOnClickListener {
            mAdapter.mItemLongClickListener?.onItemLongClick(adapterPosition)
        }
    }

    override fun getActivationElevation(): Float = ViewHelper.dpToPx(itemView.context, 4f)

    override fun getFrontView(): View = _frontView

    fun update(view: View, mediaId: String, isPlayable: Boolean) {
        val cachedState = view.getTag(R.id.tag_mediaitem_state_cache) as Int?
        val state = SongStateHelper.getMediaItemState(this.activity, mediaId, isPlayable)
        if (cachedState == null || cachedState != state) {
            val drawable = SongStateHelper.getDrawableByState(this.activity, state)
            if (drawable != null) {
                this.imageView.setImageDrawable(drawable)
                this.imageView.visibility = View.VISIBLE
            } else {
                this.imageView.visibility = View.GONE
            }
            view.setTag(R.id.tag_mediaitem_state_cache, state)
        }
    }
}