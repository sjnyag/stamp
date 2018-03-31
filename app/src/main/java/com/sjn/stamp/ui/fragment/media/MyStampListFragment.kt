package com.sjn.stamp.ui.fragment.media

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sjn.stamp.R
import com.sjn.stamp.controller.StampController
import com.sjn.stamp.ui.SongAdapter
import com.sjn.stamp.ui.activity.DrawerActivity
import com.sjn.stamp.ui.item.AbstractItem
import com.sjn.stamp.ui.item.SongItem
import com.sjn.stamp.utils.AlbumArtHelper
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.SwipeHelper
import eu.davidea.fastscroller.FastScroller
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager
import eu.davidea.flexibleadapter.helpers.UndoHelper

class MyStampListFragment : SongListFragment(), UndoHelper.OnUndoListener, FlexibleAdapter.OnItemSwipeListener {

    private val categoryValue: String?
        get() = mediaId?.let { MediaIDHelper.extractBrowseCategoryValueFromMediaID(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        LogHelper.d(TAG, "onCreateView START" + mediaId!!)
        val rootView = inflater.inflate(R.layout.fragment_list, container, false)

        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(ListFragment.LIST_STATE_KEY)
        }

        loading = rootView.findViewById(R.id.progressBar)
        emptyView = rootView.findViewById(R.id.empty_view)
        fastScroller = rootView.findViewById(R.id.fast_scroller)
        emptyTextView = rootView.findViewById(R.id.empty_text)
        swipeRefreshLayout = rootView.findViewById(R.id.refresh)
        recyclerView = rootView.findViewById(R.id.recycler_view)

        swipeRefreshLayout?.apply {
            setOnRefreshListener(this@MyStampListFragment)
            setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
        }
        adapter = SongAdapter(currentItems, this).apply {
            setNotifyChangeOfUnfilteredItems(true)
            setAnimationOnScrolling(false)
        }
        recyclerView?.apply {
            activity?.let {
                this.layoutManager = SmoothScrollLinearLayoutManager(it).apply {
                    listState?.let { onRestoreInstanceState(it) }
                }
            }
            this.adapter = this@MyStampListFragment.adapter
        }
        adapter?.apply {
            fastScroller = rootView.findViewById<View>(R.id.fast_scroller) as FastScroller
            isLongPressDragEnabled = false
            isHandleDragEnabled = false
            isSwipeEnabled = true
            setUnlinkAllItemsOnRemoveHeaders(false)
            setDisplayHeadersAtStartUp(false)
            setStickyHeaders(false)
            showAllHeaders()
        }
        initializeFabWithStamp()
        if (isShowing) {
            notifyFragmentChange()
        }
        mediaId?.let {
            MediaIDHelper.extractBrowseCategoryValueFromMediaID(it)?.let {
                arguments?.also {
                    if (activity is DrawerActivity) {
                        (activity as DrawerActivity).run {
                            updateAppbar(it.getString("IMAGE_TEXT"), { activity, imageView ->
                                AlbumArtHelper.loadAlbumArt(activity, imageView, it.getParcelable("IMAGE_BITMAP"), it.getString("IMAGE_TYPE"), it.getString("IMAGE_URL"), it.getString("IMAGE_TEXT"))
                            })
                        }

                    }
                }
            }
        }
        draw()
        LogHelper.d(TAG, "onCreateView END")
        return rootView
    }


    override fun onItemSwipe(position: Int, direction: Int) {
        LogHelper.i(TAG, "onItemSwipe position=" + position +
                " direction=" + if (direction == ItemTouchHelper.LEFT) "LEFT" else "RIGHT")
        if (adapter?.getItem(position) !is SongItem) return
        val item = adapter?.getItem(position) as SongItem
        activity?.run {
            if (categoryValue?.let { StampController(this).isCategoryStamp(it, false, item.mediaId) } == true) {
                Toast.makeText(this, R.string.error_message_stamp_failed, Toast.LENGTH_LONG).show()
                SwipeHelper.cancel(recyclerView, position)
                return
            }
            tryRemove(item, position)
        }
    }

    private fun tryRemove(item: AbstractItem<*>, position: Int) {
        val positions = mutableListOf(position)
        val message = StringBuilder().append(item.title).append(" ").append(getString(R.string.action_deleted))
        if (item.isSelectable) adapter?.setRestoreSelectionOnUndo(false)
        adapter?.isPermanentDelete = false
        swipeRefreshLayout?.isRefreshing = true
        activity?.let {
            UndoHelper(adapter, this@MyStampListFragment)
                    .withPayload(null)
                    .withConsecutive(true)
                    .start(positions, it.findViewById(R.id.main_view), message, getString(R.string.undo), UndoHelper.UNDO_TIMEOUT)
        }
    }


    override fun onActionStateChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        LogHelper.i(TAG, "onActionStateChanged actionState=$actionState")
        swipeRefreshLayout?.isEnabled = actionState == ItemTouchHelper.ACTION_STATE_IDLE
    }

    override fun onActionCanceled(action: Int) {
        LogHelper.i(TAG, "onUndoConfirmed action=$action")
        adapter?.restoreDeletedItems()
        swipeRefreshLayout?.isRefreshing = false
        if (adapter?.isRestoreWithSelection == true) listener?.restoreSelection()
    }

    override fun onActionConfirmed(action: Int, event: Int) {
        LogHelper.i(TAG, "onDeleteConfirmed action=$action")
        swipeRefreshLayout?.isRefreshing = false
        for (adapterItem in adapter?.deletedItems ?: emptyList()) {
            try {
                when (adapterItem.layoutRes) {
                    R.layout.recycler_song_item -> {
                        val subItem = adapterItem as SongItem
                        activity?.let {
                            StampController(it).run {
                                categoryValue?.let { removeSong(it, false, subItem.mediaId) }
                            }
                            LogHelper.i(TAG, "Confirm removed " + subItem.toString())
                        }
                    }
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private val TAG = LogHelper.makeLogTag(MyStampListFragment::class.java)
    }

}
