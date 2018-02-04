package com.sjn.stamp.ui.item.holder

import android.animation.Animator
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.sjn.stamp.R
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.helpers.AnimatorHelper


class ProgressViewHolder(view: View, adapter: FlexibleAdapter<*>) : LongClickDisableViewHolder(view, adapter) {
    var progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
    var progressMessage: TextView = view.findViewById(R.id.progress_message)

    override fun scrollAnimators(animators: List<Animator>, position: Int, isForward: Boolean) {
        AnimatorHelper.scaleAnimator(animators, itemView, 0f)
    }
}