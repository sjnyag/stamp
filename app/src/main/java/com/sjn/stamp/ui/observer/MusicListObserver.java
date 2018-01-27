package com.sjn.stamp.ui.observer;

import com.sjn.stamp.utils.LogHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicListObserver {
    private static final String TAG = LogHelper.INSTANCE.makeLogTag(MusicListObserver.class);

    private static MusicListObserver sInstance;

    private List<Listener> mListenerList = Collections.synchronizedList(new ArrayList<Listener>());

    public void notifyMediaListUpdated() {
        LogHelper.INSTANCE.i(TAG, "notifyMediaListUpdated:", mListenerList.size());
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

    public static MusicListObserver getInstance() {
        if (sInstance == null) {
            sInstance = new MusicListObserver();
        }
        return sInstance;
    }

    private MusicListObserver() {
    }

}
