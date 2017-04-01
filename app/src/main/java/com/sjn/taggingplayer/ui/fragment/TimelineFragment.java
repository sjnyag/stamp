package com.sjn.taggingplayer.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.controller.SongHistoryController;
import com.sjn.taggingplayer.db.SongHistory;
import com.sjn.taggingplayer.ui.DialogFacade;
import com.sjn.taggingplayer.ui.adapter.SongHistoryAdapter;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.RealmHelper;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class TimelineFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {

    static final int PAGE_SIZE = 30;
    public static final int LOAD_START_THRESHOLD = 10;
    private static final String TAG = LogHelper.makeLogTag(TimelineFragment.class);

    private StickyListHeadersListView mListView;
    private SongHistoryAdapter mAdapter;
    private SongHistoryController mSongHistoryController;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    protected List<SongHistory> mShowingSongHistoryList = new ArrayList<>();
    protected List<SongHistory> mAllSongHistoryList = new ArrayList<>();
    private int mNextTimelinePage = 1;
    private int mRequestingPage = -1;
    private boolean mIsAllHistoryLoaded = false;
    private Realm mRealm;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.fragment_timeline, container, false);
        mSongHistoryController = new SongHistoryController(getContext());
        mAdapter = new SongHistoryAdapter(getActivity(), mShowingSongHistoryList);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);

        mListView = (StickyListHeadersListView) rootView.findViewById(R.id.timeline_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);

        mRealm = RealmHelper.getRealmInstance();
        mAllSongHistoryList = mSongHistoryController.getManagedTimeline(mRealm);
        loadListIfNeed();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onPause();
        mRealm.close();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount >= totalItemCount - LOAD_START_THRESHOLD) {
            loadListIfNeed();
        }

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
                if (mListView != null) {
                    mListView.setSelectionAfterHeaderView();
                }
                return false;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onRefresh() {
        if (mSwipeRefreshLayout == null || getActivity() == null || mSongHistoryController == null) {
            return;
        }
        mAllSongHistoryList = mSongHistoryController.getManagedTimeline(mRealm);
        resetLoadingTimeline();
        loadListIfNeed();
    }

    synchronized private void loadListIfNeed() {
        LogHelper.i(TAG, "loadListIfNeed");
        if (mListView == null || mIsAllHistoryLoaded) {
            return;
        }
        if (mRequestingPage != mNextTimelinePage) {
            mRequestingPage = mNextTimelinePage;
            drawTimeline(mNextTimelinePage);
        }
    }

    private void drawTimeline(int page) {
        if (mAdapter == null || mListView == null || getActivity() == null || isDetached()) {
            return;
        }
        mAdapter.setError(false);
        if (mAllSongHistoryList == null || mAllSongHistoryList.isEmpty()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                synchronized public void run() {
                    finishLoading();
                    mListView.setVisibility(View.GONE);
                    DialogFacade.createLetsPlayMusicDialog(getContext()).show();
                }
            });
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                synchronized public void run() {
                    int start = (mNextTimelinePage - 1) * PAGE_SIZE;
                    int end = mNextTimelinePage * PAGE_SIZE - 1;
                    if (end >= mAllSongHistoryList.size()) {
                        end = mAllSongHistoryList.size();
                    }
                    for (int i = start; i < end; i++) {
                        mShowingSongHistoryList.add(mAllSongHistoryList.get(i));
                    }
                    mAdapter.notifyDataSetChanged();
                    if (mNextTimelinePage * PAGE_SIZE - 1 != end) {
                        finishLoading();
                    }
                }
            });
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            synchronized public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        mNextTimelinePage = page + 1;
    }

    private void finishLoading() {
        mIsAllHistoryLoaded = true;
        mAdapter.setServerListSize(mShowingSongHistoryList.size());
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            synchronized public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void resetLoadingTimeline() {
        mIsAllHistoryLoaded = false;
        mShowingSongHistoryList.clear();
        mRequestingPage = -1;
        mNextTimelinePage = 1;
        if (mAdapter != null) {
            mAdapter.setServerListSize(-1);
        }
    }

}