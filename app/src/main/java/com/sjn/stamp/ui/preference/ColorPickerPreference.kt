package com.sjn.stamp.ui.preference

import android.content.Context
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceViewHolder
import android.util.AttributeSet
import com.sjn.stamp.R
import com.sjn.stamp.utils.getCurrent
import io.multimoon.colorful.Colorful
import io.multimoon.colorful.ThemeColor

class ColorPickerPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs), ColorPickerDialog.OnColorSelectedListener {
    private var primary: Boolean = false
    private var accent: Boolean = false

    init {
        widgetLayoutResource = R.layout.preference_colorpicker
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.colorpicker)
        try {
            primary = typedArray.getBoolean(R.styleable.colorpicker_primary_color, false)
            accent = typedArray.getBoolean(R.styleable.colorpicker_accent_color, false)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onColorSelected(color: ThemeColor) {
        onPreferenceChangeListener?.onPreferenceChange(this, color)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        (holder.findViewById(R.id.color_indicator) as CircularView).setColor(Colorful().getCurrent(primary).asInt())
    }

    override fun onClick() {
        super.onClick()
        ColorPickerDialog(context, primary).apply {
            setOnColorSelectedListener(this@ColorPickerPreference)
        }.show()
    }
}
