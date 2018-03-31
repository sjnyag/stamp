package com.sjn.stamp.ui.item.holder

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sjn.stamp.R
import com.sjn.stamp.controller.SongController
import com.sjn.stamp.ui.observer.StampEditStateObserver
import com.sjn.stamp.utils.MediaIDHelper
import eu.davidea.flexibleadapter.FlexibleAdapter

abstract class StampContainsViewHolder(view: View, adapter: FlexibleAdapter<*>, var activity: Activity) : LongClickDisableViewHolder(view, adapter) {
    private val stampListLayout: ViewGroup = view.findViewById(R.id.stamp_info)

    val showTapTargetView: TextView?
        get() = stampListLayout.getChildAt(0) as TextView?

    private val onNewStamp = View.OnClickListener { v ->
        val mediaId = v.getTag(R.id.text_view_new_stamp_media_id) as String
        SongController(activity).registerStampList(StampEditStateObserver.selectedStampList, mediaId, false)
        activity.runOnUiThread { updateStampList(mediaId) }
    }

    private val onRemoveStamp = View.OnClickListener { v ->
        val mediaId = v.getTag(R.id.text_view_remove_stamp_media_id) as String
        val stampName = v.getTag(R.id.text_view_remove_stamp_stamp_name) as String
        val isSystem = v.getTag(R.id.text_view_remove_stamp_is_system) as Boolean
        SongController(activity).removeStamp(stampName, mediaId, isSystem)
        activity.runOnUiThread { updateStampList(mediaId) }
    }

    fun updateStampList(mediaId: String) {
        if (!StampEditStateObserver.isStampMode) {
            stampListLayout.visibility = View.GONE
            return
        }
        stampListLayout.visibility = View.VISIBLE
        if (isStampMedia(mediaId)) {
            stampListLayout.removeAllViews()
            (LayoutInflater.from(activity).inflate(R.layout.text_view_new_stamp, null) as TextView).apply {
                setTag(R.id.text_view_new_stamp_media_id, mediaId)
                setOnClickListener(onNewStamp)
                stampListLayout.addView(this)
            }
            SongController(activity).findStampsByMediaId(mediaId).forEach { stamp ->
                val stampResource = if (stamp.isSystem) R.layout.text_view_remove_smart_stamp else R.layout.text_view_remove_stamp
                val textView = (LayoutInflater.from(activity).inflate(stampResource, null) as TextView).apply {
                    text = activity.getString(R.string.stamp_delete, stamp.name)
                    setTag(R.id.text_view_remove_stamp_stamp_name, stamp.name)
                    setTag(R.id.text_view_remove_stamp_media_id, mediaId)
                    setTag(R.id.text_view_remove_stamp_is_system, stamp.isSystem)
                    setOnClickListener(onRemoveStamp)
                }
                stampListLayout.addView(textView)
            }
        }
    }

    protected open fun isStampMedia(mediaId: String): Boolean =
            MediaIDHelper.getCategoryType(mediaId) != null || MediaIDHelper.isTrack(mediaId)
}
