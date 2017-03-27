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
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.text.TextUtils;

import com.github.pedrovgs.DraggablePanel;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.ui.DraggablePanelManager;
import com.sjn.taggingplayer.ui.fragment.FullScreenPlayerFragment;
import com.sjn.taggingplayer.ui.fragment.MediaBrowserFragment;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.PermissionHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

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
    private DraggablePanelManager mDraggablePanelManager;
    private Intent mNewIntent = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "Activity onCreate");

        setContentView(R.layout.activity_player);
        mDraggablePanelManager = new DraggablePanelManager(
                this,
                (DraggablePanel) findViewById(R.id.draggable_panel)
        );
        initializeToolbar();
        mDraggablePanelManager.initializeDraggablePanel();
        navigateToBrowser(new FullScreenPlayerFragment(), false);

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
        initializeFromParams(mSavedInstanceState, mNewIntent == null ? getIntent() : mNewIntent);
        mNewIntent = null;

        if (!PermissionHelper.hasPermission(this, PERMISSIONS)) {
            Intent intent = new Intent(this, RequestPermissionActivity.class);
            intent.putExtra(RequestPermissionActivity.KEY_PERMISSIONS, PERMISSIONS);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String mediaId = getMediaId();
        if (mediaId != null) {
            outState.putString(SAVED_MEDIA_ID, mediaId);
        }
        mDraggablePanelManager.saveDraggableState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDraggablePanelManager.recoverDraggablePanelState(savedInstanceState);
    }


    public boolean onOptionsItemSelected(int itemId) {
        switch (itemId) {
            case R.id.tag_edit:
                mDraggablePanelManager.toggle();
                return true;
        }

        return false;
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
        mNewIntent = intent;
    }

    private void startFullScreenIfNeeded(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_START_FULLSCREEN, false)) {
            SlidingUpPanelLayout slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
            if (slidingUpPanelLayout == null) {
                return;
            }
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
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
        startFullScreenIfNeeded(intent);
    }

    private void navigateToBrowser(String mediaId) {
        LogHelper.d(TAG, "navigateToBrowser, mediaId=" + mediaId);
        if (mediaId == null) {
            return;
        }
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
