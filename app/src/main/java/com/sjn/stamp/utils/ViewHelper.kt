package com.sjn.stamp.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import com.sjn.stamp.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target


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

    fun tintMenuIcon(item: MenuItem, color: Int) {
        val normalDrawable = item.icon
        val wrapDrawable = DrawableCompat.wrap(normalDrawable)
        wrapDrawable?.let {
            val mutableDrawable = it.mutate()
            mutableDrawable?.let {
                DrawableCompat.setTint(it, color)
                item.icon = it
            }
        }
    }

    fun tintMenuIconByTheme(context: Context, item: MenuItem) {
        tintMenuIcon(item, ViewHelper.getThemeColor(context, android.R.attr.textColorPrimary, Color.DKGRAY))
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

    fun readBitmapAsync(context: Context, url: String, target: Target) {
        Handler(Looper.getMainLooper()).post { Picasso.with(context).load(url).into(target) }
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