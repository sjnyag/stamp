/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sjn.stamp.ui.fragment.media

import android.content.DialogInterface
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.sjn.stamp.MusicService
import com.sjn.stamp.R
import com.sjn.stamp.ui.DialogFacade
import com.sjn.stamp.ui.SongAdapter
import com.sjn.stamp.ui.activity.DrawerActivity
import com.sjn.stamp.ui.item.SongItem
import com.sjn.stamp.ui.item.holder.SongViewHolder
import com.sjn.stamp.ui.observer.MediaControllerObserver
import com.sjn.stamp.ui.observer.MusicListObserver
import com.sjn.stamp.utils.AlbumArtHelper
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaIDHelper
import eu.davidea.fastscroller.FastScroller
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem

/**
 * A Fragment that lists all the various browsable queues available
 * from a [android.service.media.MediaBrowserService].
 *
 *
 * It uses a [MediaBrowserCompat] to connect to the [MusicService].
 * Once connected, the fragment subscribes to get all the children.
 * All [MediaBrowserCompat.MediaItem]'s that can be browsed are shown in a ListView.
 */
open class SongListFragment : MediaBrowserListFragment(), MusicListObserver.Listener, MediaControllerObserver.Listener {
    private var createListAsyncTask: CreateListAsyncTask? = null
    private var loadingVisibility = View.VISIBLE

    /**
     * [ListFragment]
     */
    override val menuResourceId: Int
        get() = R.menu.song_list

    override fun emptyMessage(): String {
        if (mediaId == null || mediaId?.isEmpty() == true) return super.emptyMessage()
        return mediaId?.let { MediaIDHelper.getProviderType(it)?.getEmptyMessage(resources) }
                ?: super.emptyMessage()
    }

    /**
     * [MediaBrowserListFragment]
     */
    override fun onMediaBrowserChildrenLoaded(parentId: String,
                                              children: List<MediaBrowserCompat.MediaItem>) {
        LogHelper.d(TAG, "onMediaBrowserChildrenLoaded")
        createListAsyncTask?.cancel(true)
        createListAsyncTask = CreateListAsyncTask(this, children)
        createListAsyncTask?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onMediaBrowserError(parentId: String) {}

    /**
     * [SwipeRefreshLayout.OnRefreshListener]
     */
    override fun onRefresh() {
        swipeRefreshLayout ?: return
        activity?.let { activity ->
            DialogFacade.createRetrieveMediaDialog(activity, MaterialDialog.SingleButtonCallback { _, which ->
                when (which) {
                    DialogAction.NEGATIVE -> {
                        swipeRefreshLayout?.isRefreshing = false
                        return@SingleButtonCallback
                    }
                    DialogAction.POSITIVE -> {
                        listener?.destroyActionModeIfCan()
                        mediaBrowsable?.sendCustomAction(MusicService.CUSTOM_ACTION_RELOAD_MUSIC_PROVIDER, null, null)
                        return@SingleButtonCallback
                    }
                    else -> swipeRefreshLayout?.isRefreshing = false
                }
            }, DialogInterface.OnDismissListener { swipeRefreshLayout?.isRefreshing = false }).show()
        }
    }

    /**
     * [FlexibleAdapter.OnItemClickListener]
     */
    override fun onItemClick(position: Int): Boolean {
        LogHelper.d(TAG, "onItemClick ")
        val item = adapter?.getItem(position)
        if (item is SongItem) {
            when {
                item.isPlayable -> {
                    mediaBrowsable?.playByMediaId(item.mediaId)
                }
                item.isBrowsable -> {
                    //mediaBrowsable?.navigateToBrowser(item.mediaId, SongListFragmentFactory.create(item.mediaId), emptyList())}
                    val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
                    if (viewHolder is SongViewHolder) {
                        activity?.let {
                            val pair = viewHolder.createNextFragment(it)
                            mediaBrowsable?.navigateToBrowser(item.mediaId, pair.first, pair.second)
                        }
                    }
                }
                else -> LogHelper.w(TAG, "Ignoring MediaItem that is neither browsable nor playable: ", "mediaId=", item.mediaId)
            }
        }
        return false
    }

    /**
     * [FlexibleAdapter.OnItemLongClickListener]
     */
    override fun onItemLongClick(position: Int) {
        val item = adapter?.getItem(position) as? SongItem ?: return
        if (!item.isPlayable) {
            mediaBrowsable?.playByCategory(item.mediaId)
        } else {
            onItemClick(position)
        }
    }

    /**
     * [FlexibleAdapter.EndlessScrollListener]
     */
    override fun noMoreLoad(newItemsSize: Int) {}

    override fun onLoadMore(lastPosition: Int, currentPage: Int) {}

    /**
     * [MusicListObserver.Listener]
     */
    override fun onMediaListUpdated() {
        LogHelper.d(TAG, "onMediaListUpdated START")
        if (mediaBrowsable?.mediaBrowser?.isConnected == true && mediaId != null) {
            reloadList()
            activity?.let {
                Handler(it.mainLooper).post {
                    swipeRefreshLayout?.isRefreshing = false
                }
            }
        }
        LogHelper.d(TAG, "onMediaListUpdated END")

    }

    /**
     * [MediaControllerObserver]
     */
    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        adapter?.notifyDataSetChanged()
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        adapter?.notifyDataSetChanged()
    }

    override fun onMediaControllerConnected() {}

    override fun onSessionDestroyed() {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        LogHelper.d(TAG, "onCreateView START $mediaId")
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
            setOnRefreshListener(this@SongListFragment)
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
            this.adapter = this@SongListFragment.adapter
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

    @Synchronized
    internal fun draw() {
        LogHelper.d(TAG, "draw START")
        LogHelper.d(TAG, "isShowing: ", isShowing)
        if (!isShowing || adapter == null) {
            return
        }
        activity?.runOnUiThread(Runnable {
            if (!isAdded) {
                return@Runnable
            }
            loading?.visibility = loadingVisibility
            adapter?.updateDataSet(currentItems)
            if (currentItems.isEmpty()) hideFab() else showFab()
        })
        LogHelper.d(TAG, "draw END")
    }

    override fun onStart() {
        LogHelper.d(TAG, "onStart START")
        super.onStart()
        MusicListObserver.addListener(this)
        MediaControllerObserver.addListener(this)
        adapter?.notifyDataSetChanged()
        LogHelper.d(TAG, "onStart END")
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        LogHelper.d(TAG, "setUserVisibleHint START")
        super.setUserVisibleHint(isVisibleToUser)
        if (isShowing && view != null) {
            draw()
        }
        LogHelper.d(TAG, "setUserVisibleHint END")
    }

    override fun onStop() {
        LogHelper.d(TAG, "onStop START")
        super.onStop()
        createListAsyncTask?.cancel(true)
        MediaControllerObserver.addListener(this)
        MusicListObserver.removeListener(this)
        LogHelper.d(TAG, "onStop END")
    }

    private class CreateListAsyncTask internal constructor(internal var mFragment: SongListFragment, internal val mSongList: List<MediaBrowserCompat.MediaItem>) : AsyncTask<Void, Void, Void>() {

        init {
            LogHelper.d(TAG, "CreateListAsyncTask")
        }

        override fun doInBackground(vararg params: Void): Void? {
            LogHelper.d(TAG, "CreateListAsyncTask.doInBackground START")
            mFragment.currentItems = createItemList(mSongList)
            if (mFragment.activity == null) {
                LogHelper.d(TAG, "CreateListAsyncTask.doInBackground SKIPPED")
                return null
            }
            mFragment.loadingVisibility = View.GONE
            mFragment.draw()
            LogHelper.d(TAG, "CreateListAsyncTask.doInBackground END")
            return null
        }

        @Synchronized
        private fun createItemList(songList: List<MediaBrowserCompat.MediaItem>): List<AbstractFlexibleItem<*>> {
            return mFragment.mediaBrowsable?.let { browser ->
                mFragment.activity?.let { activity ->
                    songList.map { song -> SongItem(song, browser, activity) }
                }
            } ?: emptyList()
        }

    }

    companion object {

        private val TAG = LogHelper.makeLogTag(SongListFragment::class.java)
    }

}
