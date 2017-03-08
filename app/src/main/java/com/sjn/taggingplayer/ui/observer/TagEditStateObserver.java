package com.sjn.taggingplayer.ui.observer;

import com.sjn.taggingplayer.utils.LogHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.experimental.Accessors;


public class TagEditStateObserver {
    private static final String TAG = LogHelper.makeLogTag(TagEditStateObserver.class);

    private static TagEditStateObserver sInstance;

    @Accessors(prefix = "m")
    @Getter
    boolean mIsTagEditMode = false;
    @Accessors(prefix = "m")
    @Getter
    List<String> mSelectedTagList = new ArrayList<>();

    private List<Listener> mListenerList = Collections.synchronizedList(new ArrayList<Listener>());


    public interface Listener {

        void onConnected();
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

    public static TagEditStateObserver getInstance() {
        if (sInstance == null) {
            sInstance = new TagEditStateObserver();
        }
        return sInstance;
    }

    private TagEditStateObserver() {
    }

}
