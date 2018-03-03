package com.sjn.stamp.ui.item.holder

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sjn.stamp.R
import com.sjn.stamp.utils.ViewHelper
import eu.davidea.flexibleadapter.FlexibleAdapter

class SongHistoryViewHolder(view: View, adapter: FlexibleAdapter<*>, activity: Activity) : StampContainsViewHolder(view, adapter, activity) {

    var albumArtView: ImageView = view.findViewById(R.id.image)
    var title: TextView = view.findViewById(R.id.title)
    var subtitle: TextView = view.findViewById(R.id.subtitle)
    var date: TextView = view.findViewById(R.id.date)

    private var _frontView: View = view.findViewById(R.id.front_view)

    override fun getActivationElevation(): Float = ViewHelper.dpToPx(itemView.context, 4f)

    override fun getFrontView(): View = _frontView

    override fun isStampMedia(mediaId: String): Boolean = true
}