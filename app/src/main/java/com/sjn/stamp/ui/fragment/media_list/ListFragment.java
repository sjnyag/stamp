package com.sjn.stamp.ui.fragment.media_list;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sjn.stamp.R;
import com.sjn.stamp.ui.item.ProgressItem;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.utils.LogHelper;

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

    private static final String TAG = LogHelper.INSTANCE.makeLogTag(ListFragment.class);
    protected static final String LIST_STATE_KEY = "LIST_STATE_KEY";

    protected List<AbstractFlexibleItem<?>> mItemList = new ArrayList<>();

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
        LogHelper.INSTANCE.d(TAG, "setUserVisibleHint START");
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
        if (mIsVisibleToUser && getView() != null) {
            notifyFragmentChange();
        }
        LogHelper.INSTANCE.d(TAG, "setUserVisibleHint END");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogHelper.INSTANCE.d(TAG, "onCreate START");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        LogHelper.INSTANCE.d(TAG, "onCreate END");
    }

    @Override
    public void onAttach(Context context) {
        LogHelper.INSTANCE.d(TAG, "onAttach START");
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
        }
        LogHelper.INSTANCE.d(TAG, "onAttach END");
    }

    @Override
    public void onDetach() {
        LogHelper.INSTANCE.d(TAG, "onDetach START");
        super.onDetach();
        mListener = null;
        LogHelper.INSTANCE.d(TAG, "onDetach END");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LogHelper.INSTANCE.d(TAG, "onCreateOptionsMenu START");
        //menu.clear();
        inflater.inflate(getMenuResourceId(), menu);
        mListener.initSearchView(menu);
        LogHelper.INSTANCE.d(TAG, "onCreateOptionsMenu END");
    }

    public void notifyFragmentChange() {
        LogHelper.INSTANCE.d(TAG, "notifyFragmentChange START");
        mListener.onFragmentChange(mSwipeRefreshLayout, mRecyclerView, SelectableAdapter.Mode.IDLE);
        LogHelper.INSTANCE.d(TAG, "notifyFragmentChange END");
    }

    public List<AbstractFlexibleItem<?>> getCurrentItems() {
        return mItemList;
    }


    public String emptyMessage() {
        return getString(R.string.no_items);
    }

    @Override
    public void onUpdateEmptyView(int size) {
        LogHelper.INSTANCE.d(TAG, "onUpdateEmptyView START ", size);
        if (mEmptyTextView != null) {
            mEmptyTextView.setText(emptyMessage());
        }
        if (mFastScroller != null && mEmptyView != null) {
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
        LogHelper.INSTANCE.d(TAG, "onUpdateEmptyView END");
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        LogHelper.INSTANCE.d(TAG, "onSaveInstanceState START");
        if (mRecyclerView != null && mRecyclerView.getLayoutManager() != null) {
            mListState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        }
        state.putParcelable(LIST_STATE_KEY, mListState);
        LogHelper.INSTANCE.d(TAG, "onSaveInstanceState END");
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
