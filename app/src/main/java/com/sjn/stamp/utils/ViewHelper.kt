package com.sjn.stamp.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
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
        var themeColor = 0
        val packageName = context.packageName
        try {
            val packageContext = context.createPackageContext(packageName, 0)
            val applicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
            packageContext.setTheme(applicationInfo.theme)
            val theme = packageContext.theme
            val ta = theme.obtainStyledAttributes(intArrayOf(attribute))
            themeColor = ta.getColor(0, defaultColor)
            ta.recycle()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return themeColor
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
