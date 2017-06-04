package com.sjn.taggingplayer.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Iterables;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.controller.SongHistoryController;
import com.sjn.taggingplayer.db.SongHistory;
import com.sjn.taggingplayer.ui.adapter.SongHistoryAdapter;
import com.sjn.taggingplayer.ui.item.DateHeaderItem;
import com.sjn.taggingplayer.ui.item.ProgressItem;
import com.sjn.taggingplayer.ui.item.SongHistoryItem;
import com.sjn.taggingplayer.utils.CompatibleHelper;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.RealmHelper;
import com.sjn.taggingplayer.utils.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.helpers.ActionModeHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import io.realm.Realm;

import static eu.davidea.flexibleadapter.SelectableAdapter.MODE_MULTI;

public class TimelineFragment extends MediaControllerFragment implements SwipeRefreshLayout.OnRefreshListener,
        FastScroller.OnScrollStateChangeListener, FlexibleAdapter.OnItemLongClickListener,
        FlexibleAdapter.EndlessScrollListener {

    private static final String TAG = LogHelper.makeLogTag(TimelineFragment.class);

    @Override
    public void onItemLongClick(int position) {
        mListener.startActionModeByLongClick(position);
    }

    private RecyclerView mRecyclerView;
    private SongHistoryAdapter mAdapter;
    private SongHistoryController mSongHistoryController;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    protected List<SongHistory> mAllSongHistoryList = new ArrayList<>();
    private Realm mRealm;
    private FloatingActionButton mFab;
    private ProgressItem mProgressItem = new ProgressItem();


    protected LinearLayoutManager createNewLinearLayoutManager() {
        return new SmoothScrollLinearLayoutManager(getActivity());
    }

    public static SongHistoryItem newSimpleItem(SongHistory songHistory, IHeader header) {
        SongHistoryItem item = new SongHistoryItem(songHistory, (DateHeaderItem) header);
        item.setTitle(songHistory.getSong().getTitle());
        return item;
    }

    public static DateHeaderItem newHeader(SongHistory songHistory) {
        return new DateHeaderItem(songHistory.getRecordedAt());
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
        mAdapter = new SongHistoryAdapter(getItemList(0, 30), this);
        mAdapter.setNotifyChangeOfUnfilteredItems(true)
                .setAnimationOnScrolling(false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
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

        return rootView;
    }


    @Override
    public void onFastScrollerStateChange(boolean scrolling) {
        if (scrolling) {
            hideFab();
        } else {
            showFab();
        }
    }

    private void hideFab() {
        if (mFab == null) {
            return;
        }
        ViewCompat.animate(mFab)
                .scaleX(0f).scaleY(0f)
                .alpha(0f).setDuration(100)
                .start();
    }

    private void showFab() {
        if (mFab == null) {
            return;
        }
        ViewCompat.animate(mFab)
                .scaleX(1f).scaleY(1f)
                .alpha(1f).setDuration(200)
                .setStartDelay(300L)
                .start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.timeline, menu);
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

    private String extractTitleFrom(IFlexible flexibleItem) {
        return "";
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

    @Override
    public void onConnected() {

    }

    @Override
    public List<AbstractFlexibleItem> getCurrentMediaItems() {
        return null;
    }

    @Override
    public int getMenuResourceId() {
        return 0;
    }

    @Override
    public String getMediaId() {
        return null;
    }

    @Override
    public void notifyFragmentChange() {

    }
}