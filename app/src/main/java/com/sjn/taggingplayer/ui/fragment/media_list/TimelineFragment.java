package com.sjn.taggingplayer.ui.fragment.media_list;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Iterables;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.controller.SongHistoryController;
import com.sjn.taggingplayer.db.SongHistory;
import com.sjn.taggingplayer.ui.SongAdapter;
import com.sjn.taggingplayer.ui.item.DateHeaderItem;
import com.sjn.taggingplayer.ui.item.SongHistoryItem;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.RealmHelper;
import com.sjn.taggingplayer.utils.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IHeader;
import io.realm.Realm;

public class TimelineFragment extends MediaBrowserListFragment {

    private static final String TAG = LogHelper.makeLogTag(TimelineFragment.class);


    private SongAdapter mAdapter;
    private SongHistoryController mSongHistoryController;
    protected List<SongHistory> mAllSongHistoryList = new ArrayList<>();
    private Realm mRealm;

    /**
     * {@link ListFragment}
     */
    @Override
    public int getMenuResourceId() {
        return R.menu.timeline;
    }

    /**
     * {@link MediaBrowserListFragment}
     */
    @Override
    public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {

    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {

    }

    @Override
    void onMediaBrowserChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {

    }

    @Override
    void onMediaBrowserError(@NonNull String parentId) {

    }

    /**
     * {@link SwipeRefreshLayout.OnRefreshListener}
     */
    @Override
    public void onRefresh() {
        if (mSwipeRefreshLayout == null || getActivity() == null || mSongHistoryController == null || mListener == null) {
            return;
        }
        mListener.destroyActionModeIfCan();
        mAllSongHistoryList = mSongHistoryController.getManagedTimeline(mRealm);
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.updateDataSet(getItemList(0, 30));
    }

    /**
     * {@link FastScroller.OnScrollStateChangeListener}
     */
    @Override
    public void onFastScrollerStateChange(boolean scrolling) {
        if (scrolling) {
            hideFab();
        } else {
            showFab();
        }
    }

    /**
     * {@link FlexibleAdapter.OnItemClickListener}
     */
    @Override
    public boolean onItemClick(int position) {
        LogHelper.d(TAG, "onItemClick ");
        SongHistoryItem item = (SongHistoryItem) mAdapter.getItem(position);
        mMediaBrowsable.onMediaItemSelected(item.getSongHistory().getSong().getMediaId());
        return false;
    }

    /**
     * {@link FlexibleAdapter.OnItemLongClickListener}
     */
    @Override
    public void onItemLongClick(int position) {
        mListener.startActionModeByLongClick(position);
    }

    /**
     * {@link FlexibleAdapter.EndlessScrollListener}
     */
    @Override
    public void noMoreLoad(int newItemsSize) {
        Log.d(TAG, "newItemsSize=" + newItemsSize);
        Log.d(TAG, "Total pages loaded=" + mAdapter.getEndlessCurrentPage());
        Log.d(TAG, "Total items loaded=" + mAdapter.getMainItemCount());

    }

    @Override
    public void onLoadMore(int lastPosition, int currentPage) {
        mAdapter.onLoadMoreComplete(getItemList(mAdapter.getMainItemCount() - mAdapter.getHeaderItems().size(), 30), 5000L);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.fragment_timeline, container, false);
        mSongHistoryController = new SongHistoryController(getContext());

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);

        mRealm = RealmHelper.getRealmInstance();
        mAllSongHistoryList = mSongHistoryController.getManagedTimeline(mRealm);
        mAdapter = new SongAdapter(getItemList(0, 30), this);
        mAdapter.setNotifyChangeOfUnfilteredItems(true)
                .setAnimationOnScrolling(false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(getActivity()));
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
        mAdapter.showLayoutInfo(savedInstanceState == null);
        mAdapter.addScrollableFooter();


        // EndlessScrollListener - OnLoadMore (v5.0.0)
        mAdapter//.setLoadingMoreAtStartUp(true) //To call only if the list is empty
                //.setEndlessPageSize(3) //Endless is automatically disabled if newItems < 3
                //.setEndlessTargetCount(15) //Endless is automatically disabled if totalItems >= 15
                //.setEndlessScrollThreshold(1); //Default=1
                .setEndlessScrollListener(this, mProgressItem);
        notifyFragmentChange();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upward:
                if (mRecyclerView != null) {
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.scrollToPosition(calcGoToTopBufferedPosition(15));
                            mRecyclerView.smoothScrollToPosition(0);
                        }
                    });
                }
                return false;
            default:
                break;
        }
        return false;
    }

    private int calcGoToTopBufferedPosition(int bufferSize) {
        int position = calcCurrentPosition();
        if (position > bufferSize) {
            position = bufferSize;
        }
        return position;
    }

    private int calcCurrentPosition() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) mRecyclerView.getLayoutManager());
        return layoutManager.findFirstVisibleItemPosition();
    }

    public static DateHeaderItem newHeader(SongHistory songHistory) {
        return new DateHeaderItem(songHistory.getRecordedAt());
    }

    public static SongHistoryItem newSimpleItem(SongHistory songHistory, IHeader header) {
        SongHistoryItem item = new SongHistoryItem(songHistory, (DateHeaderItem) header);
        item.setTitle(songHistory.getSong().getTitle());
        return item;
    }

    private List<AbstractFlexibleItem> getItemList(int startPosition, int size) {
        int end = startPosition + size;
        if (end >= mAllSongHistoryList.size()) {
            end = mAllSongHistoryList.size();
        }
        List<AbstractFlexibleItem> headerItemList = new ArrayList<>();
        DateHeaderItem header = mAdapter == null ? null : (DateHeaderItem) Iterables.getLast(mAdapter.getHeaderItems());

        for (int i = startPosition; i < mAllSongHistoryList.size(); i++) {
            if (header == null || !header.isDateOf(mAllSongHistoryList.get(i).getRecordedAt())) {
                if (i >= end) {
                    break;
                }
                header = newHeader(mAllSongHistoryList.get(i));
            }
            headerItemList.add(newSimpleItem(mAllSongHistoryList.get(i), header));
        }
        return headerItemList;
    }
}