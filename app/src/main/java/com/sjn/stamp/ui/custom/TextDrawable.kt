package com.sjn.stamp.ui.custom

import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import com.sjn.stamp.utils.LogHelper
import java.util.*

class TextDrawable private constructor(builder: TextDrawable.Builder) : ShapeDrawable(builder.shape) {

    private val textPaint: Paint
    private val borderPaint: Paint
    private val text: String?
    private val color: Int
    private val shape: RectShape?
    private val height: Int
    private val width: Int
    private val fontSize: Float
    private val radius: Float
    private val borderThickness: Int

    init {

        // shape properties
        shape = builder.shape
        height = builder.height
        width = builder.width
        radius = builder.radius

        // text and color
        text = if (builder.toUpperCase) builder.text!!.toUpperCase(Locale.getDefault()) else builder.text
        color = builder.color

        // text paint settings
        fontSize = builder.fontSize
        textPaint = Paint()
        textPaint.color = builder.textColor
        textPaint.isAntiAlias = true
        textPaint.isFakeBoldText = builder.isBold
        textPaint.style = Paint.Style.FILL
        textPaint.typeface = builder.font
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.strokeWidth = builder.borderThickness.toFloat()

        // border paint settings
        borderThickness = builder.borderThickness
        borderPaint = Paint()
        borderPaint.color = getDarkerShade(color)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = borderThickness.toFloat()

        // drawable paint color
        val paint = paint
        paint.color = color

    }

    private fun getDarkerShade(color: Int): Int {
        return Color.rgb((SHADE_FACTOR * Color.red(color)).toInt(),
                (SHADE_FACTOR * Color.green(color)).toInt(),
                (SHADE_FACTOR * Color.blue(color)).toInt())
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val r = bounds

        // draw border
        if (borderThickness > 0) {
            drawBorder(canvas)
        }

        val count = canvas.save()
        canvas.rotate(20f)
        canvas.translate(10f, -10f)

        val width = if (this.width < 0) r.width() else this.width
        val height = if (this.height < 0) r.height() else this.height
        val fontSize = if (this.fontSize < 0) (Math.min(width, height) * 1.5).toFloat() else this.fontSize
        textPaint.textSize = fontSize
        text?.let {
            canvas.drawText(it, width / 2F, height / 2F - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)
        }
        canvas.restoreToCount(count)

    }

    private fun drawBorder(canvas: Canvas) {
        val rect = RectF(bounds)
        rect.inset((borderThickness / 2).toFloat(), (borderThickness / 2).toFloat())

        when (shape) {
            is OvalShape -> canvas.drawOval(rect, borderPaint)
            is RoundRectShape -> canvas.drawRoundRect(rect, radius, radius, borderPaint)
            else -> canvas.drawRect(rect, borderPaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        textPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        textPaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicWidth(): Int {
        return width
    }

    override fun getIntrinsicHeight(): Int {
        return height
    }

    class Builder : TextDrawable.IConfigBuilder, TextDrawable.IShapeBuilder, TextDrawable.IBuilder {

        var text: String? = null
        var color: Int = 0
        var borderThickness: Int = 0
        var width: Int = 0
        var height: Int = 0
        var font: Typeface? = null
        var shape: RectShape? = null
        var textColor: Int = 0
        var fontSize: Float = 0F
        var isBold: Boolean = false
        var toUpperCase: Boolean = false

        var radius: Float = 0.toFloat()

        init {
            text = ""
            color = Color.GRAY
            textColor = Color.parseColor("#55FFFFFF")
            borderThickness = 0
            width = -1
            height = -1
            shape = RectShape()
            font = Typeface.create("sans-serif-light", Typeface.NORMAL)
            fontSize = -1F
            isBold = false
            toUpperCase = false
        }

        override fun width(width: Int): TextDrawable.IConfigBuilder {
            this.width = width
            return this
        }

        override fun height(height: Int): TextDrawable.IConfigBuilder {
            this.height = height
            return this
        }

        override fun textColor(color: Int): TextDrawable.IConfigBuilder {
            this.textColor = color
            return this
        }

        override fun withBorder(thickness: Int): TextDrawable.IConfigBuilder {
            this.borderThickness = thickness
            return this
        }

        override fun useFont(font: Typeface): TextDrawable.IConfigBuilder {
            this.font = font
            return this
        }

        override fun fontSize(size: Float): TextDrawable.IConfigBuilder {
            this.fontSize = size
            return this
        }

        override fun bold(): TextDrawable.IConfigBuilder {
            this.isBold = true
            return this
        }

        override fun toUpperCase(): TextDrawable.IConfigBuilder {
            this.toUpperCase = true
            return this
        }

        override fun beginConfig(): TextDrawable.IConfigBuilder {
            return this
        }

        override fun endConfig(): TextDrawable.IShapeBuilder {
            return this
        }

        override fun rect(): TextDrawable.IBuilder {
            this.shape = RectShape()
            return this
        }

        override fun round(): TextDrawable.IBuilder {
            this.shape = OvalShape()
            return this
        }

        override fun roundRect(radius: Int): TextDrawable.IBuilder {
            this.radius = radius.toFloat()
            val radii = floatArrayOf(radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat())
            this.shape = RoundRectShape(radii, null, null)
            return this
        }

        override fun buildRect(text: String, color: Int): TextDrawable {
            rect()
            return build(text, color)
        }

        override fun buildRoundRect(text: String, color: Int, radius: Int): TextDrawable {
            roundRect(radius)
            return build(text, color)
        }

        override fun buildRound(text: String, color: Int): TextDrawable {
            round()
            return build(text, color)
        }

        override fun build(text: String, color: Int): TextDrawable {
            this.color = color
            this.text = text
            return TextDrawable(this)
        }
    }

    interface IConfigBuilder {
        fun width(width: Int): TextDrawable.IConfigBuilder

        fun height(height: Int): TextDrawable.IConfigBuilder

        fun textColor(color: Int): TextDrawable.IConfigBuilder

        fun withBorder(thickness: Int): TextDrawable.IConfigBuilder

        fun useFont(font: Typeface): TextDrawable.IConfigBuilder

        fun fontSize(size: Float): TextDrawable.IConfigBuilder

        fun bold(): TextDrawable.IConfigBuilder

        fun toUpperCase(): TextDrawable.IConfigBuilder

        fun endConfig(): TextDrawable.IShapeBuilder
    }

    interface IBuilder {

        fun build(text: String, color: Int): TextDrawable
    }

    interface IShapeBuilder {

        fun beginConfig(): TextDrawable.IConfigBuilder

        fun rect(): TextDrawable.IBuilder

        fun round(): TextDrawable.IBuilder

        fun roundRect(radius: Int): TextDrawable.IBuilder

        fun buildRect(text: String, color: Int): TextDrawable

        fun buildRoundRect(text: String, color: Int, radius: Int): TextDrawable

        fun buildRound(text: String, color: Int): TextDrawable
    }

    companion object {
        private const val SHADE_FACTOR = 0.9f
        private val TAG = LogHelper.makeLogTag(TextDrawable::class.java)

        fun builder(): TextDrawable.IShapeBuilder {
            return TextDrawable.Builder()
        }
    }
}