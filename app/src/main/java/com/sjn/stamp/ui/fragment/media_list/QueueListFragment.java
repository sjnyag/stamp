package com.sjn.stamp.ui.fragment.media_list;

import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;

import com.sjn.stamp.R;
import com.sjn.stamp.media.provider.ProviderType;
import com.sjn.stamp.ui.activity.DrawerMenu;
import com.sjn.stamp.ui.item.QueueTitleItem;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;

import java.util.List;

import static com.sjn.stamp.utils.MediaItemHelper.META_DATA_KEY_BASE_MEDIA_ID;

public class QueueListFragment extends SongListFragment {

    private static final String TAG = LogHelper.makeLogTag(QueueListFragment.class);

    public QueueListFragment() {
        setMediaId(DrawerMenu.QUEUE.getMediaId());
    }

    @Override
    public String emptyMessage(){
        return getString(R.string.empty_message_queue);
    }

    @Override
    public void onMediaBrowserChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {

        if (children != null && !children.isEmpty()) {
            String baseMediaId = children.get(0).getDescription().getExtras().getString(META_DATA_KEY_BASE_MEDIA_ID);
            ProviderType providerType = MediaIDHelper.getProviderType(baseMediaId);
            if (providerType != null && baseMediaId != null) {
                mAdapter.showLayoutInfo(
                        new QueueTitleItem(providerType, MediaIDHelper.extractBrowseCategoryValueFromMediaID(baseMediaId)), true);
            }
        }
        super.onMediaBrowserChildrenLoaded(parentId, children);
    }

}