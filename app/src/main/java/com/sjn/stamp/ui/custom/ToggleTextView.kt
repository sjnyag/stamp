package com.sjn.stamp.ui.custom

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet

import com.sjn.stamp.R

class ToggleTextView(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs) {

    var value = false

    init {
        changeViewBackground(value)
    }

    override fun performClick(): Boolean {
        value = !value
        changeViewBackground(value)
        return super.performClick()
    }

    override fun callOnClick(): Boolean {
        value = !value
        changeViewBackground(value)
        return super.callOnClick()
    }

    private fun changeViewBackground(value: Boolean) {
        this.background = ContextCompat.getDrawable(context, if (value) R.drawable.toggle_on_text else R.drawable.toggle_off_text)
    }

}