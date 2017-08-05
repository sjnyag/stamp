package com.sjn.stamp.media.provider.single;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.media.provider.ListProvider;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.MediaItemHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract class SingleListProvider extends ListProvider {

    private static final String TAG = LogHelper.makeLogTag(SingleListProvider.class);

    protected Context mContext;

    abstract protected List<MediaMetadataCompat> createTrackList(final Map<String, MediaMetadataCompat> musicListById);

    SingleListProvider(Context context) {
        mContext = context;
    }

    @Override
    final public void reset() {
    }

    @Override
    final public List<MediaBrowserCompat.MediaItem> getListItems(String mediaId, Resources resources, ProviderState state, final Map<String, MediaMetadataCompat> musicListById) {
        List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
        if (MediaIDHelper.isTrack(mediaId)) {
            return items;
        }
        if (getProviderMediaId().equals(mediaId)) {
            List<MediaMetadataCompat> metadataList = createTrackList(musicListById);
            for (MediaMetadataCompat item : metadataList) {
                items.add(createMediaItem(item));
            }
        } else {
            LogHelper.w(TAG, "Skipping unmatched mediaId: ", mediaId);
        }
        return items;
    }

    @Override
    final public List<MediaMetadataCompat> getListByKey(String key, ProviderState state, final Map<String, MediaMetadataCompat> musicListById) {
        return createTrackList(musicListById);
    }

    @Override
    final protected MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata, String key) {
        return createMediaItem(metadata);
    }

    private MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata) {
        String hierarchyAwareMediaID = MediaIDHelper.createMediaID(
                metadata.getDescription().getMediaId(), getProviderMediaId());
        return MediaItemHelper.createPlayableItem(MediaItemHelper.updateMediaId(metadata, hierarchyAwareMediaID));
    }
}
