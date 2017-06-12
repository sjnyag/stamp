package com.sjn.taggingplayer.media.source;

import android.support.v4.media.MediaMetadataCompat;

import com.sjn.taggingplayer.utils.LogHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MediaSourceObserver {
    private static final String TAG = LogHelper.makeLogTag(MediaSourceObserver.class);

    private static MediaSourceObserver sInstance;

    private List<MediaSourceObserver.Listener> mListenerList = Collections.synchronizedList(new ArrayList<MediaSourceObserver.Listener>());

    public void notifyMediaSourceChange(Iterator<MediaMetadataCompat> iterator) {
        if (mListenerList != null) {
            List<MediaSourceObserver.Listener> tempList = new ArrayList<>(mListenerList);
            for (MediaSourceObserver.Listener listener : tempList) {
                listener.onSourceChange(iterator);
            }
        }

    }

    public interface Listener {
        void onSourceChange(final Iterator<MediaMetadataCompat> trackIterator);
    }

    public void addListener(MediaSourceObserver.Listener listener) {
        if (mListenerList.contains(listener)) {
            return;
        }
        mListenerList.add(listener);
    }

    public void removeListener(MediaSourceObserver.Listener listener) {
        if (!mListenerList.contains(listener)) {
            return;
        }
        mListenerList.remove(listener);
    }

    public static MediaSourceObserver getInstance() {
        if (sInstance == null) {
            sInstance = new MediaSourceObserver();
        }
        return sInstance;
    }

    private MediaSourceObserver() {
    }

}
