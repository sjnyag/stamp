package com.sjn.stamp.ui.item.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sjn.stamp.R
import com.sjn.stamp.utils.ViewHelper
import eu.davidea.flexibleadapter.FlexibleAdapter


class RankedViewHolder(view: View, adapter: FlexibleAdapter<*>) : LongClickDisableViewHolder(view, adapter) {

    var albumArtView: ImageView = view.findViewById(R.id.image)
    var title: TextView = view.findViewById(R.id.title)
    var subtitle: TextView = view.findViewById(R.id.subtitle)
    var countView: TextView = view.findViewById(R.id.count)
    var orderView: TextView = view.findViewById(R.id.order)
    private var _frontView: View = view.findViewById(R.id.front_view)

    init {
        albumArtView.setOnClickListener {
            mAdapter.mItemLongClickListener?.let {
                it.onItemLongClick(adapterPosition)
                toggleActivation()
            }
        }
    }

    override fun getActivationElevation(): Float = ViewHelper.dpToPx(itemView.context, 4f)

    override fun getFrontView(): View = _frontView
}
