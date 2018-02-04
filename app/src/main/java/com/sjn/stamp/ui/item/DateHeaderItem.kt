package com.sjn.stamp.ui.item

import android.view.View
import com.sjn.stamp.R
import com.sjn.stamp.ui.item.holder.HeaderViewHolder
import com.sjn.stamp.utils.TimeHelper
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractHeaderItem
import eu.davidea.flexibleadapter.items.IFilterable
import java.util.*

class DateHeaderItem(date: Date) : AbstractHeaderItem<HeaderViewHolder>(), IFilterable {
    val date = TimeHelper.toDateOnly(date)
    val title = TimeHelper.toDateTime(date).toLocalDate().toString()

    init {
        isDraggable = false
    }

    override fun equals(other: Any?): Boolean {
        if (other is DateHeaderItem) {
            val inItem = other as DateHeaderItem?
            return this.date == inItem!!.date
        }
        return false
    }

    override fun hashCode(): Int = date.hashCode()

    override fun getLayoutRes(): Int = R.layout.recycler_header_item

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<*>): HeaderViewHolder = HeaderViewHolder(view, adapter)

    override fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: HeaderViewHolder, position: Int, payloads: List<*>) {
        holder.title.text = title
        holder.subtitle.text = holder.title.context.run {
            adapter.getSectionItems(this@DateHeaderItem).run {
                if (isEmpty()) getString(R.string.item_date_header_empty) else getString(R.string.item_date_header_item_counts, size.toString())
            }
        }
    }

    override fun filter(constraint: String): Boolean = title.toLowerCase().trim { it <= ' ' }.contains(constraint)

    fun isDateOf(recordedAt: Date): Boolean = TimeHelper.toDateOnly(recordedAt).compareTo(date) == 0

}