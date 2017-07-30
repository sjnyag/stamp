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

import android.app.ActivityManager;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.sjn.stamp.MusicService;
import com.sjn.stamp.R;
import com.sjn.stamp.ui.fragment.PlaybackControlsFragment;
import com.sjn.stamp.ui.observer.MediaControllerObserver;
import com.sjn.stamp.utils.BitmapHelper;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.ResourceHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Base activity for activities that need to show a playback control fragment when media is playing.
 */
public abstract class MediaBrowserActivity extends ActionBarCastActivity
        implements MediaBrowsable, MediaControllerObserver.Listener {

    private static final String TAG = LogHelper.makeLogTag(MediaBrowserActivity.class);

    private MediaBrowserCompat mMediaBrowser;
    private PlaybackControlsFragment mControlsFragment;
    //to avoid GC
    private Target mTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogHelper.i(TAG, "Activity onCreate");

        if (Build.VERSION.SDK_INT >= 21) {
            // Since our app icon has the same color as colorPrimary, our entry in the Recent Apps
            // list gets weird. We need to change either the icon or the color
            // of the TaskDescription.
            ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(
                    getTitle().toString(),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_white),
                    ResourceHelper.getThemeColor(this, R.attr.colorPrimary,
                            android.R.color.darker_gray));
            setTaskDescription(taskDesc);
        }

        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);

        mMediaBrowser.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogHelper.i(TAG, "Activity onStart");
        MediaControllerObserver.getInstance().addListener(this);

        mControlsFragment = (PlaybackControlsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_playback_controls);
        if (mControlsFragment == null) {
            LogHelper.e(TAG, "Missing fragment with id 'controls'. Cannot continue.");
            return;
            //throw new IllegalStateException("Missing fragment with id 'controls'. Cannot continue.");
        }
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).registerCallback(MediaControllerObserver.getInstance());
        }

        hidePlaybackControls();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogHelper.i(TAG, "Activity onStop");
        MediaControllerObserver.getInstance().removeListener(this);
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(MediaControllerObserver.getInstance());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogHelper.i(TAG, "Activity onDestroy");
        mMediaBrowser.disconnect();
    }

    @Override
    public void sendCustomAction(String action, Bundle extras, MediaBrowserCompat.CustomActionCallback callback) {
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            mMediaBrowser.sendCustomAction(action, extras, callback);
        }
    }

    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    protected void onMediaControllerConnected() {
        // empty implementation, can be overridden by clients.
    }

    protected void showPlaybackControls() {
        LogHelper.i(TAG, "showPlaybackControls");
        //if (NetworkHelper.isOnline(this)) {
        getSupportFragmentManager().beginTransaction()
                .show(mControlsFragment)
                .commitAllowingStateLoss();
        //}
    }

    protected void hidePlaybackControls() {
        LogHelper.i(TAG, "hidePlaybackControls");
        /*
        getSupportFragmentManager().beginTransaction()
                .hide(mControlsFragment)
                .commitAllowingStateLoss();
                */
    }

    /**
     * Check if the MediaSession is active and in a "playback-able" state
     * (not NONE and not STOPPED).
     *
     * @return true if the MediaSession's state requires playback controls to be visible.
     */
    protected boolean shouldShowControls() {
        LogHelper.i(TAG, "shouldShowControls");
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(this);
        if (mediaController == null ||
                mediaController.getMetadata() == null ||
                mediaController.getPlaybackState() == null) {
            return false;
        }
        switch (mediaController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_ERROR:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                return false;
            default:
                return true;
        }
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        LogHelper.i(TAG, "connectToSession");
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        MediaControllerCompat.setMediaController(this, mediaController);
        mediaController.registerCallback(MediaControllerObserver.getInstance());
        MediaControllerObserver.getInstance().notifyConnected();

        if (shouldShowControls()) {
            showPlaybackControls();
        } else {
            LogHelper.d(TAG, "connectionCallback.onConnected: " +
                    "hiding controls because metadata is null");
            hidePlaybackControls();
        }

        if (mControlsFragment != null) {
            mControlsFragment.onConnected();
        }
        onConnected();

        onMediaControllerConnected();
    }

    // Callback that ensures that we are showing the controls
    @Override
    public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
        if (shouldShowControls()) {
            showPlaybackControls();
        } else if (state != null) {
            LogHelper.d(TAG, "mediaControllerCallback.onPlaybackStateChanged: " +
                    "hiding controls because state is ", state.getState());
            hidePlaybackControls();
        }
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        LogHelper.d(TAG, "onMetadataChanged");
        if (shouldShowControls()) {
            showPlaybackControls();
            updateAccountHeader(metadata);
        } else {
            LogHelper.d(TAG, "mediaControllerCallback.onMetadataChanged: " +
                    "hiding controls because metadata is null");
            hidePlaybackControls();
        }
    }


    @Override
    public void onConnected() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        LogHelper.d(TAG, "onConnected, mediaController==null? ", controller == null);
        if (controller != null) {
            onMetadataChanged(controller.getMetadata());
            onPlaybackStateChanged(controller.getPlaybackState());
        }
    }

    private void updateAccountHeader(final MediaMetadataCompat metadata) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogHelper.i(TAG, "updateAccountHeader");
                if (metadata == null) {
                    return;
                }
                if (metadata.getDescription().getIconUri() != null) {

                    mTarget = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            final ProfileDrawerItem profileDrawerItem = new ProfileDrawerItem();
                            if (metadata.getDescription().getTitle() != null) {
                                profileDrawerItem.withName(metadata.getDescription().getTitle().toString());
                            }
                            if (metadata.getDescription().getSubtitle() != null) {
                                profileDrawerItem.withEmail(metadata.getDescription().getSubtitle().toString());
                            }
                            profileDrawerItem.withIcon(bitmap);
                            mAccountHeader.clear();
                            mAccountHeader.addProfiles(profileDrawerItem);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            final ProfileDrawerItem profileDrawerItem = new ProfileDrawerItem();
                            if (metadata.getDescription().getTitle() != null) {
                                profileDrawerItem.withName(metadata.getDescription().getTitle().toString());
                            }
                            if (metadata.getDescription().getSubtitle() != null) {
                                profileDrawerItem.withEmail(metadata.getDescription().getSubtitle().toString());
                            }
                            profileDrawerItem.withIcon(R.mipmap.ic_notification);
                            mAccountHeader.clear();
                            mAccountHeader.addProfiles(profileDrawerItem);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            final ProfileDrawerItem profileDrawerItem = new ProfileDrawerItem();
                            if (metadata.getDescription().getTitle() != null) {
                                profileDrawerItem.withName(metadata.getDescription().getTitle().toString());
                            }
                            if (metadata.getDescription().getSubtitle() != null) {
                                profileDrawerItem.withEmail(metadata.getDescription().getSubtitle().toString());
                            }
                            profileDrawerItem.withIcon(R.mipmap.ic_notification);
                            mAccountHeader.clear();
                            mAccountHeader.addProfiles(profileDrawerItem);
                        }
                    };
                    BitmapHelper.readBitmapAsync(MediaBrowserActivity.this, metadata.getDescription().getIconUri().toString(), mTarget);
                }
            }
        });
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    LogHelper.i(TAG, "onConnected");
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        LogHelper.e(TAG, e, "could not connect media controller");
                        hidePlaybackControls();
                    }
                }
            };

}
