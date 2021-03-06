package com.sjn.stamp.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.sjn.stamp.R
import com.sjn.stamp.utils.ViewHelper.dpToPx
import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback
import eu.davidea.viewholders.FlexibleViewHolder


@Suppress("unused")
object ViewHelper {

    fun dpToPx(context: Context, dp: Float): Float = Math.round(dp * getDisplayMetrics(context).density).toFloat()

    fun setFragmentTitle(activity: Activity?, title: String) {
        if (activity == null || activity !is AppCompatActivity) {
            return
        }
        activity.supportActionBar?.let {
            it.title = title
        }
    }

    fun setFragmentTitle(activity: Activity?, title: Int) {
        activity?.let {
            setFragmentTitle(it, it.resources.getString(title))
        }
    }

    fun getRankingColor(context: Context, position: Int): Int = ContextCompat.getColor(context, getRankingColorResourceId(position))

    /**
     * Get a color value from a theme attribute.
     *
     * @param context      used for getting the color.
     * @param attribute    theme attribute.
     * @param defaultColor default to use.
     * @return color value
     */
    fun getThemeColor(context: Context, attribute: Int, defaultColor: Int): Int {
        val outValue = TypedValue()
        val theme = context.theme
        val wasResolved = theme.resolveAttribute(attribute, outValue, true)
        if (!wasResolved) {
            return defaultColor
        }
        return ContextCompat.getColor(context, outValue.resourceId)
    }

    private fun getDisplayMetrics(context: Context): DisplayMetrics = context.resources.displayMetrics

    private fun getRankingColorResourceId(position: Int): Int = when (position) {
        0 -> R.color.color_1
        1 -> R.color.color_2
        2 -> R.color.color_3
        3 -> R.color.color_4
        4 -> R.color.color_5
        5 -> R.color.color_6
        6 -> R.color.color_7
        else -> R.color.md_black_1000
    }

}

inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}

inline fun Context.runOnUiThread(crossinline f: () -> Unit) {
    if (this is Activity) {
        this.runOnUiThread {
            f()
        }
    } else {
        Handler(Looper.getMainLooper()).post {
            f()
        }
    }
}

fun MenuItem.tintByTheme(context: Context) {
    this.icon?.let {
        DrawableCompat.setTint(it, ViewHelper.getThemeColor(context, android.R.attr.textColorPrimary, Color.DKGRAY))
    }
}

fun RecyclerView.cancelSwipe(position: Int) {
    val holder = this.findViewHolderForLayoutPosition(position)
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
}

fun CollapsingToolbarLayout.setHeight(context: Context, dp: Float) {
    this.layoutParams = this.layoutParams.apply { height = dpToPx(context, dp).toInt() }
}

fun CollapsingToolbarLayout.setHeightWrapContent() {
    this.layoutParams = this.layoutParams.apply { height = ViewGroup.LayoutParams.WRAP_CONTENT }
}

fun RecyclerView.findFirstVisibleItemPosition(): Int {
    return if (this.layoutManager is LinearLayoutManager) {
        val layoutManager = this.layoutManager as LinearLayoutManager
        layoutManager.findFirstVisibleItemPosition()
    } else if (this.layoutManager is StaggeredGridLayoutManager) {
        val layoutManager = this.layoutManager as StaggeredGridLayoutManager
        var firstVisibleItems: IntArray? = null
        firstVisibleItems = layoutManager.findFirstCompletelyVisibleItemPositions(firstVisibleItems)
        if (firstVisibleItems?.isNotEmpty() == true) {
            firstVisibleItems[0]
        } else {
            RecyclerView.NO_POSITION
        }
    } else {
        RecyclerView.NO_POSITION
    }
}

fun RecyclerView.findFirstVisibleViewHolder(): RecyclerView.ViewHolder? =
        this.findFirstVisibleItemPosition().let {
            if (it != RecyclerView.NO_POSITION) {
                this.findViewHolderForAdapterPosition(it)
            } else {
                null
            }
        }