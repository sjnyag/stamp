package com.sjn.taggingplayer.media.player;


import android.content.Context;
import android.content.Intent;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.gms.cast.framework.SessionManagerListener;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.controller.SongHistoryController;
import com.sjn.taggingplayer.media.CustomController;
import com.sjn.taggingplayer.media.QueueManager;
import com.sjn.taggingplayer.media.playback.Playback;
import com.sjn.taggingplayer.media.playback.PlaybackManager;
import com.sjn.taggingplayer.media.provider.MusicProvider;

import java.util.List;

public class Player implements SessionManager.SessionListener {

    private PlayerCallback mPlayerCallback;
    private Context mContext;
    private PlaybackManager mPlaybackManager;
    private SessionManager mSessionManager;

    private QueueUpdateListener mQueueUpdateListener;

    public interface PlayerCallback {
        Playback requestPlayback(Playback.Type type);
    }

    public Player(Context context, PlayerCallback playerCallback) {
        mContext = context;
        mPlayerCallback = playerCallback;
    }

    public MediaSessionCompat.Token initialize(PlaybackManager.PlaybackServiceCallback callback, MusicProvider musicProvider) {
        Playback playback = mPlayerCallback.requestPlayback(Playback.Type.LOCAL);
        QueueManager queueManager = initializeQueueManager(musicProvider);
        mPlaybackManager = new PlaybackManager(callback, mContext.getResources(), musicProvider, queueManager, playback,
                new SongHistoryController(mContext));
        mPlaybackManager.updatePlaybackState(null);
        mSessionManager = new SessionManager(this);
        return mSessionManager.initialize(mContext, getMediaSessionCallback());

    }

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mPlaybackManager.getMediaSessionCallback();
    }

    public void stop() {
        mPlaybackManager.handleStopRequest(null);
        mSessionManager.release();
    }

    public void pause() {
        mPlaybackManager.handlePauseRequest();
    }

    public void handleIntent(Intent startIntent) {
        mSessionManager.handleIntent(startIntent);
    }

    public void setActive(boolean active) {
        mSessionManager.setActive(active);
    }

    public void setPlaybackState(PlaybackStateCompat playbackState) {
        if (mSessionManager != null) {
            mSessionManager.setPlaybackState(playbackState);
        }
    }

    private QueueManager initializeQueueManager(MusicProvider musicProvider) {
        mQueueUpdateListener = new QueueUpdateListener();
        QueueManager queueManager = new QueueManager(mContext, musicProvider, mContext.getResources(), mQueueUpdateListener);
        musicProvider.setQueueListener(queueManager);
        return queueManager;
    }

    public boolean isPlaying() {
        return mPlaybackManager.getPlayback() != null && mPlaybackManager.getPlayback().isPlaying();
    }

    public SessionManagerListener getSessionManageListener() {
        return mSessionManager;
    }

    @Override
    public void toLocalPlayback() {
        mPlaybackManager.switchToPlayback(mPlayerCallback.requestPlayback(Playback.Type.LOCAL), false);
    }

    @Override
    public void toCastCallback() {
        mPlaybackManager.switchToPlayback(mPlayerCallback.requestPlayback(Playback.Type.CAST), true);
    }

    @Override
    public void onSessionEnd() {
        mPlaybackManager.getPlayback().updateLastKnownStreamPosition();
    }

    private class QueueUpdateListener implements QueueManager.MetadataUpdateListener {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            mSessionManager.setMetadata(metadata);
        }

        @Override
        public void onMetadataRetrieveError() {
            if (mPlaybackManager != null && mContext != null) {
                mPlaybackManager.updatePlaybackState(
                        mContext.getString(R.string.error_no_metadata));
            }
        }

        @Override
        public void onCurrentQueueIndexUpdated(int queueIndex) {
            if (mPlaybackManager != null) {
                mPlaybackManager.handlePlayRequest();
            }
        }

        @Override
        public void onQueueUpdated(String title,
                                   List<MediaSessionCompat.QueueItem> newQueue) {
            mSessionManager.updateQueue(newQueue, title);
        }
    }
}
