package com.sjn.stamp.ui.fragment

import android.content.DialogInterface
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sjn.stamp.R
import com.sjn.stamp.controller.SongController
import com.sjn.stamp.model.Song
import com.sjn.stamp.model.dao.SongDao
import com.sjn.stamp.ui.DialogFacade
import com.sjn.stamp.ui.SongAdapter
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

class UnknownSongFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, FastScroller.OnScrollStateChangeListener, FlexibleAdapter.OnItemClickListener, FlexibleAdapter.OnItemLongClickListener, FlexibleAdapter.EndlessScrollListener, FlexibleAdapter.OnUpdateListener, StampEditStateObserver.Listener {

    private var mUnknownSongRecyclerView: RecyclerView? = null
    private var mMergeSongRecyclerView: RecyclerView? = null
    private var mUnknownSongAdapter: SongAdapter? = null
    private var mMergeSongAdapter: SongAdapter? = null

    private var mLoading: ProgressBar? = null
    private var mEmptyView: View? = null
    private var mEmptyTextView: TextView? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mFastScroller: FastScroller? = null
    private var mListState: Parcelable? = null

    private var mSongSelectDialog: MaterialDialog? = null

    override fun onRefresh() {
        DialogFacade.createConfirmDialog(activity, R.string.dialog_confirm_song_db_refresh, { _, which ->
            when (which) {
                DialogAction.NEGATIVE -> {
                    mSwipeRefreshLayout?.let {
                        it.isRefreshing = false
                    }
                }
                DialogAction.POSITIVE -> {
                    CreateUnknownSongListAsyncTask(this).execute()
                }
                else -> {
                }
            }
        }, { }).show()

    }

    override fun onSelectedStampChange(selectedStampList: List<String>) {

    }

    override fun onNewStampCreated(stamp: String) {

    }

    override fun onStampStateChange(state: StampEditStateObserver.State) {

    }

    override fun onFastScrollerStateChange(scrolling: Boolean) {

    }

    override fun noMoreLoad(newItemsSize: Int) {

    }

    override fun onLoadMore(lastPosition: Int, currentPage: Int) {

    }

    override fun onItemClick(position: Int): Boolean {
        mUnknownSongAdapter?.let {
            val item = it.getItem(position)
            if (item is UnknownSongItem) {
                openSongSelectDialog(item.songId)
            }
        }
        return false
    }

    override fun onItemLongClick(position: Int) {

    }

    override fun onUpdateEmptyView(size: Int) {

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_list, container, false)

        mLoading = rootView.findViewById(R.id.progressBar)
        mEmptyView = rootView.findViewById(R.id.empty_view)
        mFastScroller = rootView.findViewById(R.id.fast_scroller)
        mEmptyTextView = rootView.findViewById(R.id.empty_text)

        mSwipeRefreshLayout = rootView.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.let {
            it.setOnRefreshListener(this)
            it.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
        }

        mUnknownSongAdapter = SongAdapter(createItemList(), this)
        mUnknownSongAdapter?.let {
            it.setNotifyChangeOfUnfilteredItems(true)
            it.setAnimationOnScrolling(false)
        }
        mUnknownSongRecyclerView = rootView.findViewById(R.id.recycler_view)
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY)
        }
        val layoutManager = SmoothScrollLinearLayoutManager(activity)
        if (mListState != null) {
            layoutManager.onRestoreInstanceState(mListState)
        }
        mUnknownSongRecyclerView?.let {
            it.layoutManager = layoutManager
            it.adapter = mUnknownSongAdapter
        }
        mUnknownSongAdapter?.let { adapter ->
            activity?.let {
                adapter.fastScroller = rootView.findViewById<View>(R.id.fast_scroller) as FastScroller
                adapter.setLongPressDragEnabled(false)
                        .setHandleDragEnabled(false)
                        .setSwipeEnabled(false)
                        .setUnlinkAllItemsOnRemoveHeaders(false)
                        .setDisplayHeadersAtStartUp(false)
                        .setStickyHeaders(false)
                        .showAllHeaders()
                adapter.addUserLearnedSelection(savedInstanceState == null)
            }
        }
        mLoading?.let {
            it.visibility = View.GONE
        }
        return rootView
    }

    private fun openSongSelectDialog(unknownSongId: Long) {
        mMergeSongRecyclerView = RecyclerView(activity, null)
        mMergeSongAdapter = SongAdapter(emptyList(), FlexibleAdapter.OnItemClickListener({ p ->
            mMergeSongAdapter?.let {
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
        }))
        mMergeSongRecyclerView?.let { view ->
            view.layoutManager = SmoothScrollLinearLayoutManager(activity)
            view.adapter = mMergeSongAdapter
            context?.let {
                mSongSelectDialog = MaterialDialog.Builder(it)
                        .title(R.string.dialog_merge_song)
                        .customView(view, false)
                        .negativeText(R.string.dialog_cancel)
                        .contentColorRes(android.R.color.white)
                        .backgroundColorRes(R.color.material_blue_grey_800)
                        .theme(Theme.DARK)
                        .show()
            }
        }
        CreateMergeSongListAsyncTask(this).execute()
    }

    private fun openMergeConfirmDialog(position: Int, unknownSong: Song, mediaMetadata: MediaMetadataCompat) {
        DialogFacade.createConfirmDialog(activity, resources.getString(R.string.dialog_confirm_merge_song, unknownSong.title, MediaItemHelper.getTitle(mediaMetadata)), MaterialDialog.SingleButtonCallback { _, which ->
            when (which) {
                DialogAction.NEGATIVE -> return@SingleButtonCallback
                DialogAction.POSITIVE -> {
                    activity?.let {
                        if (SongController(it).mergeSong(unknownSong, mediaMetadata)) {
                            it.runOnUiThread({
                                mUnknownSongAdapter?.removeItem(position)
                                mUnknownSongAdapter?.notifyItemRemoved(position)
                                mSongSelectDialog?.dismiss()
                            })
                        }
                    }
                }
                else -> {
                }
            }
        }, DialogInterface.OnDismissListener { }).show()

    }


    @Synchronized private fun createItemList(): List<AbstractFlexibleItem<*>> =
            SongDao.findUnknown(RealmHelper.getRealmInstance()).mapIndexedTo(ArrayList()) { id, song -> UnknownSongItem(id.toString(), song) }

    companion object {
        private const val LIST_STATE_KEY = "LIST_STATE_KEY"
    }

    private class CreateMergeSongListAsyncTask constructor(val fragment: UnknownSongFragment) : AsyncTask<Void, Void, List<MediaMetadataCompat>>() {

        override fun doInBackground(vararg params: Void): List<MediaMetadataCompat> =
                MediaRetrieveHelper.allMediaMetadataCompat(fragment.context, null)

        override fun onPostExecute(result: List<MediaMetadataCompat>) {
            fragment.activity?.runOnUiThread(Runnable {
                if (!fragment.isAdded) {
                    return@Runnable
                }
                fragment.mMergeSongAdapter?.updateDataSet(result.map { SimpleMediaMetadataItem(it) })
            })
        }

    }

    private class CreateUnknownSongListAsyncTask constructor(val fragment: UnknownSongFragment) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
            fragment.context?.let {
                SongController(it).refreshAllSongs(MediaRetrieveHelper.allMediaMetadataCompat(it, null))
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            fragment.activity?.runOnUiThread(Runnable {
                if (!fragment.isAdded) {
                    return@Runnable
                }
                fragment.mUnknownSongAdapter?.updateDataSet(fragment.createItemList())
                fragment.mSwipeRefreshLayout?.let {
                    it.isRefreshing = false
                }
            })
        }

    }
}
