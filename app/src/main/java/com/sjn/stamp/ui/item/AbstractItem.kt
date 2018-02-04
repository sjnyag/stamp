package com.sjn.stamp.ui.item

import android.content.Context

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.viewholders.FlexibleViewHolder

abstract class AbstractItem<VH : FlexibleViewHolder> internal constructor(var id: String) : AbstractFlexibleItem<VH>() {
    abstract val title: String
    abstract val subtitle: String

    override fun equals(other: Any?): Boolean {
        if (other is AbstractItem<*>) {
            val inItem = other as AbstractItem<*>?
            return this.id == inItem?.id
        }
        return false
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "id=$id, title=$title"

    open fun delete(context: Context) {}
}