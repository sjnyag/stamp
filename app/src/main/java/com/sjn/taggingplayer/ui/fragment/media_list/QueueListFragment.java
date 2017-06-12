package com.sjn.taggingplayer.ui.fragment.media_list;

import com.sjn.taggingplayer.ui.activity.DrawerMenu;
import com.sjn.taggingplayer.utils.LogHelper;

public class QueueListFragment extends SongListFragment {

    private static final String TAG = LogHelper.makeLogTag(QueueListFragment.class);

    public QueueListFragment() {
        setMediaId(DrawerMenu.QUEUE.getMediaId());
    }

}