package com.sjn.stamp.ui.item.holder

import android.view.View
import android.widget.TextView
import com.sjn.stamp.R
import eu.davidea.flexibleadapter.FlexibleAdapter


class HeaderViewHolder(view: View, adapter: FlexibleAdapter<*>) : LongClickDisableViewHolder(view, adapter, true) {
    var title: TextView = view.findViewById(R.id.title)
    var subtitle: TextView = view.findViewById(R.id.subtitle)
    init{
        view.background.alpha = 180
    }
}