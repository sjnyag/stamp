package com.sjn.stamp.ui.fragment.media_list;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.bowyer.app.fabtransitionlayout.BottomSheetLayout;
import com.sjn.stamp.ui.item.ProgressItem;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.R;
import com.sjn.stamp.utils.LogHelper;

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
        StampEditStateObserver.Listener {

    private static final String TAG = LogHelper.makeLogTag(ListFragment.class);


    protected List<AbstractFlexibleItem> mItemList = new ArrayList<>();
    protected RecyclerView mRecyclerView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected ProgressItem mProgressItem = new ProgressItem();
    protected FragmentInteractionListener mListener;
    protected FloatingActionButton mFab;
    protected BottomSheetLayout mBottomSheetLayout;
    protected boolean mIsVisibleToUser = false;

    View.OnClickListener startStampEdit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            StampEditStateObserver.getInstance().notifyStateChange(StampEditStateObserver.State.OPEN);
        }
    };

    View.OnClickListener stopStampEdit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            StampEditStateObserver.getInstance().notifyStateChange(StampEditStateObserver.State.CLOSE);
        }
    };

    private void openStampEdit() {
        if (!mBottomSheetLayout.isFabExpanded()) {
            mBottomSheetLayout.expandFab();
        }
        mFab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        mFab.setImageResource(R.drawable.ic_full_cancel);
        mFab.setOnClickListener(stopStampEdit);
    }


    private void closeStampEdit() {
        if (mBottomSheetLayout.isFabExpanded()) {
            mBottomSheetLayout.contractFab();
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
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
        if (mIsVisibleToUser && getView() != null) {
            notifyFragmentChange();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.v(TAG, "onCreateOptionsMenu called!");
        menu.clear();
        inflater.inflate(getMenuResourceId(), menu);
        mListener.initSearchView(menu);
    }

    public void notifyFragmentChange() {
        mListener.onFragmentChange(mSwipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);
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
        mBottomSheetLayout = (BottomSheetLayout) getActivity().findViewById(R.id.bottom_sheet);
        mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (Integer.valueOf(resourceId).equals(mFab.getTag(R.id.fab_type))) {
            return;
        }
        mFab.setTag(R.id.fab_type, resourceId);
        mFab.setImageResource(resourceId);
        mFab.setBackgroundTintList(color);
        mFab.setOnClickListener(onClickListener);
        ViewCompat.animate(mFab)
                .scaleX(1f).scaleY(1f)
                .alpha(1f).setDuration(100)
                .setStartDelay(300L)
                .start();

        mBottomSheetLayout.setFab(mFab);
    }

    protected void initializeFabWithStamp() {
        initializeFab(R.drawable.ic_stamp, ColorStateList.valueOf(Color.WHITE), startStampEdit);
    }

    public void performFabAction() {
        //default implementation does nothing
    }

    @Override
    public void onStart() {
        super.onStart();
        StampEditStateObserver.getInstance().addListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        StampEditStateObserver.getInstance().removeListener(this);
    }

    @Override
    public void onSelectedStampChange(List<String> selectedStampList) {
    }

    @Override
    public void onNewStampCreated(String stamp) {

    }

    @Override
    public void onStateChange(StampEditStateObserver.State state) {
        switch (state) {
            case OPEN:
                openStampEdit();
                break;
            case CLOSE:
                closeStampEdit();
                break;
        }
    }
}
