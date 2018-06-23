package com.sjn.stamp.ui.preference

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View

import com.sjn.stamp.R
import com.sjn.stamp.utils.getCurrent
import io.multimoon.colorful.Colorful

import io.multimoon.colorful.ThemeColor

class ColorPickerDialog(context: Context, private val primary: Boolean) : Dialog(context), View.OnClickListener, ColorPickerAdapter.OnItemClickListener {
    private var recycler: RecyclerView? = null
    private var toolbar: Toolbar? = null
    private var listener: OnColorSelectedListener? = null

    override fun onClick(view: View) {
        dismiss()
    }

    override fun onItemClick(color: ThemeColor) {
        dismiss()
        listener?.onColorSelected(color)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_colorpicker)

        recycler = findViewById<View>(R.id.colorful_color_picker_recycler) as RecyclerView
        toolbar = findViewById<View>(R.id.colorful_color_picker_toolbar) as Toolbar
        recycler?.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = ColorPickerAdapter(context).apply {
                setOnItemClickListener(this@ColorPickerDialog)
            }
        }
        toolbar?.apply {
            setNavigationOnClickListener(this@ColorPickerDialog)
            setBackgroundColor(Colorful().getCurrent(primary).asInt())
            title = "Select Color"
            setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        }

    }

    interface OnColorSelectedListener {
        fun onColorSelected(color: ThemeColor)
    }

    fun setOnColorSelectedListener(listener: OnColorSelectedListener) {
        this.listener = listener
    }
}
