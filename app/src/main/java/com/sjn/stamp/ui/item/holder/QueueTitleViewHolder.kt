package com.sjn.stamp.ui.item.holder

import android.animation.Animator
import android.view.View
import android.widget.TextView
import com.sjn.stamp.R
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.helpers.AnimatorHelper


class QueueTitleViewHolder(view: View, adapter: FlexibleAdapter<*>) : LongClickDisableViewHolder(view, adapter, true) {

    var title: TextView = view.findViewById(R.id.title)
    var subtitle: TextView = view.findViewById(R.id.subtitle)

    init{
        view.background.alpha = 180
    }

    override fun scrollAnimators(animators: List<Animator>, position: Int, isForward: Boolean) {
        AnimatorHelper.slideInFromTopAnimator(animators, itemView, mAdapter.recyclerView)
    }
}