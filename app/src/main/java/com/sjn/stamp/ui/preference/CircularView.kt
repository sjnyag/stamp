package com.sjn.stamp.ui.preference

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View

internal class CircularView : View {

    var paint = Paint()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @Suppress("unused")
    @RequiresApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    fun setColor(@ColorInt color: Int) {
        paint.color = color
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle((canvas.width / 2).toFloat(), (canvas.height / 2).toFloat(), (canvas.width / 2).toFloat(), paint)
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = Math.min(measuredWidth, measuredHeight)
        setMeasuredDimension(size, size)
    }
}
