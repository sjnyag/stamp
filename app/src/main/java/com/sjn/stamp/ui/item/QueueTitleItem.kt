package com.sjn.stamp.ui.item

import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.View
import com.sjn.stamp.R
import com.sjn.stamp.media.provider.ProviderType
import com.sjn.stamp.ui.item.holder.QueueTitleViewHolder
import eu.davidea.flexibleadapter.FlexibleAdapter

class QueueTitleItem(providerType: ProviderType?, providerValue: String?) : AbstractItem<QueueTitleViewHolder>(providerType?.name + providerValue) {
    override val title = providerType?.name ?: ""
    override val subtitle = providerValue ?: ""

    override fun getLayoutRes(): Int = R.layout.recycler_queue_title_item

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<*>): QueueTitleViewHolder = QueueTitleViewHolder(view, adapter)

    override fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: QueueTitleViewHolder, position: Int, payloads: List<*>) {
        holder.title.text = title
        holder.subtitle.text = subtitle
        holder.subtitle.visibility = if (subtitle.isEmpty()) View.GONE else View.VISIBLE
        //Support for StaggeredGridLayoutManager
        if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
            Log.d("ScrollableLayoutItem", "LayoutItem configured fullSpan for StaggeredGridLayout")
        }
    }
}