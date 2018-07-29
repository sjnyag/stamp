package com.sjn.stamp.ui.fragment

import android.content.DialogInterface
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sjn.stamp.R
import com.sjn.stamp.controller.SongController
import com.sjn.stamp.model.Song
import com.sjn.stamp.model.dao.SongDao
import com.sjn.stamp.ui.DialogFacade
import com.sjn.stamp.ui.SongAdapter
import com.sjn.stamp.ui.fragment.media.ListFragment
import com.sjn.stamp.ui.item.SimpleMediaMetadataItem
import com.sjn.stamp.ui.item.UnknownSongItem
import com.sjn.stamp.ui.observer.StampEditStateObserver
import com.sjn.stamp.utils.MediaItemHelper
import com.sjn.stamp.utils.MediaRetrieveHelper
import com.sjn.stamp.utils.RealmHelper
import eu.davidea.fastscroller.FastScroller
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem

class UnknownSongListFragment : ListFragment() {
    private var mergeSongRecyclerView: RecyclerView? = null
    private var mergeSongAdapter: SongAdapter? = null

    private var songSelectDialog: AlertDialog? = null

    /**
     * [ListFragment]
     */
    override val menuResourceId: Int
        get() = R.menu.song_list

    override fun onRefresh() {
        activity?.let {
            DialogFacade.createConfirmDialog(it, R.string.dialog_confirm_song_db_refresh,
                    { _, _ -> CreateUnknownSongListAsyncTask(this).execute() },
                    { _, _ -> swipeRefreshLayout?.let { it.isRefreshing = false } },
                    DialogInterface.OnDismissListener { }).show()
        }
    }

    override fun onSelectedStampChange(selectedStampList: List<String>) {}

    override fun onNewStampCreated(stamp: String) {}

    override fun onStampStateChange(state: StampEditStateObserver.State) {}

    override fun onFastScrollerStateChange(scrolling: Boolean) {}

    override fun noMoreLoad(newItemsSize: Int) {}

    override fun onLoadMore(lastPosition: Int, currentPage: Int) {}

    override fun onItemClick(position: Int): Boolean {
        adapter?.let {
            val item = it.getItem(position)
            if (item is UnknownSongItem) {
                openSongSelectDialog(item.songId)
            }
        }
        return false
    }

    override fun onItemLongClick(position: Int) {}

    override fun onUpdateEmptyView(size: Int) {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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
            setOnRefreshListener(this@UnknownSongListFragment)
            setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
        }
        currentItems = createItemList()
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
            this.adapter = this@UnknownSongListFragment.adapter
        }
        adapter?.apply {
            fastScroller = rootView.findViewById<View>(R.id.fast_scroller) as FastScroller
            isLongPressDragEnabled = false
            isHandleDragEnabled = false
            isSwipeEnabled = false
            setUnlinkAllItemsOnRemoveHeaders(false)
            setDisplayHeadersAtStartUp(false)
            setStickyHeaders(false)
            showAllHeaders()
        }
        notifyFragmentChange()
        loading?.let {
            it.visibility = View.GONE
        }
        return rootView
    }

    private fun openSongSelectDialog(unknownSongId: Long) {
        mergeSongRecyclerView = RecyclerView(activity, null)
        mergeSongAdapter = SongAdapter(emptyList(), FlexibleAdapter.OnItemClickListener { p ->
            mergeSongAdapter?.let {
                val item = it.getItem(p)
                if (item is SimpleMediaMetadataItem) {
                    context?.let {
                        val song = SongController(it).findSong(unknownSongId)
                        val mediaMetadata = SongController(it).resolveMediaMetadata(item.mediaId)
                        if (song != null && mediaMetadata != null) {
                            openMergeConfirmDialog(p, song, mediaMetadata)
                        }
                    }
                }
            }
            false
        })
        mergeSongRecyclerView?.let { view ->
            view.layoutManager = SmoothScrollLinearLayoutManager(activity)
            view.adapter = mergeSongAdapter
            context?.let {
                songSelectDialog = DialogFacade.createSelectValidSongDialog(it, view)
            }
        }
        CreateMergeSongListAsyncTask(this).execute()
    }

    private fun openMergeConfirmDialog(position: Int, unknownSong: Song, mediaMetadata: MediaMetadataCompat) {
        activity?.let {
            DialogFacade.createConfirmDialog(it, resources.getString(R.string.dialog_confirm_merge_song, unknownSong.title, MediaItemHelper.getTitle(mediaMetadata))
            ) { _, _ ->
                activity?.let {
                    if (SongController(it).mergeSong(unknownSong, mediaMetadata)) {
                        it.runOnUiThread {
                            adapter?.removeItem(position)
                            adapter?.notifyItemRemoved(position)
                            songSelectDialog?.dismiss()
                        }
                    }
                }
            }.show()
        }
    }

    @Synchronized
    private fun createItemList(): List<AbstractFlexibleItem<*>> =
            SongDao.findUnknown(RealmHelper.realmInstance).mapIndexedTo(ArrayList()) { id, song -> UnknownSongItem(id.toString(), song) }

    private class CreateMergeSongListAsyncTask constructor(val fragment: UnknownSongListFragment) : AsyncTask<Void, Void, List<MediaMetadataCompat>>() {

        override fun doInBackground(vararg params: Void): List<MediaMetadataCompat> {
            fragment.context?.let {
                return MediaRetrieveHelper.allMediaMetadataCompat(it, null)
            }
            return emptyList()
        }

        override fun onPostExecute(result: List<MediaMetadataCompat>) {
            if (result.isEmpty()) return
            fragment.activity?.runOnUiThread(Runnable {
                if (!fragment.isAdded) {
                    return@Runnable
                }
                fragment.mergeSongAdapter?.updateDataSet(result.map { SimpleMediaMetadataItem(it) })
            })
        }

    }

    private class CreateUnknownSongListAsyncTask constructor(val fragment: UnknownSongListFragment) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
            fragment.context?.let {
                SongController(it).refreshAllSongs(MediaRetrieveHelper.allMediaMetadataCompat(it, null))
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            if (!fragment.isAdded) {
                return
            }
            fragment.currentItems = fragment.createItemList()
            fragment.activity?.runOnUiThread {
                fragment.adapter?.updateDataSet(fragment.currentItems)
                fragment.swipeRefreshLayout?.let {
                    it.isRefreshing = false
                }
            }
        }

    }
}
