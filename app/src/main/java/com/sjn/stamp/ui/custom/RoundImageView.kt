package com.sjn.stamp.ui.custom

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import com.sjn.stamp.R
import com.sjn.stamp.utils.CompatibleHelper

class RoundImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatImageView(context, attrs) {

    private var maskedPaint = Paint().apply {
        xfermode = PorterDuffXfermode(
                PorterDuff.Mode.SRC_ATOP)
    }
    private var copyPaint = Paint()
    private var maskDrawable: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.round_mask, null)

    private var bounds: Rect? = null
    private var boundsF: RectF? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        bounds = Rect(0, 0, w, h)
        boundsF = RectF(bounds)
    }

    override fun onDraw(canvas: Canvas) {
        val sc = CompatibleHelper.saveLayer(canvas, boundsF, copyPaint)
        maskDrawable?.let {
            it.bounds = bounds
            it.draw(canvas)
        }
        CompatibleHelper.saveLayer(canvas, boundsF, maskedPaint)
        super.onDraw(canvas)
        canvas.restoreToCount(sc)
    }
}