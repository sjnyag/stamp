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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.text.TextUtils;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.ui.fragment.FullScreenPlayerFragment;
import com.sjn.taggingplayer.ui.fragment.media_list.MediaBrowserListFragment;
import com.sjn.taggingplayer.ui.fragment.media_list.PagerFragment;
import com.sjn.taggingplayer.ui.fragment.media_list.SongListFragment;
import com.sjn.taggingplayer.ui.fragment.media_list.TimelineFragment;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.MediaIDHelper;
import com.sjn.taggingplayer.utils.MediaRetrieveHelper;
import com.sjn.taggingplayer.utils.PermissionHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * Main activity for the music player.
 * This class hold the MediaBrowser and the MediaController instances. It will create a MediaBrowser
 * when it is created and connect/disconnect on start/stop. Thus, a MediaBrowser will be always
 * connected while this activity is running.
 */
public class MusicPlayerListActivity extends MediaBrowserListActivity {

    private static final String TAG = LogHelper.makeLogTag(MusicPlayerListActivity.class);
    private static final String SAVED_MEDIA_ID = "com.sjn.taggingplayer.MEDIA_ID";

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
    private Intent mNewIntent = null;
    private Uri mReservedUri = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "Activity onCreate");

        setContentView(R.layout.activity_player);
        initializeToolbar();
        navigateToBrowser(new TimelineFragment(), false);

        if (!PermissionHelper.hasPermission(this, MediaRetrieveHelper.PERMISSIONS)) {
            Intent intent = new Intent(this, RequestPermissionActivity.class);
            intent.putExtra(RequestPermissionActivity.KEY_PERMISSIONS, MediaRetrieveHelper.PERMISSIONS);
            startActivity(intent);
        }
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    public void onResume() {
        super.onResume();
        LogHelper.d(TAG, "Activity onResume");
        initializeFromParams(mSavedInstanceState, mNewIntent == null ? getIntent() : mNewIntent);
        mNewIntent = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String mediaId = getMediaId();
        if (mediaId != null) {
            outState.putString(SAVED_MEDIA_ID, mediaId);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(int itemId) {
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogHelper.d(TAG, "onNewIntent, intent=" + intent);
        mNewIntent = intent;
    }

    @Override
    public void onMediaItemSelected(String musicId) {
        LogHelper.d(TAG, "onMediaItemSelected, musicId=" + musicId);
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (controller != null) {
            controller.getTransportControls().playFromMediaId(MediaIDHelper.createDirectMediaId(musicId), null);
        }
    }

    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
        LogHelper.d(TAG, "onMediaItemSelected, mediaId=" + item.getMediaId());
        if (item.isPlayable()) {
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
            if (controller != null) {
                controller.getTransportControls().playFromMediaId(item.getMediaId(), null);
            }
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
        LogHelper.d(TAG, "initializeFromParams ", intent);
        String mediaId = null;
        // check if we were started from a "Play XYZ" voice search. If so, we save the extras
        // (which contain the query details) in a parameter, so we can reuse it later, when the
        // MediaSession is connected.
        if (intent.getAction() != null
                && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            mVoiceSearchParams = intent.getExtras();
            LogHelper.d(TAG, "Starting from voice search query=",
                    mVoiceSearchParams.getString(SearchManager.QUERY));
        } else if (intent.getData() != null) {
            LogHelper.d(TAG, "Play from Intent: " + intent.getData());
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
            if (controller != null) {
                mReservedUri = null;
                controller.getTransportControls().playFromUri(intent.getData(), null);
            } else {
                mReservedUri = intent.getData();
            }
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
        MediaBrowserListFragment fragment = getMediaBrowserListFragment();

        if (fragment == null || !TextUtils.equals(fragment.getMediaId(), mediaId)) {
            SongListFragment newFragment = new SongListFragment();
            newFragment.setMediaId(mediaId);
            navigateToBrowser(newFragment, true);
        }
    }

    private String getMediaId() {
        MediaBrowserListFragment fragment = getMediaBrowserListFragment();
        if (fragment == null) {
            return null;
        }
        return fragment.getMediaId();
    }

    private MediaBrowserListFragment getMediaBrowserListFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment instanceof MediaBrowserListFragment) {
            return (MediaBrowserListFragment) fragment;
        } else if (fragment instanceof PagerFragment) {
            Fragment page = ((PagerFragment) fragment).getCurrent();
            if (page instanceof MediaBrowserListFragment) {
                return (MediaBrowserListFragment) page;
            }
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
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
            if (controller != null) {
                controller.getTransportControls()
                        .playFromSearch(query, mVoiceSearchParams);
            }
            mVoiceSearchParams = null;
        }
        MediaBrowserListFragment mediaControllerFragment = getMediaBrowserListFragment();
        if (mediaControllerFragment != null) {
            mediaControllerFragment.onConnected();
        }
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (mReservedUri != null && controller != null) {
            controller.getTransportControls().playFromUri(mReservedUri, null);
            mReservedUri = null;
        }
    }

    @Override
    public List<AbstractFlexibleItem> getCurrentMediaItems() {
        MediaBrowserListFragment mediaControllerFragment = getMediaBrowserListFragment();
        if (mediaControllerFragment != null) {
            return mediaControllerFragment.getCurrentItems();
        }
        return null;
    }

    @Override
    public int getMenuResourceId() {
        MediaBrowserListFragment mediaControllerFragment = getMediaBrowserListFragment();
        if (mediaControllerFragment != null) {
            return mediaControllerFragment.getMenuResourceId();
        }
        return 0;
    }
}
