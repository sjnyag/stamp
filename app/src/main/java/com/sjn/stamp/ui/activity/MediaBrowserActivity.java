package com.sjn.stamp.ui.activity;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.sjn.stamp.MusicService;
import com.sjn.stamp.ui.MediaBrowsable;
import com.sjn.stamp.ui.observer.MediaControllerObserver;
import com.sjn.stamp.utils.DrawerHelper;
import com.sjn.stamp.utils.LogHelper;

/**
 * Base activity for activities that need to show a playback control fragment when media is playing.
 */
public abstract class MediaBrowserActivity extends DrawerActivity
        implements MediaBrowsable, MediaControllerObserver.Listener {

    private static final String TAG = LogHelper.makeLogTag(MediaBrowserActivity.class);

    private MediaBrowserCompat mMediaBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.i(TAG, "Activity onCreate");
        MediaControllerObserver.getInstance().addListener(this);
        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), mConnectionCallback, null);
        mMediaBrowser.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogHelper.i(TAG, "Activity onDestroy");
        MediaControllerObserver.getInstance().removeListener(this);
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerObserver.unregister(MediaControllerCompat.getMediaController(this));
        }
        mMediaBrowser.disconnect();
    }

    @Override
    final public void sendCustomAction(String action, Bundle extras, MediaBrowserCompat.CustomActionCallback callback) {
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            mMediaBrowser.sendCustomAction(action, extras, callback);
        }
    }

    @Override
    final public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    @Override
    final public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
    }

    @Override
    final public void onMetadataChanged(MediaMetadataCompat metadata) {
        LogHelper.d(TAG, "onMetadataChanged");
        DrawerHelper.INSTANCE.updateHeader(this, metadata, mAccountHeader);
    }

    @Override
    public void onMediaControllerConnected() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        LogHelper.d(TAG, "onMediaControllerConnected, mediaController==null? ", controller == null);
        if (controller != null) {
            onMetadataChanged(controller.getMetadata());
            onPlaybackStateChanged(controller.getPlaybackState());
        }
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    LogHelper.i(TAG, "onMediaControllerConnected");
                    try {
                        LogHelper.i(TAG, "connectToSession");
                        MediaControllerCompat mediaController = new MediaControllerCompat(MediaBrowserActivity.this, mMediaBrowser.getSessionToken());
                        MediaControllerCompat.setMediaController(MediaBrowserActivity.this, mediaController);
                        MediaControllerObserver.register(mediaController);
                        MediaControllerObserver.getInstance().notifyConnected();
                    } catch (RemoteException e) {
                        LogHelper.e(TAG, e, "could not connect media controller");
                    }
                }
            };

}
