package com.sjn.stamp.media.provider.multiple;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.R;

public class AlbumListProvider extends MultipleListProvider {

    public AlbumListProvider(Context context) {
        super(context);
    }

    @Override
    protected String getMediaKey() {
        return MediaMetadataCompat.METADATA_KEY_ALBUM;
    }

    @Override
    protected String getProviderMediaId() {
        return MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM;
    }

    @Override
    protected int getTitleId() {
        return R.string.media_item_label_album;
    }

    @Override
    protected int compareMediaList(MediaMetadataCompat lhs, MediaMetadataCompat rhs) {
        return lhs.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER) < (rhs.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER)) ? 0 : 1;
    }
}
