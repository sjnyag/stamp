package com.sjn.taggingplayer.ui;


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;

import com.github.pedrovgs.DraggablePanel;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.ui.fragment.TagEditFragment;
import com.sjn.taggingplayer.ui.fragment.TagListFragment;
import com.sjn.taggingplayer.utils.LogHelper;

import java.io.Serializable;


public class DraggablePanelManager {

    private static final String TAG = LogHelper.makeLogTag(DraggablePanelManager.class);

    /**
     * Enum created to represent the DraggablePanel and DraggableView different states.
     *
     * @author Pedro Vicente Gómez Sánchez.
     */
    private enum DraggableState implements Serializable {
        MINIMIZED, MAXIMIZED, CLOSED_AT_LEFT, CLOSED_AT_RIGHT;
    }

    private static final String DRAGGABLE_PANEL_STATE = "draggable_panel_state";
    private static final int DELAY_MILLIS = 50;

    AppCompatActivity mActivity;
    DraggablePanel mDraggablePanel;

    public DraggablePanelManager(AppCompatActivity activity, DraggablePanel draggablePanel) {
        mActivity = activity;
        mDraggablePanel = draggablePanel;
    }

    public void toggle() {
        LogHelper.i(TAG, getDraggableState());
        if (mDraggablePanel.getVisibility() != View.VISIBLE) {
            mDraggablePanel.setVisibility(View.VISIBLE);
            return;
        }
        switch (getDraggableState()) {
            case MAXIMIZED:
                updateDraggablePanelStateDelayed(DraggableState.CLOSED_AT_RIGHT);
                break;
            case MINIMIZED:
                updateDraggablePanelStateDelayed(DraggableState.MAXIMIZED);
                break;
            case CLOSED_AT_LEFT:
                updateDraggablePanelStateDelayed(DraggableState.MAXIMIZED);
                break;
            case CLOSED_AT_RIGHT:
                updateDraggablePanelStateDelayed(DraggableState.MAXIMIZED);
                break;
            default:
                break;
        }
    }

    /**
     * Get the DraggablePanelState from the saved bundle, modify the DraggablePanel visibility to
     * GONE
     * and apply the
     * DraggablePanelState to recover the last graphic state.
     */
    public void recoverDraggablePanelState(Bundle savedInstanceState) {
        final DraggableState draggableState =
                (DraggableState) savedInstanceState.getSerializable(DRAGGABLE_PANEL_STATE);
        if (draggableState == null) {
            mDraggablePanel.setVisibility(View.GONE);
            return;
        }
        updateDraggablePanelStateDelayed(draggableState);
    }

    /**
     * Keep a reference of the last DraggablePanelState.
     *
     * @param outState Bundle used to store the DraggablePanelState.
     */
    public void saveDraggableState(Bundle outState) {
        outState.putSerializable(DRAGGABLE_PANEL_STATE, getDraggableState());
    }

    /**
     * Initialize the DraggablePanel with top and bottom Fragments and apply all the configuration.
     */
    public void initializeDraggablePanel() {
        mDraggablePanel.setFragmentManager(mActivity.getSupportFragmentManager());
        mDraggablePanel.setTopFragment(new TagListFragment());
        mDraggablePanel.setBottomFragment(new TagEditFragment());
        TypedValue typedValue = new TypedValue();
        mActivity.getResources().getValue(R.dimen.x_scale_factor, typedValue, true);
        float xScaleFactor = typedValue.getFloat();
        typedValue = new TypedValue();
        mActivity.getResources().getValue(R.dimen.y_scale_factor, typedValue, true);
        float yScaleFactor = typedValue.getFloat();
        mDraggablePanel.setXScaleFactor(xScaleFactor);
        mDraggablePanel.setYScaleFactor(yScaleFactor);
        mDraggablePanel.setTopViewHeight(
                mActivity.getResources().getDimensionPixelSize(R.dimen.top_fragment_height));
        mDraggablePanel.setTopFragmentMarginRight(
                mActivity.getResources().getDimensionPixelSize(R.dimen.top_fragment_margin));
        mDraggablePanel.setTopFragmentMarginBottom(
                mActivity.getResources().getDimensionPixelSize(R.dimen.top_fragment_margin));
        mDraggablePanel.initializeView();
        mDraggablePanel.setVisibility(View.GONE);
    }

    /**
     * Return the view to the DraggablePanelState: minimized, maximized, closed to the right or
     * closed
     * to the left.
     *
     * @param draggableState to apply.
     */
    private void updateDraggablePanelStateDelayed(DraggableState draggableState) {
        Handler handler = new Handler();
        switch (draggableState) {
            case MAXIMIZED:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDraggablePanel.maximize();
                    }
                }, DELAY_MILLIS);
                break;
            case MINIMIZED:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDraggablePanel.minimize();
                    }
                }, DELAY_MILLIS);
                break;
            case CLOSED_AT_LEFT:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDraggablePanel.setVisibility(View.GONE);
                        mDraggablePanel.closeToLeft();
                    }
                }, DELAY_MILLIS);
                break;
            case CLOSED_AT_RIGHT:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDraggablePanel.setVisibility(View.GONE);
                        mDraggablePanel.closeToRight();
                    }
                }, DELAY_MILLIS);
                break;
            default:
                mDraggablePanel.setVisibility(View.GONE);
                break;
        }
    }

    private DraggableState getDraggableState() {
        DraggableState draggableState = null;
        if (mDraggablePanel.isMaximized()) {
            draggableState = DraggableState.MAXIMIZED;
        } else if (mDraggablePanel.isMinimized()) {
            draggableState = DraggableState.MINIMIZED;
        } else if (mDraggablePanel.isClosedAtLeft()) {
            draggableState = DraggableState.CLOSED_AT_LEFT;
        } else if (mDraggablePanel.isClosedAtRight()) {
            draggableState = DraggableState.CLOSED_AT_RIGHT;
        }
        return draggableState;
    }
}
