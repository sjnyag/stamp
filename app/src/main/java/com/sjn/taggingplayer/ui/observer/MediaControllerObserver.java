package com.sjn.taggingplayer.ui.observer;

import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.sjn.taggingplayer.utils.LogHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaControllerObserver extends MediaControllerCompat.Callback {
    private static final String TAG = LogHelper.makeLogTag(MediaControllerObserver.class);

    private static MediaControllerObserver sInstance;

    private List<Listener> mListenerList = Collections.synchronizedList(new ArrayList<Listener>());

    public void notifyConnected() {
        if (mListenerList != null) {
            List<Listener> tempList = new ArrayList<>(mListenerList);
            for (Listener listener : tempList) {
                listener.onConnected();
            }
        }
    }

    public interface Listener {
        void onPlaybackStateChanged(@NonNull PlaybackStateCompat state);

        void onMetadataChanged(MediaMetadataCompat metadata);

        void onConnected();
    }

    public void addListener(Listener listener) {
        mListenerList.add(listener);
    }

    public void removeListener(Listener listener) {
        mListenerList.add(listener);
    }

    @Override
    public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
        if (mListenerList != null) {
            for (Listener listener : mListenerList) {
                listener.onPlaybackStateChanged(state);
            }
        }
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        if (mListenerList != null) {
            for (Listener listener : mListenerList) {
                listener.onMetadataChanged(metadata);
            }
        }
    }

    public static MediaControllerObserver getInstance() {
        if (sInstance == null) {
            sInstance = new MediaControllerObserver();
        }
        return sInstance;
    }

    private MediaControllerObserver() {
    }

}
