package com.sjn.stamp.ui.fragment.media_list;

import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;

import com.sjn.stamp.media.QueueManager;
import com.sjn.stamp.media.provider.ProviderType;
import com.sjn.stamp.ui.activity.DrawerMenu;
import com.sjn.stamp.ui.item.QueueTitleItem;
import com.sjn.stamp.ui.item.SongItem;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;

import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public class QueueListFragment extends SongListFragment {

    private static final String TAG = LogHelper.makeLogTag(QueueListFragment.class);

    public QueueListFragment() {
        setMediaId(DrawerMenu.QUEUE.getMediaId());
    }

    @Override
    public void onMediaBrowserChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
        try {
            LogHelper.d(TAG, "fragment onChildrenLoaded, parentId=" + parentId +
                    "  count=" + children.size());
            mItemList.clear();
            mAdapter.clear();
            String baseMediaId = "";
            for (MediaBrowserCompat.MediaItem item : children) {
                AbstractFlexibleItem songItem = new SongItem(item);
                mItemList.add(songItem);
                mAdapter.addItem(songItem);
                if (item.getDescription().getExtras() != null) {
                    baseMediaId = item.getDescription().getExtras().getString(QueueManager.META_DATA_KEY_BASE_MEDIA_ID);
                }
            }
            mAdapter.notifyDataSetChanged();
            ProviderType providerType = MediaIDHelper.getProviderType(baseMediaId);
            if (providerType != null && baseMediaId != null) {
                mAdapter.showLayoutInfo(
                        new QueueTitleItem(providerType, MediaIDHelper.extractBrowseCategoryValueFromMediaID(baseMediaId)), true);
            }
        } catch (Throwable t) {
            LogHelper.e(TAG, "Error on childrenloaded", t);
        }
    }

}