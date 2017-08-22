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

public abstract class ListFragment extends Fragment implements
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
    protected RecyclerView mRecyclerView;
    protected SongAdapter mAdapter;

    protected ProgressBar mLoading;
    protected View mEmptyView;
    protected TextView mEmptyTextView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected FastScroller mFastScroller;
    protected Fab mFab;
    protected CenteredMaterialSheetFab mCenteredMaterialSheetFab;
    protected ProgressItem mProgressItem = new ProgressItem();

    protected FragmentInteractionListener mListener;
    protected boolean mIsVisibleToUser = true;
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

    View.OnClickListener startStampEdit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            StampEditStateObserver.getInstance().notifyStateChange(StampEditStateObserver.State.EDITING);
        }
    };

    View.OnClickListener stopStampEdit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            StampEditStateObserver.getInstance().notifyStateChange(StampEditStateObserver.State.NO_EDIT);
        }
    };

    private void openStampEdit() {
        if (!mCenteredMaterialSheetFab.isSheetVisible()) {
            mCenteredMaterialSheetFab.showSheet();
        }
        mFab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        mFab.setImageResource(R.drawable.ic_full_cancel);
        mFab.setOnClickListener(stopStampEdit);
    }


    private void closeStampEdit() {
        if (mCenteredMaterialSheetFab.isSheetVisible()) {
            mCenteredMaterialSheetFab.hideSheet();
        }
        mFab.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        mFab.setImageResource(R.drawable.ic_stamp);
        mFab.setOnClickListener(startStampEdit);
    }

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

    protected void hideFab() {
        if (mFab == null) {
            return;
        }
        ViewCompat.animate(mFab)
                .scaleX(0f).scaleY(0f)
                .alpha(0f).setDuration(100)
                .start();
    }

    protected void showFab() {
        if (mFab == null) {
            return;
        }
        ViewCompat.animate(mFab)
                .scaleX(1f).scaleY(1f)
                .alpha(1f).setDuration(200)
                .setStartDelay(300L)
                .start();
    }

    protected void initializeFab(int resourceId, ColorStateList color, View.OnClickListener onClickListener) {
        mFab = getActivity().findViewById(R.id.fab);
        mFab.setVisibility(View.VISIBLE);
        if (Integer.valueOf(resourceId).equals(mFab.getTag(R.id.fab_type))) {
            return;
        }
        StampEditStateObserver.getInstance().notifyStateChange(StampEditStateObserver.State.NO_EDIT);
        mFab.setTag(R.id.fab_type, resourceId);
        mFab.setImageResource(resourceId);
        mFab.setBackgroundTintList(color);
        mFab.setOnClickListener(onClickListener);
        ViewCompat.animate(mFab)
                .scaleX(1f).scaleY(1f)
                .alpha(1f).setDuration(100)
                .setStartDelay(300L)
                .start();
    }

    protected void initializeFabWithStamp() {
        initializeFab(R.drawable.ic_stamp, ColorStateList.valueOf(Color.WHITE), startStampEdit);
        View sheetView = getActivity().findViewById(R.id.fab_sheet);
        sheetView.setVisibility(View.VISIBLE);
        View overlay = getActivity().findViewById(R.id.overlay);
        overlay.setVisibility(View.VISIBLE);
        int sheetColor = ContextCompat.getColor(getActivity(), R.color.background);
        int fabColor = ContextCompat.getColor(getActivity(), R.color.fab_color);
        mCenteredMaterialSheetFab = new CenteredMaterialSheetFab<>(mFab, sheetView, overlay, sheetColor, fabColor);
        mCenteredMaterialSheetFab.setEventListener(new MaterialSheetFabEventListener() {
            @Override
            public void onShowSheet() {
            }

            @Override
            public void onSheetShown() {
                StampEditStateObserver.getInstance().notifyStateChange(StampEditStateObserver.State.EDITING);
            }

            @Override
            public void onHideSheet() {
            }

            public void onSheetHidden() {
                StampEditStateObserver.State state = StampEditStateObserver.State.STAMPING;
                if (StampEditStateObserver.getInstance().getSelectedStampList() == null || StampEditStateObserver.getInstance().getSelectedStampList().isEmpty()) {
                    state = StampEditStateObserver.State.NO_EDIT;
                }
                StampEditStateObserver.getInstance().notifyStateChange(state);
            }
        });
    }

    public void performFabAction() {
        //default implementation does nothing
    }

    @Override
    public void onStart() {
        LogHelper.d(TAG, "onStart START");
        super.onStart();
        StampEditStateObserver.getInstance().addListener(this);
        LogHelper.d(TAG, "onStart END");
    }

    @Override
    public void onStop() {
        LogHelper.d(TAG, "onStop START");
        super.onStop();
        StampEditStateObserver.getInstance().removeListener(this);
        LogHelper.d(TAG, "onStop END");
    }

    @Override
    public void onSelectedStampChange(List<String> selectedStampList) {
    }

    @Override
    public void onNewStampCreated(String stamp) {

    }

    /**
     * {@link StampEditStateObserver.Listener}
     */
    @Override
    public void onStampStateChange(StampEditStateObserver.State state) {
        LogHelper.d(TAG, "onStampStateChange: ", state);
        switch (state) {
            case EDITING:
                openStampEdit();
                break;
            case NO_EDIT:
                closeStampEdit();
                break;
            case STAMPING:
                if (mCenteredMaterialSheetFab.isSheetVisible()) {
                    mCenteredMaterialSheetFab.hideSheet();
                }
                if (mIsVisibleToUser && !SpotlightHelper.isShown(getActivity(), SpotlightHelper.KEY_STAMP_ADD)) {
                    showSpotlight();
                }
                break;
        }
        mAdapter.notifyDataSetChanged();
    }

    private void showSpotlight() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) mRecyclerView.getLayoutManager());
        RecyclerView.ViewHolder view = mRecyclerView.findViewHolderForAdapterPosition(layoutManager.findFirstVisibleItemPosition());
        if (view != null && view instanceof SongItem.SimpleViewHolder) {
            View addStampView = ((SongItem.SimpleViewHolder) view).getShowTapTargetView();
            if (addStampView != null) {
                Spotlight.with(getActivity())
                        .setDuration(200L)
                        .setAnimation(new DecelerateInterpolator(2f))
                        .setTargets(new SimpleTarget.Builder(getActivity())
                                .setPoint(addStampView)
                                .setRadius(120f)
                                .setTitle(getString(R.string.spotlight_stamp_add_title))
                                .setDescription(getString(R.string.spotlight_stamp_add_description))
                                .build())
                        .start();
                SpotlightHelper.setShown(getActivity(), SpotlightHelper.KEY_STAMP_ADD);
            }
        }
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
