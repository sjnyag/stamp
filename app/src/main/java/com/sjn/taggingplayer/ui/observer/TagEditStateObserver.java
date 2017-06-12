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
    List<String> mSelectedTagList = new ArrayList<>();

    private List<Listener> mListenerList = Collections.synchronizedList(new ArrayList<Listener>());

    public void notifyAllTagChange(String tag) {
        LogHelper.i(TAG, "notifyAllTagChange", mListenerList.size());
        if (mListenerList != null) {
            List<Listener> tempList = new ArrayList<>(mListenerList);
            for (Listener listener : tempList) {
                listener.onNetTagCreated(tag);
            }
        }
    }

    public void notifySelectedTagListChange(List<String> tagList) {
        LogHelper.i(TAG, "notifySelectedTagListChange", mListenerList.size());
        mSelectedTagList = tagList;
        if (mListenerList != null) {
            List<Listener> tempList = new ArrayList<>(mListenerList);
            for (Listener listener : tempList) {
                listener.onSelectedTagChange(mSelectedTagList);
            }
        }
    }


    public interface Listener {
        void onSelectedTagChange(List<String> selectedTagList);

        void onNetTagCreated(String tag);
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
