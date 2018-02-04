package com.sjn.stamp.ui.item.holder

import android.animation.Animator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View

import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.helpers.AnimatorHelper
import eu.davidea.viewholders.FlexibleViewHolder


abstract class LongClickDisableViewHolder : FlexibleViewHolder {

    constructor(view: View, adapter: FlexibleAdapter<*>) : super(view, adapter)

    constructor(view: View, adapter: FlexibleAdapter<*>, stickyHeader: Boolean) : super(view, adapter, stickyHeader)

    override fun onLongClick(view: View?): Boolean {
        val oldL = mAdapter.mItemLongClickListener
        mAdapter.mItemLongClickListener = null
        super.onLongClick(view)
        mAdapter.mItemLongClickListener = oldL
        return false
    }

    override fun scrollAnimators(animators: List<Animator>, position: Int, isForward: Boolean) {
        if (mAdapter.recyclerView.layoutManager is GridLayoutManager || mAdapter.recyclerView.layoutManager is StaggeredGridLayoutManager) {
            if (position % 2 != 0)
                AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.recyclerView, 0.5f)
            else
                AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.recyclerView, 0.5f)
        } else {
            //Linear layout
            if (mAdapter.isSelected(position))
                AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.recyclerView, 0.5f)
            else
                AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.recyclerView, 0.5f)
        }
    }
}
