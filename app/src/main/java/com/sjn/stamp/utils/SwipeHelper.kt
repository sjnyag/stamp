package com.sjn.stamp.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback
import eu.davidea.viewholders.FlexibleViewHolder

object SwipeHelper {
    fun cancel(recyclerView: RecyclerView?, position: Int) {
        val holder = recyclerView?.findViewHolderForLayoutPosition(position)
        if (holder is ItemTouchHelperCallback.ViewHolderCallback) {
            val view = (holder as ItemTouchHelperCallback.ViewHolderCallback).frontView
            ObjectAnimator.ofFloat(view, "translationX", view.translationX, 0F).apply {
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}

                    override fun onAnimationEnd(animator: Animator) {
                        view.translationX = 0f
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        view.translationX = 0f
                    }

                    override fun onAnimationRepeat(animator: Animator) {}
                })
            }.start()
            (holder as? FlexibleViewHolder)?.onActionStateChanged(position, ItemTouchHelper.ACTION_STATE_IDLE)
        }
        return
    }

}
