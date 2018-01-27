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
package com.sjn.stamp.ui.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.text.TextUtils;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sjn.stamp.MusicService;
import com.sjn.stamp.R;
import com.sjn.stamp.ui.DialogFacade;
import com.sjn.stamp.ui.DrawerMenu;
import com.sjn.stamp.ui.SongListFragmentFactory;
import com.sjn.stamp.ui.fragment.FullScreenPlayerFragment;
import com.sjn.stamp.ui.fragment.media_list.MediaBrowserListFragment;
import com.sjn.stamp.ui.fragment.media_list.PagerFragment;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.MediaRetrieveHelper;
import com.sjn.stamp.utils.PermissionHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.Arrays;
import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * Main activity for the music player.
 * This class hold the MediaBrowser and the MediaController instances. It will create a MediaBrowser
 * when it is created and connect/disconnect on start/stop. Thus, a MediaBrowser will be always
 * connected while this activity is running.
 */
public class MusicPlayerListActivity extends MediaBrowserListActivity {

    private final int REQUEST_PERMISSION = 1;

    private static final String TAG = LogHelper.INSTANCE.makeLogTag(MusicPlayerListActivity.class);
    private static final String SAVED_MEDIA_ID = "com.sjn.stamp.MEDIA_ID";

    public static final String EXTRA_START_FULLSCREEN =
            "com.sjn.stamp.EXTRA_START_FULLSCREEN";

    /**
     * Optionally used with {@link #EXTRA_START_FULLSCREEN} to carry a MediaDescription to
     * the {@link FullScreenPlayerFragment}, speeding up the screen rendering
     * while the {@link android.support.v4.media.session.MediaControllerCompat} is connecting.
     */
    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION =
            "com.sjn.stamp.CURRENT_MEDIA_DESCRIPTION";

    private Bundle mVoiceSearchParams;
    private Bundle mSavedInstanceState;
    private Intent mNewIntent = null;
    private Uri mReservedUri = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.INSTANCE.d(TAG, "Activity onCreate");

        setContentView(R.layout.activity_player);
        initializeToolbar();
        if (savedInstanceState == null) {
            navigateToBrowser(DrawerMenu.first().getFragment(), false);
        }

        if (!PermissionHelper.INSTANCE.hasPermission(this, MediaRetrieveHelper.INSTANCE.getPERMISSIONS())) {
            LogHelper.INSTANCE.d(TAG, "has no Permission");
            PermissionHelper.INSTANCE.requestPermissions(this, MediaRetrieveHelper.INSTANCE.getPERMISSIONS(), REQUEST_PERMISSION);
        }
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LogHelper.INSTANCE.d(TAG, "onRequestPermissionsResult");
        LogHelper.INSTANCE.d(TAG, "onRequestPermissionsResult: requestCode " + requestCode);
        LogHelper.INSTANCE.d(TAG, "onRequestPermissionsResult: permissions " + Arrays.toString(permissions));
        LogHelper.INSTANCE.d(TAG, "onRequestPermissionsResult: grantResults " + Arrays.toString(grantResults));
        if (!PermissionHelper.INSTANCE.hasPermission(this, MediaRetrieveHelper.INSTANCE.getPERMISSIONS())) {
            DialogFacade.createPermissionNecessaryDialog(this, new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    finish();
                }
            }).show();
        }
        if (Arrays.asList(permissions).contains(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            sendCustomAction(MusicService.CUSTOM_ACTION_RELOAD_MUSIC_PROVIDER, null, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LogHelper.INSTANCE.d(TAG, "Activity onResume");
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
        LogHelper.INSTANCE.d(TAG, "onNewIntent, intent=" + intent);
        mNewIntent = intent;
    }

    @Override
    public void search(String query, Bundle extras, MediaBrowserCompat.SearchCallback callback) {
        if (getMediaBrowser() != null) {
            getMediaBrowser().search(query, extras, callback);
        }
    }

    @Override
    public void playByCategory(String mediaId) {
        LogHelper.INSTANCE.d(TAG, "playByCategory, mediaId=" + mediaId);
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (controller != null) {
            controller.getTransportControls().playFromMediaId(mediaId, null);
        }

    }

    @Override
    public void onMediaItemSelected(String musicId) {
        LogHelper.INSTANCE.d(TAG, "onMediaItemSelected, musicId=" + musicId);
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (controller != null) {
            controller.getTransportControls().playFromMediaId(MediaIDHelper.INSTANCE.createDirectMediaId(musicId), null);
        }
    }

    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
        onMediaItemSelected(item.getMediaId(), item.isPlayable(), item.isBrowsable());
    }

    @Override
    public void onMediaItemSelected(String mediaId, boolean isPlayable, boolean isBrowsable) {
        LogHelper.INSTANCE.d(TAG, "onMediaItemSelected, mediaId=" + mediaId);
        if (isPlayable) {
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
            if (controller != null) {
                controller.getTransportControls().playFromMediaId(mediaId, null);
            }
        } else if (isBrowsable) {
            navigateToBrowser(mediaId);
        } else {
            LogHelper.INSTANCE.w(TAG, "Ignoring MediaItem that is neither browsable nor playable: ",
                    "mediaId=", mediaId);
        }
    }

    @Override
    public void setToolbarTitle(CharSequence title) {
        LogHelper.INSTANCE.d(TAG, "Setting toolbar title to ", title);
        if (title == null) {
            title = getString(R.string.app_name);
        }
        setTitle(title);
    }

    @Override
    public void onMediaControllerConnected() {
        super.onMediaControllerConnected();
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
            mediaControllerFragment.onMediaControllerConnected();
        }
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (mReservedUri != null && controller != null) {
            controller.getTransportControls().playFromUri(mReservedUri, null);
            mReservedUri = null;
        }
    }

    @Override
    public void onSessionDestroyed() {

    }

    private void startFullScreenIfNeeded(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_START_FULLSCREEN, false)) {
            SlidingUpPanelLayout slidingUpPanelLayout = findViewById(R.id.sliding_layout);
            if (slidingUpPanelLayout == null) {
                return;
            }
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        }
    }

    protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
        LogHelper.INSTANCE.d(TAG, "initializeFromParams ", intent);
        String mediaId = null;
        // check if we were started from a "Play XYZ" voice search. If so, we create the extras
        // (which contain the query details) in a parameter, so we can reuse it later, when the
        // MediaSession is connected.
        if (intent.getAction() != null
                && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            mVoiceSearchParams = intent.getExtras();
            LogHelper.INSTANCE.d(TAG, "Starting from voice search query=",
                    mVoiceSearchParams.getString(SearchManager.QUERY));
        } else if (intent.getData() != null) {
            LogHelper.INSTANCE.d(TAG, "Play from Intent: " + intent.getData());
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
        LogHelper.INSTANCE.d(TAG, "navigateToBrowser, mediaId=" + mediaId);
        if (mediaId == null) {
            return;
        }
        MediaBrowserListFragment fragment = getMediaBrowserListFragment();

        if (fragment == null || !TextUtils.equals(fragment.getMediaId(), mediaId)) {
            navigateToBrowser(SongListFragmentFactory.INSTANCE.create(mediaId), true);
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
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(DrawerActivity.FRAGMENT_TAG);
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
    public List<AbstractFlexibleItem<?>> getCurrentMediaItems() {
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

    @Override
    public void onBackPressed() {
        SlidingUpPanelLayout slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        if (slidingUpPanelLayout != null && slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }
        super.onBackPressed();
    }
}
