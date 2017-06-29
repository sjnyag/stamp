package com.sjn.stamp.media.provider.multiple;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.R;

public class ArtistListProvider extends MultipleListProvider {

    public ArtistListProvider(Context context) {
        super(context);
    }

    @Override
    protected String getMediaKey() {
        return MediaMetadataCompat.METADATA_KEY_ARTIST;
    }

    @Override
    protected String getProviderMediaId() {
        return MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST;
    }

    @Override
    protected int getTitleId() {
        return R.string.media_item_label_artist;
    }

    @Override
    protected int compareMediaList(MediaMetadataCompat lhs, MediaMetadataCompat rhs) {
        return lhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE).compareTo(rhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
    }

}
