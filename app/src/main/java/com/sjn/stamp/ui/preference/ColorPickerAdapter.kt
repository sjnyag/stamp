package com.sjn.stamp.ui.preference

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sjn.stamp.R
import com.sjn.stamp.getCurrent
import io.multimoon.colorful.ThemeColor

internal class ColorPickerAdapter(private val context: Context) : RecyclerView.Adapter<ColorPickerAdapter.ItemViewHolder>() {
    private var onItemClickListener: OnItemClickListener? = null

    override fun getItemCount(): Int {
        return ThemeColor.values().size
    }

    override fun onBindViewHolder(ViewHolder: ItemViewHolder, i: Int) {
        ViewHolder.circle.setColor(ThemeColor.values()[i].getColorPack().normal().asInt())
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemViewHolder {
        val holder = ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_coloritem, viewGroup, false))
        holder.circle.setOnClickListener {
            onItemClickListener?.onItemClick(ThemeColor.values()[holder.adapterPosition])
        }
        return holder
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    internal class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var circle: CircularView = view as CircularView
    }

    internal interface OnItemClickListener {
        fun onItemClick(color: ThemeColor)
    }
}
