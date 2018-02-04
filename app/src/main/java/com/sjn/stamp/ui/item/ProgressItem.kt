package com.sjn.stamp.ui.item

import android.view.View
import com.sjn.stamp.R
import com.sjn.stamp.ui.item.holder.ProgressViewHolder
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.Payload
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem

class ProgressItem : AbstractFlexibleItem<ProgressViewHolder>() {

    private var status = StatusEnum.MORE_TO_LOAD

    override fun equals(other: Any?): Boolean = this === other//The default implementation

    override fun hashCode(): Int = status.hashCode()

    override fun getLayoutRes(): Int = R.layout.progress_item

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<*>): ProgressViewHolder =
            ProgressViewHolder(view, adapter)

    override fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: ProgressViewHolder,
                                position: Int, payloads: List<*>) {

        val context = holder.itemView.context
        holder.progressBar.visibility = View.GONE
        holder.progressMessage.visibility = View.VISIBLE

        if (!adapter.isEndlessScrollEnabled) {
            status = StatusEnum.DISABLE_ENDLESS
        } else if (payloads.contains(Payload.NO_MORE_LOAD)) {
            status = StatusEnum.NO_MORE_LOAD
        }

        when (this.status) {
            ProgressItem.StatusEnum.NO_MORE_LOAD -> {
                holder.progressMessage.text = context.getString(R.string.no_more_load_retry)
                // Reset to default status for next binding
                status = StatusEnum.MORE_TO_LOAD
            }
            ProgressItem.StatusEnum.DISABLE_ENDLESS -> holder.progressMessage.text = context.getString(R.string.no_more_data)
            ProgressItem.StatusEnum.ON_CANCEL -> {
                holder.progressMessage.text = context.getString(R.string.no_more_data_by_error)
                // Reset to default status for next binding
                status = StatusEnum.MORE_TO_LOAD
            }
            ProgressItem.StatusEnum.ON_ERROR -> {
                holder.progressMessage.text = context.getString(R.string.no_more_data_by_error)
                // Reset to default status for next binding
                status = StatusEnum.MORE_TO_LOAD
            }
            else -> {
                holder.progressBar.visibility = View.VISIBLE
                holder.progressMessage.visibility = View.GONE
            }
        }
    }

    private enum class StatusEnum {
        MORE_TO_LOAD, //Default = should have an empty Payload
        DISABLE_ENDLESS, //Endless is disabled because user has set limits
        NO_MORE_LOAD, //Non-empty Payload = Payload.NO_MORE_LOAD
        ON_CANCEL,
        ON_ERROR
    }

}