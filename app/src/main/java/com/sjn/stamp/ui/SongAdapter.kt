package com.sjn.stamp.ui

import com.sjn.stamp.ui.item.AbstractItem
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem

class SongAdapter(items: List<AbstractFlexibleItem<*>>, listeners: Any) : FlexibleAdapter<AbstractFlexibleItem<*>>(items, listeners, true) {

    private var originalItemListener: OriginalItemListener? = null

    interface OriginalItemListener {
        fun onNeedOriginalItem(): List<AbstractFlexibleItem<*>>
    }

    val originalItems: List<AbstractFlexibleItem<*>>
        get() {
            return originalItemListener?.onNeedOriginalItem() ?: emptyList()
        }

    override fun addListener(listener: Any?): FlexibleAdapter<AbstractFlexibleItem<*>> {
        if (listener is OriginalItemListener) {
            originalItemListener = listener
        }
        return super.addListener(listener)
    }

    fun showLayoutInfo(item: AbstractItem<*>, scrollToPosition: Boolean) {
        if (!hasSearchText()) {
            addScrollableHeaderWithDelay(item, 1200L, scrollToPosition)
            removeScrollableHeaderWithDelay(item, 4000L)
        }
    }
}