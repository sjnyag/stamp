package com.sjn.stamp.ui

import com.sjn.stamp.ui.item.AbstractItem

import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem

class SongAdapter(items: List<AbstractFlexibleItem<*>>, listeners: Any) : FlexibleAdapter<AbstractFlexibleItem<*>>(items, listeners, true) {

    fun showLayoutInfo(item: AbstractItem<*>, scrollToPosition: Boolean) {
        if (!hasSearchText()) {
            addScrollableHeaderWithDelay(item, 1200L, scrollToPosition)
            removeScrollableHeaderWithDelay(item, 4000L)
        }
    }
}