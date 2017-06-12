package com.sjn.taggingplayer.ui.observer;

import com.sjn.taggingplayer.utils.LogHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaSourceObserver {
    private static final String TAG = LogHelper.makeLogTag(MediaSourceObserver.class);

    private static MediaSourceObserver sInstance;

    private List<Listener> mListenerList = Collections.synchronizedList(new ArrayList<Listener>());

    public void notifyMediaListUpdated() {
        LogHelper.i(TAG, "notifyMediaListUpdated:", mListenerList.size());
        if (mListenerList != null) {
            List<Listener> tempList = new ArrayList<>(mListenerList);
            for (Listener listener : tempList) {
                listener.onMediaListUpdated();
            }
        }
    }

    public interface Listener {
        void onMediaListUpdated();
    }

    public void addListener(Listener listener) {
        if (mListenerList.contains(listener)) {
            return;
        }
        mListenerList.add(listener);
    }

    public void removeListener(Listener listener) {
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
