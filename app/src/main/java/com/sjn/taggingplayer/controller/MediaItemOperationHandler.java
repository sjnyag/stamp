package com.sjn.taggingplayer.controller;

import com.sjn.taggingplayer.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class MediaItemOperationHandler {

    private static final String TAG = LogHelper.makeLogTag(MediaItemOperationHandler.class);
    private static MediaItemOperationHandler sInstance;

    @Accessors(prefix = "m")
    @Getter
    @Setter
    private TagEditModeInterface mTagEditModeInterface;

    public interface TagEditModeInterface {
        List<String> getSelectedTagList();

        boolean isTagEditMode();
    }

    public enum Mode {
        PLAY,
        TAG
    }

    public List<String> getSelectedTagList() {
        if (mTagEditModeInterface == null) {
            return new ArrayList<>();
        }
        return mTagEditModeInterface.getSelectedTagList();
    }

    public boolean isTagEditMode() {
        if (mTagEditModeInterface == null) {
            return false;
        }
        return mTagEditModeInterface.isTagEditMode();
    }


    private MediaItemOperationHandler() {

    }

    public static MediaItemOperationHandler getInstance() {
        if (sInstance == null) {
            sInstance = new MediaItemOperationHandler();
        }
        return sInstance;
    }


}
