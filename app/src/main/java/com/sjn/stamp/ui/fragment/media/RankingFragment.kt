package com.sjn.stamp.ui.fragment.media

import android.app.ProgressDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sjn.stamp.R
import com.sjn.stamp.controller.SongHistoryController
import com.sjn.stamp.model.RankedArtist
import com.sjn.stamp.model.RankedSong
import com.sjn.stamp.model.Shareable
import com.sjn.stamp.ui.SongAdapter
import com.sjn.stamp.ui.custom.PeriodSelectLayout
import com.sjn.stamp.ui.item.RankedArtistItem
import com.sjn.stamp.ui.item.RankedSongItem
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaItemHelper
import com.sjn.stamp.utils.RealmHelper
import eu.davidea.fastscroller.FastScroller
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import io.realm.Realm

class RankingFragment : MediaBrowserListFragment() {

    private var mPeriod: PeriodSelectLayout.Period = PeriodSelectLayout.Period.latestWeek()
    private var mRankKind: RankKind? = null
    private var mSongHistoryController: SongHistoryController? = null
    private var mAsyncTask: CalculateAsyncTask? = null
    private var mProgressDialog: ProgressDialog? = null

    /**
     * [ListFragment]
     */
    override val menuResourceId: Int
        get() = R.menu.ranking

    override fun emptyMessage(): String {
        return getString(R.string.empty_message_ranking)
    }

    /**
     * [MediaBrowserListFragment]
     */
    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {}

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {}

    override fun onMediaBrowserChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {}

    override fun onMediaBrowserError(parentId: String) {}

    /**
     * [SwipeRefreshLayout.OnRefreshListener]
     */
    override fun onRefresh() {
        listener?.destroyActionModeIfCan()
        swipeRefreshLayout?.isRefreshing = false
        draw()
    }


    /**
     * [FlexibleAdapter.OnItemClickListener]
     */
    override fun onItemClick(position: Int): Boolean {
        LogHelper.d(TAG, "onItemClick ")
        val item = adapter?.getItem(position)
        if (item is RankedSongItem) {
            mediaBrowsable?.onMediaItemSelected(item.mediaId)
        } else if (item is RankedArtistItem) {
            mediaBrowsable?.onMediaItemSelected(MediaItemHelper.createArtistMediaItem(item.artistName))
        }
        return false
    }

    /**
     * [FlexibleAdapter.OnItemLongClickListener]
     */
    override fun onItemLongClick(position: Int) {
        listener?.startActionModeByLongClick(position)
    }

    /**
     * [FlexibleAdapter.EndlessScrollListener]
     */
    override fun noMoreLoad(newItemsSize: Int) {}

    override fun onLoadMore(lastPosition: Int, currentPage: Int) {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(false)
        val rootView = inflater.inflate(R.layout.fragment_list, container, false)
        mRankKind = parseArgRankKind()
        context?.let {
            mSongHistoryController = SongHistoryController(it)
        }
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
            setOnRefreshListener(this@RankingFragment)
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
            this.adapter = this@RankingFragment.adapter
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
        if (isShowing) {
            notifyFragmentChange()
        }
        if (currentItems.isEmpty()) {
            loading?.visibility = View.VISIBLE
            draw()
        } else {
            loading?.visibility = View.GONE
        }
        return rootView
    }

    @Synchronized
    private fun draw() {
        if (mRankKind == null) {
            return
        }
        mRankKind?.let { rankKind ->
            adapter?.let { adapter ->
                mSongHistoryController?.let { controller ->
                    mAsyncTask?.cancel(true)
                    mAsyncTask = CalculateAsyncTask(this, adapter, rankKind, mPeriod, controller)
                    mAsyncTask?.execute()
                }
            }
        }
    }

    fun setPeriodAndReload(period: PeriodSelectLayout.Period) {
        mPeriod = period
        mProgressDialog = ProgressDialog(activity).apply {
            setMessage(getString(R.string.message_processing))
        }
        mProgressDialog?.show()
        draw()
    }

    private fun parseArgRankKind(): RankKind? =
            RankKind.of(arguments?.getString(PagerFragment.PAGER_KIND_KEY))

    private class CalculateAsyncTask internal constructor(internal var fragment: RankingFragment, internal var adapter: SongAdapter, internal var mCallback: Callback, internal var mPeriod: PeriodSelectLayout.Period, internal var mSongHistoryController: SongHistoryController) : AsyncTask<Void, Void, Void>() {

        internal interface Callback {
            fun createItemList(context: Context, realm: Realm, period: PeriodSelectLayout.Period, songHistoryController: SongHistoryController): List<AbstractFlexibleItem<*>>
        }

        override fun doInBackground(vararg params: Void): Void? {
            RealmHelper.realmInstance.use {
                fragment.activity?.let { activity ->
                    fragment.currentItems = mCallback.createItemList(activity, it, mPeriod, mSongHistoryController)
                    activity.runOnUiThread(Runnable {
                        if (!fragment.isAdded) {
                            return@Runnable
                        }
                        fragment.loading?.visibility = View.INVISIBLE
                        adapter.updateDataSet(fragment.currentItems)
                    })
                }
            }
            fragment.mProgressDialog?.dismiss()
            return null
        }

    }

    internal enum class RankKind : CalculateAsyncTask.Callback {

        SONG {
            override fun getRankingShareMessage(resources: Resources, controller: SongHistoryController, period: PeriodSelectLayout.Period, songNum: Int): String =
                    RealmHelper.realmInstance.use { createShareMessage(resources, controller.getRankedSongList(it, period).toList(), songNum) }

            override fun createItemList(context: Context, realm: Realm, period: PeriodSelectLayout.Period, songHistoryController: SongHistoryController): List<AbstractFlexibleItem<*>> {
                var order = 1
                return songHistoryController.getRankedSongList(realm, period).map { newSimpleItem(it, order++) }
            }

            private fun newSimpleItem(rankedSong: RankedSong, order: Int): RankedSongItem {
                return RankedSongItem(rankedSong.song.buildMediaMetadataCompat(), rankedSong.playCount, order)
            }
        },

        ARTIST {
            override fun getRankingShareMessage(resources: Resources, controller: SongHistoryController, period: PeriodSelectLayout.Period, songNum: Int): String =
                    RealmHelper.realmInstance.use { createShareMessage(resources, controller.getRankedArtistList(it, period).toList(), songNum) }

            override fun createItemList(context: Context, realm: Realm, period: PeriodSelectLayout.Period, songHistoryController: SongHistoryController): List<AbstractFlexibleItem<*>> {
                var order = 1
                return songHistoryController.getRankedArtistList(realm, period).map { newSimpleItem(context, it, order++) }
            }

            private fun newSimpleItem(context: Context, rankedArtist: RankedArtist, order: Int): RankedArtistItem =
                    RankedArtistItem(context, rankedArtist.mostPlayedSong()!!.buildMediaMetadataCompat(), rankedArtist.artist.name, rankedArtist.artist.albumArtUri, rankedArtist.playCount, order)

        };

        abstract fun getRankingShareMessage(resources: Resources, controller: SongHistoryController, period: PeriodSelectLayout.Period, songNum: Int): String

        companion object {

            fun of(value: String?): RankKind? {
                return RankKind.values().firstOrNull { it.toString() == value }
            }

            private fun createShareMessage(resources: Resources, shareableList: List<Shareable>, songNum: Int): String {
                var content = ""
                var order = 1
                for (shareable in shareableList) {
                    content += resources.getString(R.string.share_each_rank, order++, shareable.share(resources))
                    if (order > songNum) {
                        break
                    }
                }
                return content
            }
        }
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(RankingFragment::class.java)
    }

}