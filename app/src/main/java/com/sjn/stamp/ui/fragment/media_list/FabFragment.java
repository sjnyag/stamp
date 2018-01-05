package com.sjn.stamp.ui.fragment.media_list;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener;
import com.sjn.stamp.R;
import com.sjn.stamp.ui.SongAdapter;
import com.sjn.stamp.ui.custom.CenteredMaterialSheetFab;
import com.sjn.stamp.ui.custom.Fab;
import com.sjn.stamp.ui.item.SongItem;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.SpotlightHelper;
import com.takusemba.spotlight.SimpleTarget;
import com.takusemba.spotlight.Spotlight;

import java.util.List;

public abstract class FabFragment extends Fragment implements StampEditStateObserver.Listener {

    private static final String TAG = LogHelper.makeLogTag(FabFragment.class);

    protected RecyclerView mRecyclerView;
    protected SongAdapter mAdapter;

    protected Fab mFab;
    protected CenteredMaterialSheetFab mCenteredMaterialSheetFab;

    protected boolean mIsVisibleToUser = true;

    public boolean isVisibleToUser(){
        return mIsVisibleToUser;
    }


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
        if(mCenteredMaterialSheetFab == null || mFab == null){
            return;
        }
        if (!mCenteredMaterialSheetFab.isSheetVisible()) {
            mCenteredMaterialSheetFab.showSheet();
        }
        mFab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        mFab.setImageResource(R.drawable.ic_dialog_close_light);
        mFab.setTag(R.id.fab_type, R.drawable.ic_dialog_close_light);
        mFab.setOnClickListener(stopStampEdit);
    }


    private void closeStampEdit() {
        if(mCenteredMaterialSheetFab == null || mFab == null){
            return;
        }
        if (mCenteredMaterialSheetFab.isSheetVisible()) {
            mCenteredMaterialSheetFab.hideSheet();
        }
        mFab.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        mFab.setImageResource(R.drawable.ic_stamp);
        mFab.setTag(R.id.fab_type, R.drawable.ic_stamp);
        mFab.setOnClickListener(startStampEdit);
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
        if (onClickListener != null) {
            mFab.setOnClickListener(onClickListener);
        }
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

    @SuppressWarnings("unused")
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
        if (mCenteredMaterialSheetFab != null && mCenteredMaterialSheetFab.isSheetVisible()) {
            mCenteredMaterialSheetFab.hideSheet();
        }
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
        if(mCenteredMaterialSheetFab == null || mFab == null){
            return;
        }
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
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void showSpotlight() {
        if(mRecyclerView == null){
            return;
        }
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
}
