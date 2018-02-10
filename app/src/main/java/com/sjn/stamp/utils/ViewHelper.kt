package com.sjn.stamp.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import com.sjn.stamp.R
import com.sjn.stamp.ui.custom.TextDrawable
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.util.*

@Suppress("unused")
object ViewHelper {
    // Resolution reasonable for carrying around as an icon (generally in
    // MediaDescription.getIconBitmap). This should not be bigger than necessary, because
    // the MediaDescription object should be lightweight. If you set it too high and try to
    // serialize the MediaDescription, you may get FAILED BINDER TRANSACTION errors.
    private const val MAX_ART_WIDTH_ICON = 128  // pixels
    private const val MAX_ART_HEIGHT_ICON = 128  // pixels

    private var colorAccent = -1

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun getColorAccent(context: Context): Int {
        if (colorAccent < 0) {
            val accentAttr = if (CompatibleHelper.hasLollipop()) android.R.attr.colorAccent else R.attr.colorAccent
            val androidAttr = context.theme.obtainStyledAttributes(intArrayOf(accentAttr))
            colorAccent = androidAttr.getColor(0, -0xff6978) //Default: material_deep_teal_500
            androidAttr.recycle()
        }
        return colorAccent
    }

    fun dpToPx(context: Context, dp: Float): Float = Math.round(dp * getDisplayMetrics(context).density).toFloat()

    private fun getDisplayMetrics(context: Context): DisplayMetrics = context.resources.displayMetrics

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

    private fun getRankingColorResourceId(position: Int): Int = when (position) {
        0 -> R.color.color_1
        1 -> R.color.color_2
        2 -> R.color.color_3
        3 -> R.color.color_4
        4 -> R.color.color_5
        5 -> R.color.color_6
        6 -> R.color.color_7
        else -> R.color.colorPrimaryDark
    }

    fun updateAlbumArtIfEnable(activity: Activity?, view: ImageView?, artUrl: String?, text: String?, targetWidth: Int = 128, targetHeight: Int = 128) {
        activity?.let { _activity ->
            view?.let { view ->
                artUrl?.let { artUrl ->
                    text?.let { text ->
                        ViewHelper.updateAlbumArt(_activity, view, artUrl, text, targetWidth, targetHeight)
                    }
                }
            }
        }
    }

    @JvmOverloads
    fun updateAlbumArt(activity: Activity, view: ImageView, artUrl: String, text: String, targetWidth: Int = 128, targetHeight: Int = 128) {
        view.tag = artUrl
        if (artUrl.isEmpty()) {
            //view.setImageDrawable(ContextCompat.getDrawable(activity, R.mipmap.ic_launcher));
            view.setImageDrawable(createTextDrawable(text))
            return
        }
        Picasso.with(activity).load(artUrl).placeholder(createTextDrawable(text)).resize(targetWidth, targetHeight).into(view, object : Callback {
            override fun onSuccess() {
                if (view.tag != null && artUrl != view.tag) {
                    updateAlbumArt(activity, view, view.tag as String, text)
                }
            }

            override fun onError() {
                view.setImageDrawable(createTextDrawable(text))
                //view.setImageDrawable(ContextCompat.getDrawable(activity, R.mipmap.ic_launcher));
            }
        })
    }

    fun setDrawableLayerColor(activity: Activity, view: View, color: Int, drawableId: Int, layerId: Int) {
        (ContextCompat.getDrawable(activity, drawableId) as LayerDrawable?)?.let {
            it.findDrawableByLayerId(layerId).mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
            view.background = it
        }
    }

    fun createTextDrawable(text: CharSequence?): TextDrawable =
            if (text == null) createTextDrawable("") else createTextDrawable(text.toString())

    fun createTextDrawable(text: String): TextDrawable = TextDrawable.builder()
            .beginConfig()
            .useFont(Typeface.DEFAULT)
            .bold()
            .toUpperCase()
            .endConfig()
            .rect()
            .build(if (text.isEmpty()) "" else text[0].toString(), ColorGenerator.MATERIAL.getColor(text))

    fun toBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas()
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

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

    fun createIcon(bitmap: Bitmap): Bitmap = scaleBitmap(bitmap, MAX_ART_WIDTH_ICON, MAX_ART_HEIGHT_ICON)

    private fun scaleBitmap(src: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val scaleFactor = Math.min(maxWidth.toDouble() / src.width, maxHeight.toDouble() / src.height)
        return Bitmap.createScaledBitmap(src, (src.width * scaleFactor).toInt(), (src.height * scaleFactor).toInt(), false)
    }

    private class ColorGenerator private constructor(private val colors: List<Int>) {
        private val random: Random = Random(System.currentTimeMillis())

        val randomColor: Int
            get() = colors[random.nextInt(colors.size)]

        internal fun getColor(key: Any?): Int = if (key == null) colors[0] else colors[Math.abs(key.hashCode()) % colors.size]

        companion object {

            internal var DEFAULT: ColorGenerator

            internal var MATERIAL: ColorGenerator

            init {
                DEFAULT = create(Arrays.asList(
                        -0xe9c9c,
                        -0xa7aa7,
                        -0x65bc2,
                        -0x1b39d2,
                        -0x98408c,
                        -0xa65d42,
                        -0xdf6c33,
                        -0x529d59,
                        -0x7fa87f
                ))
                MATERIAL = create(Arrays.asList(
                        -0x1a8c8d,
                        -0xf9d6e,
                        -0x459738,
                        -0x6a8a33,
                        -0x867935,
                        -0x9b4a0a,
                        -0xb03c09,
                        -0xb22f1f,
                        -0xb24954,
                        -0x7e387c,
                        -0x512a7f,
                        -0x759b,
                        -0x2b1ea9,
                        -0x2ab1,
                        -0x48b3,
                        -0x5e7781,
                        -0x6f5b52
                ))
            }

            fun create(colorList: List<Int>): ColorGenerator {
                return ColorGenerator(colorList)
            }
        }
    }

}
