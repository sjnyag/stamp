package com.sjn.stamp.media;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.sjn.stamp.utils.LogHelper;

import lombok.AllArgsConstructor;

public class MediaLogger {

    private static final String TAG = LogHelper.makeLogTag(MediaLogger.class);
    private static final Long START_WAIT_TIME = 2000L;
    private static final Long START_LIMIT_TIME = 5000L;
    private static final Long PLAY_WAIT_TIME = 20000L;
    private static final Long COMPLETE_REMAIN_TIME = 10000L;
    private Listener mListener;
    static private Handler mTimerHandler;

    public interface Listener {

        MediaMetadataCompat getCurrentMedia();

        int getPlaybackState();

        int getCurrentPosition();

        void onSongStart(@NonNull MediaMetadataCompat metadata);

        void onSongPlay(@NonNull MediaMetadataCompat metadata);

        void onSongSkip(@NonNull MediaMetadataCompat metadata);

        void onSongComplete(@NonNull MediaMetadataCompat metadata);
    }

    public MediaLogger(Listener listener) {
        mListener = listener;
    }

    public void onStart() {
        if (mListener == null) {
            return;
        }
        resetTimerHandler();
        mTimerHandler.postDelayed(new MediaStartTimer(), START_WAIT_TIME);
    }

    public void onComplete(MediaMetadataCompat metadata) {
        if (mListener == null || metadata == null) {
            return;
        }
        mListener.onSongComplete(metadata);
    }

    public void onSkip(MediaMetadataCompat metadata, int position) {
        if (mListener == null || metadata == null) {
            return;
        }
        if (START_WAIT_TIME < position && position < PLAY_WAIT_TIME) {
            mListener.onSongSkip(metadata);
        } else if (metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) - position < COMPLETE_REMAIN_TIME) {
            mListener.onSongComplete(metadata);
        }
    }

    private void resetTimerHandler() {
        if (mTimerHandler == null) {
            mTimerHandler = new Handler();
        } else {
            mTimerHandler.removeCallbacksAndMessages(null);
        }
    }

    private class MediaStartTimer implements Runnable {

        @Override
        public void run() {
            if (mListener != null && mListener.getCurrentMedia() != null && mTimerHandler != null && isStart(mListener.getPlaybackState(), mListener.getCurrentPosition())) {
                mListener.onSongStart(mListener.getCurrentMedia());
                resetTimerHandler();
                mTimerHandler.postDelayed(new MediaPlayTimer(mListener.getCurrentMedia()), PLAY_WAIT_TIME - START_WAIT_TIME);
            }
        }
    }

    @AllArgsConstructor(suppressConstructorProperties = true)
    private class MediaPlayTimer implements Runnable {
        final MediaMetadataCompat mStartMetadata;

        @Override
        public void run() {
            if (mListener != null && mListener.getCurrentMedia() != null && isSongPlaying(mListener.getPlaybackState())
                    && mStartMetadata != null && isSameSong(mStartMetadata, mListener.getCurrentMedia())) {
                mListener.onSongPlay(mListener.getCurrentMedia());
            }
        }
    }

    private static boolean isStart(int state, int position) {
        return isPlaying(state) && position < START_LIMIT_TIME;
    }

    private static boolean isSongPlaying(int state) {
        return isPlaying(state);
    }

    private static boolean isSameSong(@NonNull MediaMetadataCompat startMetadata, @NonNull MediaMetadataCompat playingMetadata) {
        return startMetadata.getDescription().toString().equals(playingMetadata.getDescription().toString());
    }


    private static boolean isPlaying(int state) {
        return state == PlaybackStateCompat.STATE_PLAYING;
    }

}
