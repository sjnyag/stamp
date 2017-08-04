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

package com.sjn.stamp;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;

import com.sjn.stamp.media.player.CastPlayer;
import com.sjn.stamp.ui.activity.MusicPlayerListActivity;
import com.sjn.stamp.utils.BitmapHelper;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.ResourceHelper;
import com.sjn.stamp.utils.ViewHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import static com.sjn.stamp.utils.NotificationHelper.ACTION_CMD;
import static com.sjn.stamp.utils.NotificationHelper.CMD_KILL;
import static com.sjn.stamp.utils.NotificationHelper.CMD_NAME;
import static com.sjn.stamp.utils.NotificationHelper.CMD_STOP_CASTING;

/**
 * Keeps track of a notification and updates it automatically for a given
 * MediaSession. Maintaining a visible notification (usually) guarantees that the music service
 * won't be killed during playback.
 */
public class MediaNotificationManager extends BroadcastReceiver {
    private static final String TAG = LogHelper.makeLogTag(MediaNotificationManager.class);

    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_PAUSE = "com.sjn.stamp.pause";
    public static final String ACTION_PLAY = "com.sjn.stamp.play";
    public static final String ACTION_PREV = "com.sjn.stamp.prev";
    public static final String ACTION_NEXT = "com.sjn.stamp.next";
    public static final String ACTION_STOP_CASTING = "com.sjn.stamp.stop_cast";

    private final MusicService mService;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mController;
    private MediaControllerCompat.TransportControls mTransportControls;

    private PlaybackStateCompat mPlaybackState;
    private MediaMetadataCompat mMetadata;

    private final NotificationManagerCompat mNotificationManager;

    private final PendingIntent mPauseIntent;
    private final PendingIntent mPlayIntent;
    private final PendingIntent mPreviousIntent;
    private final PendingIntent mNextIntent;
    private final PendingIntent mKillIntent;

    private final PendingIntent mStopCastIntent;

    private final int mNotificationColor;

    private boolean mStarted = false;
    //to avoid GC
    @SuppressWarnings(value = "FieldCanBeLocal")
    private Target mTarget;
    private Notification mLatestNotification = null;

    public MediaNotificationManager(MusicService service) throws RemoteException {
        mService = service;
        updateSessionToken();

        mNotificationColor = ResourceHelper.getThemeColor(mService, com.sjn.stamp.R.attr.colorPrimary,
                Color.DKGRAY);

        mNotificationManager = NotificationManagerCompat.from(service);

        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopCastIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP_CASTING).setPackage(pkg),
                PendingIntent.FLAG_CANCEL_CURRENT);
        Intent i = new Intent(mService, MusicService.class);
        i.setAction(ACTION_CMD);
        i.putExtra(CMD_NAME, CMD_KILL);
        mKillIntent = PendingIntent.getService(mService, REQUEST_CODE, i, PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();
    }

    public void startForeground() {
        LogHelper.i(TAG, "startForeground");
        if (mLatestNotification != null) {
            mService.startForeground(NOTIFICATION_ID, mLatestNotification);
        }
    }

    public void stopForeground(boolean removeNotification) {
        LogHelper.i(TAG, "stopForeground");
        mService.stopForeground(removeNotification);
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before {@link #stopNotification} is called.
     */
    public void startNotification() {
        if (!mStarted) {
            mMetadata = mController.getMetadata();
            mPlaybackState = mController.getPlaybackState();

            // The notification must be updated after setting started to true
            mLatestNotification = createNotification();
            if (mLatestNotification != null) {
                mController.registerCallback(mCb);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);
                filter.addAction(ACTION_STOP_CASTING);
                mService.registerReceiver(this, filter);
                LogHelper.i(TAG, "startNotification startForeground()");
                mService.startForeground(NOTIFICATION_ID, mLatestNotification);
                mStarted = true;
            }
        }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification() {
        LogHelper.i(TAG, "stopNotification");
        if (mStarted) {
            mStarted = false;
            mController.unregisterCallback(mCb);
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            LogHelper.i(TAG, "stopNotification stopForeground(true)");
            mService.stopForeground(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        LogHelper.d(TAG, "Received intent with action " + action);
        switch (action) {
            case ACTION_PAUSE:
                mTransportControls.pause();
                break;
            case ACTION_PLAY:
                mTransportControls.play();
                break;
            case ACTION_NEXT:
                mTransportControls.skipToNext();
                break;
            case ACTION_PREV:
                mTransportControls.skipToPrevious();
                break;
            case ACTION_STOP_CASTING:
                Intent i = new Intent(context, MusicService.class);
                i.setAction(ACTION_CMD);
                i.putExtra(CMD_NAME, CMD_STOP_CASTING);
                mService.startService(i);
                break;
            default:
                LogHelper.w(TAG, "Unknown intent ignored. Action=", action);
        }
    }

    /**
     * Update the state based on a change on the session token. Called either when
     * we are running for the first time or when the media session owner has destroyed the session
     * (see {@link android.media.session.MediaController.Callback#onSessionDestroyed()})
     */
    private void updateSessionToken() throws RemoteException {
        MediaSessionCompat.Token freshToken = mService.getSessionToken();
        if (mSessionToken == null && freshToken != null ||
                mSessionToken != null && !mSessionToken.equals(freshToken)) {
            if (mController != null) {
                mController.unregisterCallback(mCb);
            }
            mSessionToken = freshToken;
            if (mSessionToken != null) {
                mController = new MediaControllerCompat(mService, mSessionToken);
                mTransportControls = mController.getTransportControls();
                if (mStarted) {
                    mController.registerCallback(mCb);
                }
            }
        }
    }

    private PendingIntent createContentIntent(MediaDescriptionCompat description) {
        Intent openUI = new Intent(mService, MusicPlayerListActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openUI.putExtra(MusicPlayerListActivity.EXTRA_START_FULLSCREEN, true);
        if (description != null) {
            openUI.putExtra(MusicPlayerListActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
        }
        return PendingIntent.getActivity(mService, REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private final MediaControllerCompat.Callback mCb = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            mPlaybackState = state;
            LogHelper.d(TAG, "Received new playback state", state);
            if (state.getState() == PlaybackStateCompat.STATE_STOPPED ||
                    state.getState() == PlaybackStateCompat.STATE_NONE) {
                stopNotification();
            } else {
                mLatestNotification = createNotification();
                if (mLatestNotification != null) {
                    mNotificationManager.notify(NOTIFICATION_ID, mLatestNotification);
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            mMetadata = metadata;
            LogHelper.d(TAG, "Received new metadata ", metadata);
            mLatestNotification = createNotification();
            if (mLatestNotification != null) {
                mNotificationManager.notify(NOTIFICATION_ID, mLatestNotification);
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            LogHelper.d(TAG, "Session was destroyed, resetting to the new session token");
            try {
                updateSessionToken();
            } catch (RemoteException e) {
                LogHelper.e(TAG, e, "could not connect media controller");
            }
        }
    };

    private Notification createNotification() {
        LogHelper.d(TAG, "updateNotificationMetadata. mMetadata=" + mMetadata);
        if (mMetadata == null || mPlaybackState == null) {
            return null;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService);
        int playPauseButtonPosition = 0;

        // If skip to previous action is enabled
        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            notificationBuilder.addAction(com.sjn.stamp.R.drawable.ic_skip_previous_white_24dp,
                    mService.getString(com.sjn.stamp.R.string.label_previous), mPreviousIntent);

            // If there is a "skip to previous" button, the play/pause button will
            // be the second one. We need to keep track of it, because the MediaStyle notification
            // requires to specify the index of the buttons (actions) that should be visible
            // when in compact view.
            playPauseButtonPosition = 1;
        }

        addPlayPauseAction(notificationBuilder);

        // If skip to next action is enabled
        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            notificationBuilder.addAction(com.sjn.stamp.R.drawable.ic_skip_next_white_24dp,
                    mService.getString(com.sjn.stamp.R.string.label_next), mNextIntent);
        }

        MediaDescriptionCompat description = mMetadata.getDescription();

        notificationBuilder
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(
                                new int[]{playPauseButtonPosition, playPauseButtonPosition + 1})  // show only play/pause in compact view
                        .setMediaSession(mSessionToken))
                .setColor(mNotificationColor)
                .setSmallIcon(R.mipmap.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setUsesChronometer(true)
                .setDeleteIntent(mKillIntent)
                .setContentIntent(createContentIntent(description))
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle());

        if (mController != null && mController.getExtras() != null) {
            String castName = mController.getExtras().getString(CastPlayer.EXTRA_CONNECTED_CAST);
            if (castName != null) {
                String castInfo = mService.getResources()
                        .getString(com.sjn.stamp.R.string.casting_to_device, castName);
                notificationBuilder.setSubText(castInfo);
                notificationBuilder.addAction(com.sjn.stamp.R.drawable.ic_close_black_24dp,
                        mService.getString(com.sjn.stamp.R.string.stop_casting), mStopCastIntent);
            }
        }

        setNotificationPlaybackState(notificationBuilder);

        if (description.getIconUri() != null) {
            fetchBitmapFromURLAsync(description.getIconUri().toString(), notificationBuilder);
        }

        return notificationBuilder.build();
    }

    private void addPlayPauseAction(NotificationCompat.Builder builder) {
        LogHelper.d(TAG, "updatePlayPauseAction");
        String label;
        int icon;
        PendingIntent intent;
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            label = mService.getString(com.sjn.stamp.R.string.label_pause);
            icon = com.sjn.stamp.R.drawable.stamp_ic_pause_white_24dp;
            intent = mPauseIntent;
        } else {
            label = mService.getString(com.sjn.stamp.R.string.label_play);
            icon = com.sjn.stamp.R.drawable.stamp_ic_play_arrow_white_24dp;
            intent = mPlayIntent;
        }
        builder.addAction(new NotificationCompat.Action(icon, label, intent));
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        LogHelper.d(TAG, "updateNotificationPlaybackState. mPlaybackState=" + mPlaybackState);
        if (mPlaybackState == null || !mStarted) {
            LogHelper.d(TAG, "updateNotificationPlaybackState. cancelling notification!");
            LogHelper.i(TAG, "setNotificationPlaybackState stopForeground(true)");
            mService.stopForeground(true);
            return;
        }
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING
                && mPlaybackState.getPosition() >= 0) {
            LogHelper.d(TAG, "updateNotificationPlaybackState. updating playback position to ",
                    (System.currentTimeMillis() - mPlaybackState.getPosition()) / 1000, " seconds");
            builder
                    .setWhen(System.currentTimeMillis() - mPlaybackState.getPosition())
                    .setShowWhen(true)
                    .setUsesChronometer(true);
        } else {
            LogHelper.d(TAG, "updateNotificationPlaybackState. hiding playback position");
            builder
                    .setWhen(0)
                    .setShowWhen(false)
                    .setUsesChronometer(false);
        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    private void fetchBitmapFromURLAsync(final String bitmapUrl,
                                         final NotificationCompat.Builder builder) {
        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (mMetadata != null && mMetadata.getDescription().getIconUri() != null &&
                        mMetadata.getDescription().getIconUri().toString().equals(bitmapUrl)) {
                    LogHelper.d(TAG, "fetchBitmapFromURLAsync: set bitmap to ", bitmapUrl);
                    // If the media is still the same, update the notification:
                    new SetNotificationBitmapAsyncTask(builder, bitmap).execute();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                builder.setLargeIcon(ViewHelper.toBitmap(ViewHelper.createTextDrawable(mMetadata.getDescription().getTitle()), 128, 128));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        BitmapHelper.readBitmapAsync(mService, bitmapUrl, mTarget);
    }

    private class SetNotificationBitmapAsyncTask extends AsyncTask<Void, Void, Void> {
        final NotificationCompat.Builder mBuilder;
        Bitmap mBitmap;

        SetNotificationBitmapAsyncTask(final NotificationCompat.Builder builder, Bitmap bitmap) {
            mBuilder = builder;
            mBitmap = bitmap;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mBuilder.setLargeIcon(mBitmap);
            LogHelper.d(TAG, "fetchBitmapFromURLAsync: finish");
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            LogHelper.d(TAG, "onBitmapLoaded: finish");
            return null;
        }
    }
}
