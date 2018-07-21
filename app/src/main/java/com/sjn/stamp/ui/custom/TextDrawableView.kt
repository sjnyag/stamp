package com.sjn.stamp.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.widget.TextView

class TextDrawableView(context: Context, attrs: AttributeSet) : TextView(context, attrs) {

    override fun onDraw(canvas: Canvas) {
        setDrawableTint()
        super.onDraw(canvas)
    }

    private fun setDrawableTint() {
        val drawables = this.compoundDrawables
        if (drawables.isEmpty()) {
            return
        }

        var i = 0
        val size = drawables.size
        while (i < size) {
            var drawable: Drawable? = drawables[i]
            if (drawable == null) {
                i++
                continue
            }
            drawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(drawable, currentTextColor)
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN)
            drawables[i] = drawable
            i++
        }
        this.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3])
    }


}