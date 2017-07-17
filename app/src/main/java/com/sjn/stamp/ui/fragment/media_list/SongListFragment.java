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
package com.sjn.stamp.ui.fragment.media_list;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sjn.stamp.MusicService;
import com.sjn.stamp.R;
import com.sjn.stamp.ui.DialogFacade;
import com.sjn.stamp.ui.SongAdapter;
import com.sjn.stamp.ui.item.SongItem;
import com.sjn.stamp.ui.observer.MusicListObserver;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

import static com.sjn.stamp.MusicService.CUSTOM_ACTION_RELOAD_MUSIC_PROVIDER;

/**
 * A Fragment that lists all the various browsable queues available
 * from a {@link android.service.media.MediaBrowserService}.
 * <p/>
 * It uses a {@link MediaBrowserCompat} to connect to the {@link MusicService}.
 * Once connected, the fragment subscribes to get all the children.
 * All {@link MediaBrowserCompat.MediaItem}'s that can be browsed are shown in a ListView.
 */
public class SongListFragment extends MediaBrowserListFragment implements MusicListObserver.Listener {

    private ProgressDialog mProgressDialog;
    protected List<MediaBrowserCompat.MediaItem> mSongList = new ArrayList<>();
    private static final String TAG = LogHelper.makeLogTag(SongListFragment.class);

    /**
     * {@link ListFragment}
     */
    @Override
    public int getMenuResourceId() {
        return R.menu.main;
    }

    /**
     * {@link MediaBrowserListFragment}
     */
    @Override
    public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
        if (mAdapter == null) {
            return;
        }
        LogHelper.d(TAG, "onPlaybackStateChanged ");
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        if (metadata == null || mAdapter == null) {
            return;
        }
        LogHelper.d(TAG, "Received metadata change to media ", metadata.getDescription().getMediaId());
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMediaBrowserChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
        try {
            LogHelper.d(TAG, "fragment onChildrenLoaded, parentId=" + parentId + "  count=" + children.size());
            mSongList = children;
            draw();
        } catch (Throwable t) {
            LogHelper.e(TAG, "Error on childrenloaded", t);
        }
    }

    @Override
    public void onMediaBrowserError(@NonNull String id) {
        LogHelper.e(TAG, "browse fragment subscription onError, id=" + id);
        //Toast.makeText(getActivity(), R.string.error_loading_media, Toast.LENGTH_LONG).show();
    }

    /**
     * {@link SwipeRefreshLayout.OnRefreshListener}
     */
    @Override
    public void onRefresh() {
        if (mSwipeRefreshLayout == null || getActivity() == null) {
            return;
        }
        DialogFacade.createRetrieveMediaDialog(getActivity(), new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                switch (which) {
                    case NEGATIVE:
                        mSwipeRefreshLayout.setRefreshing(false);
                        return;
                    case POSITIVE:
                        mListener.destroyActionModeIfCan();
                        mMediaBrowsable.sendCustomAction(CUSTOM_ACTION_RELOAD_MUSIC_PROVIDER, null, null);
                        Handler handler = new Handler(getActivity().getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                mProgressDialog = new ProgressDialog(getActivity());
                                mProgressDialog.setMessage("処理中");
                                mProgressDialog.show();
                            }
                        });
                        break;
                }
            }
        }).show();
    }

    /**
     * {@link FlexibleAdapter.OnItemClickListener}
     */
    @Override
    public boolean onItemClick(int position) {
        LogHelper.d(TAG, "onItemClick ");
        AbstractFlexibleItem item = mAdapter.getItem(position);
        if (item instanceof SongItem) {
            mMediaBrowsable.onMediaItemSelected(((SongItem) item).getMediaItem());
        }
        return false;
    }

    /**
     * {@link FlexibleAdapter.OnItemLongClickListener}
     */
    @Override
    public void onItemLongClick(int position) {
        if (mListener == null) {
            return;
        }
        mListener.startActionModeByLongClick(position);
    }

    /**
     * {@link FlexibleAdapter.EndlessScrollListener}
     */
    @Override
    public void noMoreLoad(int newItemsSize) {
        LogHelper.d(TAG, "newItemsSize=" + newItemsSize);
        LogHelper.d(TAG, "Total pages loaded=" + mAdapter.getEndlessCurrentPage());
        LogHelper.d(TAG, "Total items loaded=" + mAdapter.getMainItemCount());

    }

    @Override
    public void onLoadMore(int lastPosition, int currentPage) {
        //mAdapter.onLoadMoreComplete(getItemList(mAdapter.getMainItemCount() - mAdapter.getHeaderItems().size(), 30), 5000L);
    }

    /**
     * {@link MusicListObserver.Listener}
     */
    @Override
    public void onMediaListUpdated() {
        LogHelper.d(TAG, "onMediaListUpdated START");
        MediaBrowserCompat mediaBrowser = mMediaBrowsable.getMediaBrowser();
        if (mediaBrowser != null && mediaBrowser.isConnected() && mMediaId != null) {
            reloadList();
            Handler handler = new Handler(getActivity().getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                }
            });
        }
        LogHelper.d(TAG, "onMediaListUpdated END");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogHelper.d(TAG, "onCreateView START" + getMediaId());
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        mEmptyView = rootView.findViewById(R.id.empty_view);
        mFastScroller = (FastScroller) rootView.findViewById(R.id.fast_scroller);
        mEmptyTextView = (TextView) rootView.findViewById(R.id.empty_text);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);

        mAdapter = new SongAdapter(mItemList, this);
        mAdapter.setNotifyChangeOfUnfilteredItems(true)
                .setAnimationOnScrolling(false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }
        RecyclerView.LayoutManager layoutManager = new SmoothScrollLinearLayoutManager(getActivity());
        if (mListState != null) {
            layoutManager.onRestoreInstanceState(mListState);
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        //mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter.setFastScroller((FastScroller) rootView.findViewById(R.id.fast_scroller),
                ViewHelper.getColorAccent(getActivity()), this);

        mAdapter.setLongPressDragEnabled(true)
                .setHandleDragEnabled(true)
                .setSwipeEnabled(true)
                .setUnlinkAllItemsOnRemoveHeaders(false)
                .setDisplayHeadersAtStartUp(false)
                .setStickyHeaders(true)
                .showAllHeaders();
        mAdapter.addUserLearnedSelection(savedInstanceState == null);
        //mAdapter.addScrollableHeaderWithDelay(new DateHeaderItem(TimeHelper.getJapanNow().toDate()), 900L, false);
        //mAdapter.showLayoutInfo(savedInstanceState == null);
//        mAdapter.addScrollableFooter();


        // EndlessScrollListener - OnLoadMore (v5.0.0)
//        mAdapter//.setLoadingMoreAtStartUp(true) //To call only if the list is empty
        //.setEndlessPageSize(3) //Endless is automatically disabled if newItems < 3
        //.setEndlessTargetCount(15) //Endless is automatically disabled if totalItems >= 15
        //.setEndlessScrollThreshold(1); //Default=1
//                .setEndlessScrollListener(this, mProgressItem);

        /*
        mBrowserAdapter = new BrowseAdapter(getActivity());

        ListView listView = (ListView) rootView.findViewById(R.id.list_view);
        listView.setAdapter(mBrowserAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogHelper.d(TAG, "onItemClick ");
                checkForUserVisibleErrors(false);
                MediaBrowserCompat.MediaItem item = mBrowserAdapter.getItem(position);
                mMediaBrowsable.onMediaItemSelected(item);
            }
        });
        */
        initializeFabWithStamp();
        if (mIsVisibleToUser) {
            notifyFragmentChange();
        }
        if (mItemList == null || mItemList.isEmpty()) {
            draw();
        }
        LogHelper.d(TAG, "onCreateView END");
        return rootView;
    }

    synchronized void draw() {
        LogHelper.d(TAG, "draw START");
        if (mSongList == null || mAdapter == null) {
            return;
        }
        mItemList = createItemList();
        mAdapter.updateDataSet(mItemList);
        LogHelper.d(TAG, "draw END");
    }

    private List<AbstractFlexibleItem> createItemList() {
        LogHelper.d(TAG, "createItemList START");
        List<AbstractFlexibleItem> itemList = new ArrayList<>();
        for (MediaBrowserCompat.MediaItem item : mSongList) {
            AbstractFlexibleItem songItem = new SongItem(item);
            itemList.add(songItem);
        }
        LogHelper.d(TAG, "createItemList END");
        return itemList;
    }

    @Override
    public void onStart() {
        LogHelper.d(TAG, "onStart START");
        super.onStart();
        MusicListObserver.getInstance().addListener(this);
        LogHelper.d(TAG, "onStart END");
    }

    @Override
    public void onStop() {
        LogHelper.d(TAG, "onStop START");
        super.onStop();
        MusicListObserver.getInstance().removeListener(this);
        LogHelper.d(TAG, "onStop END");
    }
}
