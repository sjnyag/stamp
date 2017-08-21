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

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sjn.stamp.MusicService;
import com.sjn.stamp.R;
import com.sjn.stamp.media.provider.ProviderType;
import com.sjn.stamp.ui.DialogFacade;
import com.sjn.stamp.ui.SongAdapter;
import com.sjn.stamp.ui.item.SongItem;
import com.sjn.stamp.ui.observer.MusicListObserver;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * A Fragment that lists all the various browsable queues available
 * from a {@link android.service.media.MediaBrowserService}.
 * <p/>
 * It uses a {@link MediaBrowserCompat} to connect to the {@link MusicService}.
 * Once connected, the fragment subscribes to get all the children.
 * All {@link MediaBrowserCompat.MediaItem}'s that can be browsed are shown in a ListView.
 */
public class SongListFragment extends MediaBrowserListFragment implements MusicListObserver.Listener {

    private static final String TAG = LogHelper.makeLogTag(SongListFragment.class);
    private CreateListAsyncTask mCreateListAsyncTask;
    private int mLoadingVisibility = View.VISIBLE;

    /**
     * {@link ListFragment}
     */
    @Override
    public int getMenuResourceId() {
        return R.menu.song_list;
    }

    @Override
    public String emptyMessage() {
        String mediaId = getMediaId();
        if (mediaId == null || mediaId.isEmpty()) {
            return super.emptyMessage();
        }
        ProviderType providerType = MediaIDHelper.getProviderType(mediaId);
        if (providerType == null) {
            return super.emptyMessage();
        }

        return providerType.getEmptyMessage(getResources());
    }


    /**
     * {@link MediaBrowserListFragment}
     */
    @Override
    public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMediaBrowserChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
        LogHelper.d(TAG, "onMediaBrowserChildrenLoaded");
        if (mCreateListAsyncTask != null) {
            mCreateListAsyncTask.cancel(true);
        }
        mCreateListAsyncTask = new CreateListAsyncTask(this, children);
        mCreateListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onMediaBrowserError(@NonNull String id) {
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
                        mMediaBrowsable.sendCustomAction(MusicService.Companion.getCUSTOM_ACTION_RELOAD_MUSIC_PROVIDER(), null, null);
                        return;
                    default:
                        mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        }, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mSwipeRefreshLayout.setRefreshing(false);
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
            mMediaBrowsable.onMediaItemSelected(((SongItem) item).getMediaId(), ((SongItem) item).isPlayable(), ((SongItem) item).isBrowsable());
        }
        return false;
    }

    /**
     * {@link FlexibleAdapter.OnItemLongClickListener}
     */
    @Override
    public void onItemLongClick(int position) {
        AbstractFlexibleItem item = mAdapter.getItem(position);
        if (!(item instanceof SongItem)) {
            return;
        }
        if (!((SongItem) item).isPlayable()) {
            mMediaBrowsable.playByCategory(((SongItem) item).getMediaId());
        } else {
            onItemClick(position);
        }
    }

    /**
     * {@link FlexibleAdapter.EndlessScrollListener}
     */
    @Override
    public void noMoreLoad(int newItemsSize) {
    }

    @Override
    public void onLoadMore(int lastPosition, int currentPage) {
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
                }
            });
        }
        LogHelper.d(TAG, "onMediaListUpdated END");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogHelper.d(TAG, "onCreateView START" + getMediaId());
        final View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        mLoading = rootView.findViewById(R.id.progressBar);
        mEmptyView = rootView.findViewById(R.id.empty_view);
        mFastScroller = rootView.findViewById(R.id.fast_scroller);
        mEmptyTextView = rootView.findViewById(R.id.empty_text);

        mSwipeRefreshLayout = rootView.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);

        mAdapter = new SongAdapter(mItemList, this);
        mAdapter.setNotifyChangeOfUnfilteredItems(true)
                .setAnimationOnScrolling(false);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }
        RecyclerView.LayoutManager layoutManager = new SmoothScrollLinearLayoutManager(getActivity());
        if (mListState != null) {
            layoutManager.onRestoreInstanceState(mListState);
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setFastScroller((FastScroller) rootView.findViewById(R.id.fast_scroller),
                ViewHelper.getColorAccent(getActivity()), this);
        mAdapter.setLongPressDragEnabled(false)
                .setHandleDragEnabled(false)
                .setSwipeEnabled(false)
                .setUnlinkAllItemsOnRemoveHeaders(false)
                .setDisplayHeadersAtStartUp(false)
                .setStickyHeaders(false)
                .showAllHeaders();
        mAdapter.addUserLearnedSelection(savedInstanceState == null);
        initializeFabWithStamp();
        if (mIsVisibleToUser) {
            notifyFragmentChange();
        }
        draw();
        LogHelper.d(TAG, "onCreateView END");
        return rootView;
    }

    synchronized void draw() {
        LogHelper.d(TAG, "draw START");
        LogHelper.d(TAG, "mIsVisibleToUser: ", mIsVisibleToUser);
        if (!mIsVisibleToUser || mAdapter == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            synchronized public void run() {
                if (!isAdded()) {
                    return;
                }
                if (mLoading != null) {
                    mLoading.setVisibility(mLoadingVisibility);
                }
                mAdapter.updateDataSet(mItemList);
                if (mItemList.isEmpty()) {
                    hideFab();
                } else {
                    showFab();
                }
            }
        });
        LogHelper.d(TAG, "draw END");
    }

    @Override
    public void onStart() {
        LogHelper.d(TAG, "onStart START");
        super.onStart();
        MusicListObserver.getInstance().addListener(this);
        LogHelper.d(TAG, "onStart END");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        LogHelper.d(TAG, "setUserVisibleHint START");
        super.setUserVisibleHint(isVisibleToUser);
        if (mIsVisibleToUser && getView() != null) {
            draw();
        }
        LogHelper.d(TAG, "setUserVisibleHint END");
    }

    @Override
    public void onStop() {
        LogHelper.d(TAG, "onStop START");
        super.onStop();
        if (mCreateListAsyncTask != null) {
            mCreateListAsyncTask.cancel(true);
        }
        MusicListObserver.getInstance().removeListener(this);
        LogHelper.d(TAG, "onStop END");
    }

    private static class CreateListAsyncTask extends AsyncTask<Void, Void, Void> {

        SongListFragment mFragment;
        final List<MediaBrowserCompat.MediaItem> mSongList;

        CreateListAsyncTask(SongListFragment fragment, List<MediaBrowserCompat.MediaItem> songList) {
            LogHelper.d(TAG, "CreateListAsyncTask");
            this.mFragment = fragment;
            this.mSongList = songList;
        }

        @Override
        protected Void doInBackground(Void... params) {
            LogHelper.d(TAG, "CreateListAsyncTask.doInBackground START");
            mFragment.mItemList = createItemList(mSongList);
            if (mFragment.getActivity() == null) {
                LogHelper.d(TAG, "CreateListAsyncTask.doInBackground SKIPPED");
                return null;
            }
            mFragment.mLoadingVisibility = View.GONE;
            mFragment.draw();
            LogHelper.d(TAG, "CreateListAsyncTask.doInBackground END");
            return null;
        }

        synchronized private List<AbstractFlexibleItem> createItemList(List<MediaBrowserCompat.MediaItem> songList) {
            LogHelper.d(TAG, "createItemList START");
            List<AbstractFlexibleItem> itemList = new ArrayList<>();
            for (MediaBrowserCompat.MediaItem item : songList) {
                AbstractFlexibleItem songItem = new SongItem(item, mFragment.mMediaBrowsable, mFragment.getActivity());
                itemList.add(songItem);
            }
            LogHelper.d(TAG, "createItemList END");
            return itemList;
        }

    }

}
