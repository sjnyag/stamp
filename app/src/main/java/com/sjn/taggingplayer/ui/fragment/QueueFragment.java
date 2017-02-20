package com.sjn.taggingplayer.ui.fragment;

import com.sjn.taggingplayer.ui.activity.DrawerMenu;
import com.sjn.taggingplayer.utils.LogHelper;

public class QueueFragment extends MediaBrowserFragment {

    private static final String TAG = LogHelper.makeLogTag(QueueFragment.class);

    public QueueFragment() {
        setMediaId(DrawerMenu.QUEUE.getMediaId());
    }

}