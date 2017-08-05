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
package com.sjn.stamp.media.playback;

import android.content.Context;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;

import org.json.JSONException;
import org.json.JSONObject;

import static android.support.v4.media.session.MediaSessionCompat.QueueItem;

/**
 * An implementation of Playback that talks to Cast.
 */
class CastPlayback implements Playback {

    private static final String TAG = LogHelper.makeLogTag(CastPlayback.class);

    private static final String MIME_TYPE_AUDIO_MPEG = "audio/mpeg";
    static final String ITEM_ID = "itemId";

    final Context mAppContext;
    final RemoteMediaClient mRemoteMediaClient;
    private final RemoteMediaClient.Listener mRemoteMediaClientListener;

    /**
     * The current PlaybackState
     */
    int mState;
    /**
     * Callback for making completion/error calls on
     */
    private Callback mCallback;
    volatile int mCurrentPosition;
    volatile String mCurrentMediaId;

    CastPlayback(Context context) {
        mAppContext = context.getApplicationContext();

        CastSession castSession = CastContext.getSharedInstance(mAppContext).getSessionManager()
                .getCurrentCastSession();
        mRemoteMediaClient = castSession.getRemoteMediaClient();
        mRemoteMediaClientListener = new CastMediaClientListener();
    }

    @Override
    public void start() {
        mRemoteMediaClient.addListener(mRemoteMediaClientListener);
    }

    @Override
    public void stop(boolean notifyListeners) {
        mRemoteMediaClient.removeListener(mRemoteMediaClientListener);
        mState = PlaybackStateCompat.STATE_STOPPED;
        if (notifyListeners && mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
    }

    @Override
    public void setState(int state) {
        this.mState = state;
    }

    @Override
    public int getCurrentStreamPosition() {
        if (!isConnected()) {
            return mCurrentPosition;
        }
        return (int) mRemoteMediaClient.getApproximateStreamPosition();
    }

    @Override
    public void setCurrentStreamPosition(int pos) {
        this.mCurrentPosition = pos;
    }

    @Override
    public void updateLastKnownStreamPosition() {
        mCurrentPosition = getCurrentStreamPosition();
    }

    @Override
    public void play(QueueItem item) {
        try {
            loadMedia(item, true);
            mState = PlaybackStateCompat.STATE_BUFFERING;
            if (mCallback != null) {
                mCallback.onPlaybackStatusChanged(mState);
            }
        } catch (JSONException e) {
            LogHelper.e(TAG, "Exception loading media ", e, null);
            if (mCallback != null) {
                mCallback.onError(e.getMessage());
            }
        }
    }

    @Override
    public void pause() {
        if (mRemoteMediaClient.hasMediaSession()) {
            mRemoteMediaClient.pause();
            mCurrentPosition = (int) mRemoteMediaClient.getApproximateStreamPosition();
        }
    }

    @Override
    public void seekTo(int position) {
        if (mCurrentMediaId == null) {
            if (mCallback != null) {
                mCallback.onError("seekTo cannot be calling in the absence of mediaId.");
            }
            return;
        }
        if (mRemoteMediaClient.hasMediaSession()) {
            mRemoteMediaClient.seek(position);
            mCurrentPosition = position;
        }
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        this.mCurrentMediaId = mediaId;
    }

    @Override
    public String getCurrentMediaId() {
        return mCurrentMediaId;
    }

    @Override
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public boolean isConnected() {
        CastSession castSession = CastContext.getSharedInstance(mAppContext).getSessionManager()
                .getCurrentCastSession();
        return (castSession != null && castSession.isConnected());
    }

    @Override
    public boolean isPlaying() {
        return isConnected() && mRemoteMediaClient.isPlaying();
    }

    @Override
    public int getState() {
        return mState;
    }

    protected void loadMedia(QueueItem item, boolean autoPlay) throws JSONException {
        String mediaId = item.getDescription().getMediaId();
        if (mediaId == null || mediaId.isEmpty()) {
            throw new IllegalArgumentException("Invalid mediaId");
        }
        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
        if (musicId == null || musicId.isEmpty()) {
            throw new IllegalArgumentException("Invalid mediaId");
        }
        if (!TextUtils.equals(mediaId, mCurrentMediaId) || mState != PlaybackStateCompat.STATE_PAUSED) {
            mCurrentMediaId = mediaId;
            mCurrentPosition = 0;
        }
        JSONObject customData = new JSONObject();
        customData.put(ITEM_ID, mediaId);
        MediaInfo media = toCastMediaMetadata(item, customData);
        mRemoteMediaClient.load(media, autoPlay, mCurrentPosition, customData);
    }

    /**
     * Helper method to convert a {@link android.media.MediaMetadata} to a
     * {@link com.google.android.gms.cast.MediaInfo} used for sending media to the receiver app.
     *
     * @param track      {@link com.google.android.gms.cast.MediaMetadata}
     * @param customData custom data specifies the local mediaId used by the player.
     * @return mediaInfo {@link com.google.android.gms.cast.MediaInfo}
     */
    private static MediaInfo toCastMediaMetadata(QueueItem track,
                                                 JSONObject customData) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE,
                track.getDescription().getTitle() == null ? "" :
                        track.getDescription().getTitle().toString());
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE,
                track.getDescription().getSubtitle() == null ? "" :
                        track.getDescription().getSubtitle().toString());
        if (track.getDescription().getExtras() != null) {
            mediaMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST,
                    track.getDescription().getExtras().getString((MediaMetadataCompat.METADATA_KEY_ARTIST)));
            mediaMetadata.putString(MediaMetadata.KEY_ARTIST,
                    track.getDescription().getExtras().getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            mediaMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE,
                    track.getDescription().getExtras().getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
        }
        WebImage image = new WebImage(
                new Uri.Builder().encodedPath(track.getDescription().getIconUri().toString())
                        .build());
        // First image is used by the receiver for showing the audio album art.
        mediaMetadata.addImage(image);
        // Second image is used by Cast Companion Library on the full screen activity that is shown
        // when the cast dialog is clicked.
        mediaMetadata.addImage(image);

        //noinspection ResourceType
        return new MediaInfo.Builder(track.getDescription().getMediaUri().toString())
                .setContentType(MIME_TYPE_AUDIO_MPEG)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .setCustomData(customData)
                .build();
    }

    private void setMetadataFromRemote() {
        // Sync: We get the customData from the remote media information and update the local
        // metadata if it happens to be different from the one we are currently using.
        // This can happen when the app was either restarted/disconnected + connected, or if the
        // app joins an existing session while the Chromecast was playing a queue.
        try {
            MediaInfo mediaInfo = mRemoteMediaClient.getMediaInfo();
            if (mediaInfo == null) {
                return;
            }
            JSONObject customData = mediaInfo.getCustomData();

            if (customData != null && customData.has(ITEM_ID)) {
                String remoteMediaId = customData.getString(ITEM_ID);
                if (!TextUtils.equals(mCurrentMediaId, remoteMediaId)) {
                    mCurrentMediaId = remoteMediaId;
                    if (mCallback != null) {
                        mCallback.setCurrentMediaId(remoteMediaId);
                    }
                    updateLastKnownStreamPosition();
                }
            }
        } catch (JSONException e) {
            LogHelper.e(TAG, e, "Exception processing update metadata");
        }
    }

    private void updatePlaybackState() {
        int status = mRemoteMediaClient.getPlayerState();
        int idleReason = mRemoteMediaClient.getIdleReason();

        LogHelper.d(TAG, "onRemoteMediaPlayerStatusUpdated ", status);

        // Convert the remote playback states to media playback states.
        switch (status) {
            case MediaStatus.PLAYER_STATE_IDLE:
                if (idleReason == MediaStatus.IDLE_REASON_FINISHED) {
                    if (mCallback != null) {
                        mCallback.onCompletion();
                    }
                }
                break;
            case MediaStatus.PLAYER_STATE_BUFFERING:
                mState = PlaybackStateCompat.STATE_BUFFERING;
                if (mCallback != null) {
                    mCallback.onPlaybackStatusChanged(mState);
                }
                break;
            case MediaStatus.PLAYER_STATE_PLAYING:
                mState = PlaybackStateCompat.STATE_PLAYING;
                setMetadataFromRemote();
                if (mCallback != null) {
                    mCallback.onPlaybackStatusChanged(mState);
                }
                break;
            case MediaStatus.PLAYER_STATE_PAUSED:
                mState = PlaybackStateCompat.STATE_PAUSED;
                setMetadataFromRemote();
                if (mCallback != null) {
                    mCallback.onPlaybackStatusChanged(mState);
                }
                break;
            default: // case unknown
                LogHelper.d(TAG, "State default : ", status);
                break;
        }
    }

    private class CastMediaClientListener implements RemoteMediaClient.Listener {
        private int mLastStatus = -1;

        @Override
        public void onMetadataUpdated() {
            LogHelper.d(TAG, "RemoteMediaClient.onMetadataUpdated");
            setMetadataFromRemote();
        }

        @Override
        public void onStatusUpdated() {
            LogHelper.d(TAG, "RemoteMediaClient.onStatusUpdated");
            if (mLastStatus != mRemoteMediaClient.getPlayerState()) {
                updatePlaybackState();
            }
            mLastStatus = mRemoteMediaClient.getPlayerState();
        }

        @Override
        public void onSendingRemoteMediaRequest() {
        }

        @Override
        public void onQueueStatusUpdated() {
        }

        @Override
        public void onPreloadStatusUpdated() {
        }
    }
}
