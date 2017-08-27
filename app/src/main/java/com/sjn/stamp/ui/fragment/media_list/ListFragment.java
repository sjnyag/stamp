package com.sjn.stamp.ui.fragment.media_list;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener;
import com.sjn.stamp.R;
import com.sjn.stamp.ui.SongAdapter;
import com.sjn.stamp.ui.custom.CenteredMaterialSheetFab;
import com.sjn.stamp.ui.custom.Fab;
import com.sjn.stamp.ui.item.ProgressItem;
import com.sjn.stamp.ui.item.SongItem;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.SpotlightHelper;
import com.takusemba.spotlight.SimpleTarget;
import com.takusemba.spotlight.Spotlight;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public abstract class ListFragment extends FabFragment implements
        SwipeRefreshLayout.OnRefreshListener,
        FastScroller.OnScrollStateChangeListener,
        FlexibleAdapter.OnItemClickListener,
        FlexibleAdapter.OnItemLongClickListener,
        FlexibleAdapter.EndlessScrollListener,
        FlexibleAdapter.OnUpdateListener,
        StampEditStateObserver.Listener {

    private static final String TAG = LogHelper.makeLogTag(ListFragment.class);
    protected static final String LIST_STATE_KEY = "LIST_STATE_KEY";

    protected List<AbstractFlexibleItem> mItemList = new ArrayList<>();

    protected ProgressBar mLoading;
    protected View mEmptyView;
    protected TextView mEmptyTextView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected FastScroller mFastScroller;
    protected ProgressItem mProgressItem = new ProgressItem();

    protected FragmentInteractionListener mListener;
    protected Parcelable mListState;


    private final Handler mRefreshHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0: // Stop
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    return true;
                case 1: // Start
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                    return true;
                case 2: // Show empty view
                    if (mEmptyView != null) {
                        ViewCompat.animate(mEmptyView).alpha(1);
                    }
                    return true;
                default:
                    return false;
            }
        }
    });

    abstract public int getMenuResourceId();

    public interface FragmentInteractionListener {

        void onFragmentChange(SwipeRefreshLayout swipeRefreshLayout, RecyclerView recyclerView,
                              @SelectableAdapter.Mode int mode);

        void initSearchView(final Menu menu);

        void startActionModeByLongClick(int position);

        void destroyActionModeIfCan();

        void setToolbarTitle(CharSequence title);

        void updateContextTitle(int selectedItemCount);

        void restoreSelection();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        LogHelper.d(TAG, "setUserVisibleHint START");
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
        if (mIsVisibleToUser && getView() != null) {
            notifyFragmentChange();
        }
        LogHelper.d(TAG, "setUserVisibleHint END");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogHelper.d(TAG, "onCreate START");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        LogHelper.d(TAG, "onCreate END");
    }

    @Override
    public void onAttach(Context context) {
        LogHelper.d(TAG, "onAttach START");
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
        }
        LogHelper.d(TAG, "onAttach END");
    }

    @Override
    public void onDetach() {
        LogHelper.d(TAG, "onDetach START");
        super.onDetach();
        mListener = null;
        LogHelper.d(TAG, "onDetach END");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LogHelper.d(TAG, "onCreateOptionsMenu START");
        //menu.clear();
        inflater.inflate(getMenuResourceId(), menu);
        mListener.initSearchView(menu);
        LogHelper.d(TAG, "onCreateOptionsMenu END");
    }

    public void notifyFragmentChange() {
        LogHelper.d(TAG, "notifyFragmentChange START");
        mListener.onFragmentChange(mSwipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);
        LogHelper.d(TAG, "notifyFragmentChange END");
    }

    public List<AbstractFlexibleItem> getCurrentItems() {
        return mItemList;
    }


    public String emptyMessage() {
        return getString(R.string.no_items);
    }

    @Override
    public void onUpdateEmptyView(int size) {
        LogHelper.d(TAG, "onUpdateEmptyView START ", size);
        if (mEmptyTextView != null) {
            mEmptyTextView.setText(emptyMessage());
        }
        if (mFastScroller != null && mRefreshHandler != null && mEmptyView != null) {
            if (size > 0 || (mLoading != null && mLoading.getVisibility() == View.VISIBLE)) {
                mFastScroller.setVisibility(View.VISIBLE);
                mRefreshHandler.removeMessages(2);
                mEmptyView.setAlpha(0);
                mEmptyView.setVisibility(View.GONE);
            } else {
                mFastScroller.setVisibility(View.GONE);
                mRefreshHandler.sendEmptyMessage(2);
                mEmptyView.setAlpha(0);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }
//        if (mAdapter != null) {
//            String message = (mAdapter.hasSearchText() ? "Filtered " : "Refreshed ");
//            message += size + " items in " + mAdapter.getTime() + "ms";
//            Snackbar.make(getActivity().findViewById(R.id.main_view), message, Snackbar.LENGTH_SHORT).show();
//        }
        LogHelper.d(TAG, "onUpdateEmptyView END");
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        LogHelper.d(TAG, "onSaveInstanceState START");
        if (mRecyclerView != null && mRecyclerView.getLayoutManager() != null) {
            mListState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        }
        state.putParcelable(LIST_STATE_KEY, mListState);
        LogHelper.d(TAG, "onSaveInstanceState END");
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
}
