package com.sjn.stamp.media.provider.multiple;

import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.R;
import com.sjn.stamp.controller.StampController;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.MediaItemHelper;

import java.util.List;
import java.util.Map;

public class MyStampListProvider extends MultipleListProvider {

    public MyStampListProvider(Context context) {
        super(context);
    }

    @Override
    protected String getMediaKey() {
        return MediaMetadataCompat.METADATA_KEY_GENRE;
    }

    @Override
    protected String getProviderMediaId() {
        return MediaIDHelper.MEDIA_ID_MUSICS_BY_MY_STAMP;
    }

    @Override
    protected int getTitleId() {
        return R.string.media_item_label_stamp;
    }

    @Override
    protected int compareMediaList(MediaMetadataCompat lhs, MediaMetadataCompat rhs) {
        return lhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE).compareTo(rhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
    }

    @Override
    protected Map<String, List<MediaMetadataCompat>> createTrackListMap(final Map<String, MediaMetadataCompat> musicListById) {
        StampController stampController = new StampController(mContext);
        return stampController.createStampMap(musicListById, false);
    }

    @Override
    protected MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata, String key) {
        return MediaItemHelper.createPlayableItem(MediaItemHelper.updateMediaId(metadata, createHierarchyAwareMediaID(metadata, key)));
    }

    @Override
    protected Map<String, List<MediaMetadataCompat>> getTrackListMap(final Map<String, MediaMetadataCompat> musicListById) {
        return createTrackListMap(musicListById);
    }

    private String createHierarchyAwareMediaID(MediaMetadataCompat metadata, String key) {
        return MediaIDHelper.createMediaID(metadata.getDescription().getMediaId(), getProviderMediaId(), key);
    }

}
