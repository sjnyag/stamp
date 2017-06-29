package com.sjn.stamp.ui.fragment.media_list;

import com.sjn.stamp.ui.activity.DrawerMenu;
import com.sjn.stamp.utils.LogHelper;

public class QueueListFragment extends SongListFragment {

    private static final String TAG = LogHelper.makeLogTag(QueueListFragment.class);

    public QueueListFragment() {
        setMediaId(DrawerMenu.QUEUE.getMediaId());
    }

}