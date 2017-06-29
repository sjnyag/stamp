package com.sjn.stamp.media.player;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.sjn.stamp.ui.activity.NowPlayingActivity;
import com.sjn.stamp.utils.CarHelper;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.WearHelper;

import java.util.List;

public class SessionManager implements SessionManagerListener<CastSession> {

    private static final String TAG = LogHelper.makeLogTag(SessionManager.class);

    private MediaRouter mMediaRouter;
    private MediaSessionCompat mSession;
    private Bundle mSessionExtras;
    private SessionListener mSessionListener;

    interface SessionListener {
        void toLocalPlayback();

        void toCastCallback();

        void onSessionEnd();
    }

    public SessionManager(Context context, MediaSessionCompat.Callback callback, SessionListener sessionListener) {
        LogHelper.i(TAG, "constructor");
        mSessionListener = sessionListener;
        initialize(context, callback);
    }

    public MediaSessionCompat.Token getSessionToken() {
        if (mSession == null) {
            return null;
        }
        return mSession.getSessionToken();
    }

    private void initialize(Context context, MediaSessionCompat.Callback callback) {
        LogHelper.i(TAG, "initialize");
        mSessionExtras = makeSessionExtras();
        mSession = createSession(context, callback, mSessionExtras);
        mMediaRouter = MediaRouter.getInstance(context.getApplicationContext());
    }

    public void setMetadata(MediaMetadataCompat metadata) {
        if (mSession == null) {
            return;
        }
        mSession.setMetadata(metadata);
    }

    public void updateQueue(List<MediaSessionCompat.QueueItem> newQueue, String title) {
        if (mSession == null) {
            return;
        }
        mSession.setQueue(newQueue);
        mSession.setQueueTitle(title);
    }

    public void handleIntent(Intent startIntent) {
        if (mSession == null) {
            return;
        }
        MediaButtonReceiver.handleIntent(mSession, startIntent);
    }

    public void release() {
        LogHelper.i(TAG, "release");
        if (mSession == null) {
            return;
        }
        mSession.release();
    }

    public void setActive(boolean active) {
        LogHelper.i(TAG, "setActive");
        if (mSession == null) {
            return;
        }
        mSession.setActive(active);
    }

    public void setPlaybackState(PlaybackStateCompat playbackState) {
        LogHelper.i(TAG, "setPlaybackState");
        if (mSession == null) {
            return;
        }
        mSession.setPlaybackState(playbackState);
    }

    private Bundle makeSessionExtras() {
        Bundle sessionExtras = new Bundle();
        CarHelper.setSlotReservationFlags(sessionExtras, true, true, true);
        WearHelper.setSlotReservationFlags(sessionExtras, true, true);
        WearHelper.setUseBackgroundFromTheme(sessionExtras, true);
        return sessionExtras;
    }

    private MediaSessionCompat createSession(Context context, MediaSessionCompat.Callback callback, Bundle sessionExtra) {
        LogHelper.i(TAG, "createSession");
        // Start a new MediaSession
        MediaSessionCompat session = new MediaSessionCompat(context, "MusicService");
        session.setCallback(callback);
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        context = context.getApplicationContext();
        Intent intent = new Intent(context, NowPlayingActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        session.setSessionActivity(pi);
        session.setExtras(sessionExtra);
        return session;
    }

    /**
     * Session Manager Listener responsible for switching the Playback instances
     * depending on whether it is connected to a remote player.
     */
    @Override
    public void onSessionEnded(CastSession session, int error) {
        LogHelper.i(TAG, "onSessionEnded error: ", error);
        if (mSessionListener == null || mSessionExtras == null || mSession == null || mMediaRouter == null) {
            return;
        }
        LogHelper.d(TAG, "onSessionEnded");
        mSessionExtras.remove(CastPlayer.EXTRA_CONNECTED_CAST);
        mSession.setExtras(mSessionExtras);
        mMediaRouter.setMediaSessionCompat(null);
        mSessionListener.toLocalPlayback();
    }

    @Override
    public void onSessionResumed(CastSession session, boolean wasSuspended) {
    }

    @Override
    public void onSessionStarted(CastSession session, String sessionId) {
        LogHelper.i(TAG, "onSessionStarted sessionId: ", sessionId);
        if (mSessionListener == null || mSessionExtras == null || mSession == null || mMediaRouter == null) {
            return;
        }
        // In case we are casting, send the device name as an extra on MediaSession metadata.
        mSessionExtras.putString(CastPlayer.EXTRA_CONNECTED_CAST,
                session.getCastDevice().getFriendlyName());
        mSession.setExtras(mSessionExtras);
        mMediaRouter.setMediaSessionCompat(mSession);
        mSessionListener.toCastCallback();
    }

    @Override
    public void onSessionStarting(CastSession session) {
    }

    @Override
    public void onSessionStartFailed(CastSession session, int error) {
    }

    @Override
    public void onSessionEnding(CastSession session) {
        LogHelper.i(TAG, "onSessionEnding session: ", session);
        if (mSessionListener == null) {
            return;
        }
        // This is our final chance to update the underlying stream position
        // In onSessionEnded(), the underlying CastPlayback#mRemoteMediaClient
        // is disconnected and hence we update our local value of stream position
        // to the latest position.
        mSessionListener.onSessionEnd();
    }

    @Override
    public void onSessionResuming(CastSession session, String sessionId) {
    }

    @Override
    public void onSessionResumeFailed(CastSession session, int error) {
    }

    @Override
    public void onSessionSuspended(CastSession session, int reason) {
    }
}
