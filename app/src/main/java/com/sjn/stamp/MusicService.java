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

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.sjn.stamp.media.playback.Playback;
import com.sjn.stamp.media.playback.PlaybackManager;
import com.sjn.stamp.media.player.CarPlayer;
import com.sjn.stamp.media.player.CastPlayer;
import com.sjn.stamp.media.player.Player;
import com.sjn.stamp.media.provider.MusicProvider;
import com.sjn.stamp.media.source.LocalMediaSource;
import com.sjn.stamp.utils.CarHelper;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.MediaRetrieveHelper;
import com.sjn.stamp.utils.WearHelper;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.ArrayList;
import java.util.List;

import static com.sjn.stamp.utils.NotificationHelper.ACTION_CMD;
import static com.sjn.stamp.utils.NotificationHelper.CMD_KILL;
import static com.sjn.stamp.utils.NotificationHelper.CMD_NAME;
import static com.sjn.stamp.utils.NotificationHelper.CMD_PAUSE;
import static com.sjn.stamp.utils.NotificationHelper.CMD_STOP_CASTING;

/**
 * This class provides a MediaBrowser through a service. It exposes the media library to a browsing
 * client, through the onGetRoot and onLoadChildren methods. It also creates a MediaSession and
 * exposes it through its MediaSession.Token, which allows the client to create a MediaController
 * that connects to and send control commands to the MediaSession remotely. This is useful for
 * user interfaces that need to interact with your media session, like Android Auto. You can
 * (should) also use the same service from your app's UI, which gives a seamless playback
 * experience to the user.
 * <p>
 * To implement a MediaBrowserService, you need to:
 * <p>
 * <ul>
 * <p>
 * <li> Extend {@link android.service.media.MediaBrowserService}, implementing the media browsing
 * related methods {@link android.service.media.MediaBrowserService#onGetRoot} and
 * {@link android.service.media.MediaBrowserService#onLoadChildren};
 * <li> In onCreate, start a new {@link android.media.session.MediaSession} and notify its parent
 * with the session's token {@link android.service.media.MediaBrowserService#setSessionToken};
 * <p>
 * <li> Set a callback on the
 * {@link android.media.session.MediaSession#setCallback(android.media.session.MediaSession.Callback)}.
 * The callback will receive all the user's actions, like play, pause, etc;
 * <p>
 * <li> Handle all the actual music playing using any method your app prefers (for example,
 * {@link android.media.MediaPlayer})
 * <p>
 * <li> Update playbackState, "now playing" metadata and queue, using MediaSession proper methods
 * {@link android.media.session.MediaSession#setPlaybackState(android.media.session.PlaybackState)}
 * {@link android.media.session.MediaSession#setMetadata(android.media.MediaMetadata)} and
 * {@link android.media.session.MediaSession#setQueue(java.util.List)})
 * <p>
 * <li> Declare and export the service in AndroidManifest with an intent receiver for the action
 * android.media.browse.MediaBrowserService
 * <p>
 * </ul>
 * <p>
 * To make your app compatible with Android Auto, you also need to:
 * <p>
 * <ul>
 * <p>
 * <li> Declare a meta-data tag in AndroidManifest.xml linking to a xml resource
 * with a &lt;automotiveApp&gt; root element. For a media app, this must include
 * an &lt;uses name="media"/&gt; element as a child.
 * For example, in AndroidManifest.xml:
 * &lt;meta-data android:name="com.google.android.gms.car.application"
 * android:resource="@xml/automotive_app_desc"/&gt;
 * And in res/values/automotive_app_desc.xml:
 * &lt;automotiveApp&gt;
 * &lt;uses name="media"/&gt;
 * &lt;/automotiveApp&gt;
 * <p>
 * </ul>
 *
 * @see <a href="README.md">README.md</a> for more details.
 */
public class MusicService extends MediaBrowserServiceCompat
        implements MediaRetrieveHelper.PermissionRequiredCallback,
        PlaybackManager.PlaybackServiceCallback,
        Player.PlayerCallback {

    private static final String TAG = LogHelper.makeLogTag(MusicService.class);

    public static final String CUSTOM_ACTION_RELOAD_MUSIC_PROVIDER = "RELOAD_MUSIC_PROVIDER";

    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 30000;

    /*
    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);
    */

    private PackageValidator mPackageValidator;
    private MediaNotificationManager mMediaNotificationManager;

    private Player mPlayer;
    private CarPlayer mCarPlayer;
    private CastPlayer mCastPlayer;
    private MusicProvider mMusicProvider;

    /*
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        LogHelper.d(TAG, "onCreate");
        mPackageValidator = new PackageValidator(this);
        mMusicProvider = new MusicProvider(this, new LocalMediaSource(this, this));
        // To make the app more responsive, fetch and cache catalog information now.
        // This can help improve the response time in the method
        // {@link #onLoadChildren(String, Result<List<MediaItem>>) onLoadChildren()}.
        mMusicProvider.retrieveMediaAsync(
                new MusicProvider.Callback() {
                    @Override
                    public void onMusicCatalogReady(boolean success) {
                        mPlayer = new Player(MusicService.this, MusicService.this);
                        setSessionToken(mPlayer.initialize(MusicService.this, mMusicProvider));
                        mPlayer.restorePreviousState(mMusicProvider);
                        mCarPlayer = new CarPlayer(MusicService.this);
                        mCastPlayer = new CastPlayer(MusicService.this, mPlayer.getSessionManageListener());
                        try {
                            mMediaNotificationManager = new MediaNotificationManager(MusicService.this);
                        } catch (RemoteException e) {
                            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
                        }
                        mCarPlayer.registerCarConnectionReceiver();
                    }
                }
        );
    }

    /**
     * (non-Javadoc)
     *
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        LogHelper.d(TAG, "onStartCommand");
        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
                    mPlayer.pause();
                } else if (CMD_STOP_CASTING.equals(command)) {
                    if (mPlayer != null) {
                        mCastPlayer.stop();
                    }
                } else if (CMD_KILL.equals(command)) {
                    if (mPlayer != null) {
                        stopSelf();
                    }
                }
            } else {
                // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
                if (mPlayer != null) {
                    mPlayer.handleIntent(startIntent);
                }
            }
        }
        /*
        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        */
        return START_STICKY;
    }

    /**
     * (non-Javadoc)
     *
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        LogHelper.d(TAG, "onDestroy");
        if (mCarPlayer != null) {
            mCarPlayer.unregisterCarConnectionReceiver();
        }
        // Service is being killed, so make sure we release our resources
        if (mPlayer != null) {
            mPlayer.stop();
        }
        if (mMediaNotificationManager != null) {
            mMediaNotificationManager.stopNotification();
        }
        if (mCastPlayer != null) {
            mCastPlayer.finish();
        }
        /*
        if (mDelayedStopHandler != null) {
            mDelayedStopHandler.removeCallbacksAndMessages(null);
        }
        */
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid,
                                 Bundle rootHints) {
        LogHelper.d(TAG, "OnGetRoot: clientPackageName=" + clientPackageName,
                "; clientUid=" + clientUid + " ; rootHints=", rootHints);
        // To ensure you are not allowing any arbitrary app to browse your app's contents, you
        // need to check the origin:
        if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            // If the request comes from an untrusted package, return an empty browser root.
            // If you return null, then the media browser will not be able to connect and
            // no further calls will be made to other media browsing methods.
            LogHelper.i(TAG, "OnGetRoot: Browsing NOT ALLOWED for unknown caller. "
                    + "Returning empty browser root so all apps can use MediaController."
                    + clientPackageName);
            return new MediaBrowserServiceCompat.BrowserRoot(MediaIDHelper.MEDIA_ID_EMPTY_ROOT, null);
        }
        //noinspection StatementWithEmptyBody
        if (CarHelper.isValidCarPackage(clientPackageName)) {
            // Optional: if your app needs to adapt the music library to show a different subset
            // when connected to the car, this is where you should handle it.
            // If you want to adapt other runtime behaviors, like tweak ads or change some behavior
            // that should be different on cars, you should instead use the boolean flag
            // set by the BroadcastReceiver mCarConnectionReceiver (mIsConnectedToCar).
        }
        //noinspection StatementWithEmptyBody
        if (WearHelper.isValidWearCompanionPackage(clientPackageName)) {
            // Optional: if your app needs to adapt the music library for when browsing from a
            // Wear device, you should return a different MEDIA ROOT here, and then,
            // on onLoadChildren, handle it accordingly.
        }

        return new BrowserRoot(MediaIDHelper.MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaItem>> result) {
        LogHelper.d(TAG, "OnLoadChildren: parentMediaId=", parentMediaId);
        if (MediaIDHelper.MEDIA_ID_EMPTY_ROOT.equals(parentMediaId)) {
            result.sendResult(new ArrayList<MediaItem>());
        } else if (mMusicProvider.isInitialized()) {
            // if music library is ready, return immediately
            result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources(), null, null, null));
        } else {
            // otherwise, only return results when the music library is retrieved
            result.detach();
//            mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
//                @Override
//                public void onMusicCatalogReady(boolean success) {
//                    result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources(), null, null, null));
//                }
//            });
        }
    }

    @Override
    public void onPermissionRequired() {
    }

    /**
     * Callback method called from PlaybackManager whenever the music is about to play.
     */
    @Override
    public void onPlaybackStart() {
        LogHelper.i(TAG, "onPlaybackStart");
        mPlayer.setActive(true);
        /*
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        */
        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), MusicService.class));
        if (mMediaNotificationManager != null) {
            LogHelper.i(TAG, "onPlaybackStart startForeground()");
            mMediaNotificationManager.startForeground();
        }
    }


    /**
     * Callback method called from PlaybackManager whenever the music stops playing.
     */
    @Override
    public void onPlaybackStop() {
        LogHelper.i(TAG, "onPlaybackStop");
        mPlayer.setActive(false);
        // Reset the delayed stop handler, so after STOP_DELAY it will be executed again,
        // potentially stopping the service.
        /*
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        */
        if (mMediaNotificationManager != null) {
            LogHelper.i(TAG, "onPlaybackStop stopForeground(true)");
            mMediaNotificationManager.stopForeground(true);
        }
    }

    @Override
    public void onNotificationRequired() {
        mMediaNotificationManager.startNotification();
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mPlayer.setPlaybackState(newState);
    }

    @Override
    public Playback requestPlayback(Playback.Type type) {
        return type.createInstance(mMusicProvider, this);
    }

    @Override
    public void onCustomAction(@NonNull String action, Bundle extras,
                               @NonNull Result<Bundle> result) {
        switch (action) {
            case CUSTOM_ACTION_RELOAD_MUSIC_PROVIDER:
                result.detach();
                mMusicProvider.cacheAndNotifyLatestMusicMap();
                return;
        }
        result.sendError(null);
    }

    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
//    private static class DelayedStopHandler extends Handler {
//        private final WeakReference<MusicService> mWeakReference;
//
//        private DelayedStopHandler(MusicService service) {
//            mWeakReference = new WeakReference<>(service);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            MusicService service = mWeakReference.get();
//            if (service != null && !service.mPlayer.isPlaying()) {
//                LogHelper.d(TAG, "Stopping service with delay handler.");
//                service.stopSelf();
//            }
//        }
//    }

}
