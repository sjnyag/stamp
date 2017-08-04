package com.sjn.stamp;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.sjn.stamp.media.playback.Playback;
import com.sjn.stamp.media.playback.PlaybackManager;
import com.sjn.stamp.media.player.CastPlayer;
import com.sjn.stamp.media.player.Player;
import com.sjn.stamp.media.provider.MusicProvider;
import com.sjn.stamp.media.source.LocalMediaSource;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.MediaRetrieveHelper;

import java.util.ArrayList;
import java.util.List;

import static com.sjn.stamp.utils.NotificationHelper.ACTION_CMD;
import static com.sjn.stamp.utils.NotificationHelper.CMD_KILL;
import static com.sjn.stamp.utils.NotificationHelper.CMD_NAME;
import static com.sjn.stamp.utils.NotificationHelper.CMD_PAUSE;
import static com.sjn.stamp.utils.NotificationHelper.CMD_STOP_CASTING;

public class MusicService extends MediaBrowserServiceCompat implements PlaybackManager.PlaybackServiceCallback, Player.PlayerCallback {

    private static final String TAG = LogHelper.makeLogTag(MusicService.class);
    public static final String CUSTOM_ACTION_RELOAD_MUSIC_PROVIDER = "RELOAD_MUSIC_PROVIDER";

    private MediaNotificationManager mMediaNotificationManager;
    private Player mPlayer;
    private CastPlayer mCastPlayer;
    private MusicProvider mMusicProvider;

    @Override
    public void onCreate() {
        super.onCreate();
        LogHelper.d(TAG, "onCreate");
        mMusicProvider = new MusicProvider(this, new LocalMediaSource(this, new MediaRetrieveHelper.PermissionRequiredCallback() {
            @Override
            public void onPermissionRequired() {

            }
        }));
        mMusicProvider.retrieveMediaAsync(
                new MusicProvider.Callback() {
                    @Override
                    public void onMusicCatalogReady(boolean success) {
                        LogHelper.d(TAG, "MusicProvider.callBack start");
                        mPlayer = new Player(MusicService.this, MusicService.this);
                        setSessionToken(mPlayer.initialize(MusicService.this, mMusicProvider));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mPlayer.restorePreviousState(mMusicProvider);
                                mCastPlayer = new CastPlayer(MusicService.this, mPlayer.getSessionManageListener());
                                try {
                                    mMediaNotificationManager = new MediaNotificationManager(MusicService.this);
                                } catch (RemoteException e) {
                                    throw new IllegalStateException("Could not create a MediaNotificationManager", e);
                                }
                                LogHelper.d(TAG, "MusicProvider.callBack end");
                            }
                        }).start();
                    }
                }
        );
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        LogHelper.d(TAG, "onStartCommand");
        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action)) {
                handleActionCommand(command);
            } else if (mPlayer != null) {
                mPlayer.handleIntent(startIntent);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogHelper.d(TAG, "onDestroy");
        if (mPlayer != null) {
            mPlayer.stop();
        }
        if (mMediaNotificationManager != null) {
            mMediaNotificationManager.stopNotification();
        }
        if (mCastPlayer != null) {
            mCastPlayer.finish();
        }
    }

    /**
     * {@link MediaBrowserServiceCompat}
     */
    @Override
    public void onCustomAction(@NonNull String action, Bundle extras,
                               @NonNull Result<Bundle> result) {
        LogHelper.d(TAG, "onCustomAction " + action);
        switch (action) {
            case CUSTOM_ACTION_RELOAD_MUSIC_PROVIDER:
                result.detach();
                mMusicProvider.cacheAndNotifyLatestMusicMap();
                return;
        }
        result.sendError(null);
    }

    @Override
    public void onSearch(@NonNull final String query, Bundle extras,
                         final @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        LogHelper.d(TAG, "onSearch " + query);
        result.detach();
        new Thread(new Runnable() {
            @Override
            public void run() {
                result.sendResult(mMusicProvider.getChildren(query, getResources()));
            }
        }).start();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid,
                                 Bundle rootHints) {
        LogHelper.d(TAG, "OnGetRoot: clientPackageName=" + clientPackageName,
                "; clientUid=" + clientUid + " ; rootHints=", rootHints);
        return new BrowserRoot(MediaIDHelper.MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaItem>> result) {
        LogHelper.d(TAG, "OnLoadChildren: parentMediaId=", parentMediaId);
        if (MediaIDHelper.MEDIA_ID_EMPTY_ROOT.equals(parentMediaId)) {
            result.sendResult(new ArrayList<MediaItem>());
        } else if (mMusicProvider.isInitialized()) {
            result.detach();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
                }
            }).start();
        } else {
            result.detach();
        }
    }

    /**
     * {@link PlaybackManager.PlaybackServiceCallback}
     */
    @Override
    public void onPlaybackStart() {
        mPlayer.setActive(true);
        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), MusicService.class));
        if (mMediaNotificationManager != null) {
            mMediaNotificationManager.startForeground();
        }
    }

    @Override
    public void onPlaybackStop() {
        mPlayer.setActive(false);
        if (mMediaNotificationManager != null) {
            mMediaNotificationManager.stopForeground(true);
        }
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mPlayer.setPlaybackState(newState);
    }

    @Override
    public void onNotificationRequired() {
        mMediaNotificationManager.startNotification();
    }

    /**
     * {@link Player.PlayerCallback}
     */
    @Override
    public Playback requestPlayback(Playback.Type type) {
        LogHelper.d(TAG, "requestPlayback");
        return type.createInstance(mMusicProvider, this);
    }

    private void handleActionCommand(String command) {
        switch (command) {
            case CMD_PAUSE:
                mPlayer.pause();
                break;
            case CMD_STOP_CASTING:
                if (mCastPlayer != null) {
                    mCastPlayer.stop();
                }
                break;
            case CMD_KILL:
                if (mPlayer != null) {
                    stopSelf();
                }
                break;
        }
    }

}
