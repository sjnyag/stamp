package com.sjn.stamp.media;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.media.session.PlaybackStateCompat;

public class MediaLogger {

    private static final Long START_WAIT_TIME = 2000L;
    private static final Long START_LIMIT_TIME = 5000L;
    private static final Long PLAY_WAIT_TIME = 20000L;
    private Listener mListener;
    static private Handler mTimerHandler;

    public interface Listener {

        String getCurrentMediaId();

        int getPlaybackState();

        int getCurrentPosition();

        void onSongStart(@NonNull String mediaId);

        void onSongPlay(@NonNull String mediaId);

        void onSongSkip(@NonNull String mediaId);

        void onSongComplete(@NonNull String mediaId);
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

    public void onComplete(String mediaId) {
        if (mListener == null || mediaId == null) {
            return;
        }
        mListener.onSongComplete(mediaId);
    }

    public void onSkip(String mediaId, int position) {
        if (mListener == null || mediaId == null) {
            return;
        }
        if (START_WAIT_TIME < position && position < PLAY_WAIT_TIME) {
            mListener.onSongSkip(mediaId);
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
            if (mListener != null && mListener.getCurrentMediaId() != null && mTimerHandler != null && isStart(mListener.getPlaybackState(), mListener.getCurrentPosition())) {
                mListener.onSongStart(mListener.getCurrentMediaId());
                resetTimerHandler();
                mTimerHandler.postDelayed(new MediaPlayTimer(mListener.getCurrentMediaId()), PLAY_WAIT_TIME - START_WAIT_TIME);
            }
        }
    }

    private class MediaPlayTimer implements Runnable {
        final String mStartMediaId;

        MediaPlayTimer(String startMediaId) {
            mStartMediaId = startMediaId;
        }

        @Override
        public void run() {
            if (mListener != null && mListener.getCurrentMediaId() != null && isSongPlaying(mListener.getPlaybackState())
                    && mStartMediaId != null && mStartMediaId.equals(mListener.getCurrentMediaId())) {
                mListener.onSongPlay(mListener.getCurrentMediaId());
            }
        }
    }

    private static boolean isStart(int state, int position) {
        return isPlaying(state) && position < START_LIMIT_TIME;
    }

    private static boolean isSongPlaying(int state) {
        return isPlaying(state);
    }

    private static boolean isPlaying(int state) {
        return state == PlaybackStateCompat.STATE_PLAYING;
    }

}
