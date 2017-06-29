package com.sjn.stamp.media.provider.multiple;

import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.controller.StampController;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.R;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class StampListProvider extends MultipleListProvider {

    public StampListProvider(Context context) {
        super(context);
    }

    @Override
    protected String getMediaKey() {
        return MediaMetadataCompat.METADATA_KEY_GENRE;
    }

    @Override
    protected String getProviderMediaId() {
        return MediaIDHelper.MEDIA_ID_MUSICS_BY_STAMP;
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
    protected ConcurrentMap<String, List<MediaMetadataCompat>> createTrackListMap(final ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        StampController stampController = new StampController(mContext);
        return stampController.getAllSongList(musicListById);
    }

    @Override
    protected MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata, String key) {
        // Since mediaMetadata fields are immutable, we need to create a copy, so we
        // can set a hierarchy-aware mediaID. We will need to know the media hierarchy
        // when we get a onPlayFromMusicID call, so we can create the proper queue based
        // on where the music was selected from (by artist, by genre, random, etc)
        MediaMetadataCompat copy = new MediaMetadataCompat.Builder(metadata)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, createHierarchyAwareMediaID(metadata, key))
                .build();
        return new MediaBrowserCompat.MediaItem(copy.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    protected String createHierarchyAwareMediaID(MediaMetadataCompat metadata, String key) {
        return MediaIDHelper.createMediaID(metadata.getDescription().getMediaId(), getProviderMediaId(), key);
    }

}
