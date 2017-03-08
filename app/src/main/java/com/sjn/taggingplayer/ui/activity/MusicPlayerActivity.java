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
package com.sjn.taggingplayer.ui.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.MediaBrowserCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;

import com.github.pedrovgs.DraggablePanel;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.ui.fragment.FullScreenPlayerFragment;
import com.sjn.taggingplayer.ui.fragment.MediaBrowserFragment;
import com.sjn.taggingplayer.ui.fragment.TagEditFragment;
import com.sjn.taggingplayer.ui.fragment.TagListFragment;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.PermissionHelper;

import java.io.Serializable;

import static com.sjn.taggingplayer.media.source.LocalMediaSource.PERMISSIONS;

/**
 * Main activity for the music player.
 * This class hold the MediaBrowser and the MediaController instances. It will create a MediaBrowser
 * when it is created and connect/disconnect on start/stop. Thus, a MediaBrowser will be always
 * connected while this activity is running.
 */
public class MusicPlayerActivity extends MediaBrowserActivity
        implements MediaBrowserFragment.MediaFragmentListener {

    private static final String TAG = LogHelper.makeLogTag(MusicPlayerActivity.class);
    private static final String SAVED_MEDIA_ID = "com.sjn.taggingplayer.MEDIA_ID";
    private static final String FRAGMENT_TAG = "uamp_list_container";

    public static final String EXTRA_START_FULLSCREEN =
            "com.sjn.taggingplayer.EXTRA_START_FULLSCREEN";

    /**
     * Optionally used with {@link #EXTRA_START_FULLSCREEN} to carry a MediaDescription to
     * the {@link FullScreenPlayerFragment}, speeding up the screen rendering
     * while the {@link android.support.v4.media.session.MediaControllerCompat} is connecting.
     */
    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION =
            "com.sjn.taggingplayer.CURRENT_MEDIA_DESCRIPTION";

    private Bundle mVoiceSearchParams;
    private Bundle mSavedInstanceState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "Activity onCreate");

        setContentView(R.layout.activity_player);
        mDraggablePanel = (DraggablePanel)findViewById(R.id.draggable_panel);
        initializeToolbar();
        initializeDraggablePanel();
        initializeFromParams(savedInstanceState, getIntent());

        if (!PermissionHelper.hasPermission(this, PERMISSIONS)) {
            Intent intent = new Intent(this, RequestPermissionActivity.class);
            intent.putExtra(RequestPermissionActivity.KEY_PERMISSIONS, PERMISSIONS);
            startActivity(intent);
        }
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeFromParams(mSavedInstanceState, getIntent());

        if (!PermissionHelper.hasPermission(this, PERMISSIONS)) {
            Intent intent = new Intent(this, RequestPermissionActivity.class);
            intent.putExtra(RequestPermissionActivity.KEY_PERMISSIONS, PERMISSIONS);
            startActivity(intent);
        }
        // Only check if a full screen player is needed on the first time:
        if (mSavedInstanceState == null) {
            startFullScreenActivityIfNeeded(getIntent());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String mediaId = getMediaId();
        if (mediaId != null) {
            outState.putString(SAVED_MEDIA_ID, mediaId);
        }
        saveDraggableState(outState);
        super.onSaveInstanceState(outState);
    }


    private static final String DRAGGABLE_PANEL_STATE = "draggable_panel_state";
    private static final int DELAY_MILLIS = 50;

    /**
     * Enum created to represent the DraggablePanel and DraggableView different states.
     *
     * @author Pedro Vicente Gómez Sánchez.
     */
    public enum DraggableState implements Serializable {

        MINIMIZED, MAXIMIZED, CLOSED_AT_LEFT, CLOSED_AT_RIGHT;

    }

    DraggablePanel mDraggablePanel;

    public boolean onOptionsItemSelected(int itemId) {
        switch (itemId) {
            case R.id.tag_edit:
                return true;
        }

        return false;

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        recoverDraggablePanelState(savedInstanceState);
    }
    /**
     * Get the DraggablePanelState from the saved bundle, modify the DraggablePanel visibility to
     * GONE
     * and apply the
     * DraggablePanelState to recover the last graphic state.
     */
    private void recoverDraggablePanelState(Bundle savedInstanceState) {
        final DraggableState draggableState =
                (DraggableState) savedInstanceState.getSerializable(DRAGGABLE_PANEL_STATE);
        if (draggableState == null) {
            mDraggablePanel.setVisibility(View.GONE);
            return;
        }
        updateDraggablePanelStateDelayed(draggableState);
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
                    @Override public void run() {
                        mDraggablePanel.maximize();
                    }
                }, DELAY_MILLIS);
                break;
            case MINIMIZED:
                handler.postDelayed(new Runnable() {
                    @Override public void run() {
                        mDraggablePanel.minimize();
                    }
                }, DELAY_MILLIS);
                break;
            case CLOSED_AT_LEFT:
                handler.postDelayed(new Runnable() {
                    @Override public void run() {
                        mDraggablePanel.setVisibility(View.GONE);
                        mDraggablePanel.closeToLeft();
                    }
                }, DELAY_MILLIS);
                break;
            case CLOSED_AT_RIGHT:
                handler.postDelayed(new Runnable() {
                    @Override public void run() {
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

    /**
     * Keep a reference of the last DraggablePanelState.
     *
     * @param outState Bundle used to store the DraggablePanelState.
     */
    private void saveDraggableState(Bundle outState) {
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
        outState.putSerializable(DRAGGABLE_PANEL_STATE, draggableState);
    }

    /**
     * Initialize the DraggablePanel with top and bottom Fragments and apply all the configuration.
     */
    private void initializeDraggablePanel() {
        mDraggablePanel.setFragmentManager(getSupportFragmentManager());
        mDraggablePanel.setTopFragment(new TagListFragment());
        mDraggablePanel.setBottomFragment(new TagEditFragment());
        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.x_scale_factor, typedValue, true);
        float xScaleFactor = typedValue.getFloat();
        typedValue = new TypedValue();
        getResources().getValue(R.dimen.y_scale_factor, typedValue, true);
        float yScaleFactor = typedValue.getFloat();
        mDraggablePanel.setXScaleFactor(xScaleFactor);
        mDraggablePanel.setYScaleFactor(yScaleFactor);
        mDraggablePanel.setTopViewHeight(
                getResources().getDimensionPixelSize(R.dimen.top_fragment_height));
        mDraggablePanel.setTopFragmentMarginRight(
                getResources().getDimensionPixelSize(R.dimen.top_fragment_margin));
        mDraggablePanel.setTopFragmentMarginBottom(
                getResources().getDimensionPixelSize(R.dimen.top_fragment_margin));
        mDraggablePanel.initializeView();
    }



    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
        LogHelper.d(TAG, "onMediaItemSelected, mediaId=" + item.getMediaId());
        if (item.isPlayable()) {
            getSupportMediaController().getTransportControls()
                    .playFromMediaId(item.getMediaId(), null);
        } else if (item.isBrowsable()) {
            navigateToBrowser(item.getMediaId());
        } else {
            LogHelper.w(TAG, "Ignoring MediaItem that is neither browsable nor playable: ",
                    "mediaId=", item.getMediaId());
        }
    }

    @Override
    public void setToolbarTitle(CharSequence title) {
        LogHelper.d(TAG, "Setting toolbar title to ", title);
        if (title == null) {
            title = getString(R.string.app_name);
        }
        setTitle(title);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogHelper.d(TAG, "onNewIntent, intent=" + intent);
        initializeFromParams(null, intent);
        startFullScreenActivityIfNeeded(intent);
    }

    private void startFullScreenActivityIfNeeded(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_START_FULLSCREEN, false)) {
            Intent fullScreenIntent = new Intent(this, FullScreenPlayerActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION,
                            intent.getParcelableExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION));
            startActivity(fullScreenIntent);
        }
    }

    protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
        String mediaId = null;
        // check if we were started from a "Play XYZ" voice search. If so, we save the extras
        // (which contain the query details) in a parameter, so we can reuse it later, when the
        // MediaSession is connected.
        if (intent.getAction() != null
                && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            mVoiceSearchParams = intent.getExtras();
            LogHelper.d(TAG, "Starting from voice search query=",
                    mVoiceSearchParams.getString(SearchManager.QUERY));
        } else {
            if (savedInstanceState != null) {
                // If there is a saved media ID, use it
                mediaId = savedInstanceState.getString(SAVED_MEDIA_ID);
            }
        }
        navigateToBrowser(mediaId);
    }

    public void navigateToBrowser(Fragment fragment, boolean addToBackStack, long selection) {
        navigateToBrowser(fragment, addToBackStack);
        mDrawer.setSelection(selection);
    }

    public void navigateToBrowser(Fragment fragment, boolean addToBackStack) {
        if (!addToBackStack) {
            getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, FRAGMENT_TAG);
        if (addToBackStack) {
            // If this is not the top level media (root), we add it to the fragment back stack,
            // so that actionbar toggle and Back will work appropriately:
            //transaction.setCustomAnimations(R.anim.fade_out, R.anim.fade_in);
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private void navigateToBrowser(String mediaId) {
        LogHelper.d(TAG, "navigateToBrowser, mediaId=" + mediaId);
        MediaBrowserFragment fragment = getBrowseFragment();

        if (fragment == null || !TextUtils.equals(fragment.getMediaId(), mediaId)) {
            fragment = new MediaBrowserFragment();
            fragment.setMediaId(mediaId);
            navigateToBrowser(fragment, true);
        }
    }

    public String getMediaId() {
        MediaBrowserFragment fragment = getBrowseFragment();
        if (fragment == null) {
            return null;
        }
        return fragment.getMediaId();
    }

    private MediaBrowserFragment getBrowseFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment instanceof MediaBrowserFragment) {
            return (MediaBrowserFragment) fragment;
        }
        return null;
    }

    @Override
    protected void onMediaControllerConnected() {
        if (mVoiceSearchParams != null) {
            // If there is a bootstrap parameter to start from a search query, we
            // send it to the media session and set it to null, so it won't play again
            // when the activity is stopped/started or recreated:
            String query = mVoiceSearchParams.getString(SearchManager.QUERY);
            getSupportMediaController().getTransportControls()
                    .playFromSearch(query, mVoiceSearchParams);
            mVoiceSearchParams = null;
        }
        MediaBrowserFragment mediaBrowserFragment = getBrowseFragment();
        if (mediaBrowserFragment != null) {
            mediaBrowserFragment.onConnected();
        }
    }
}
