package com.sjn.taggingplayer.media.provider.multiple;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.utils.MediaIDHelper;

public class GenreListProvider extends MultipleListProvider {

    public GenreListProvider(Context context) {
        super(context);
    }

    @Override
    protected String getMediaKey() {
        return MediaMetadataCompat.METADATA_KEY_GENRE;
    }

    @Override
    protected String getProviderMediaId() {
        return MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE;
    }

    @Override
    protected int getTitleId() {
        return R.string.media_item_label_genre;
    }

    @Override
    protected int compareMediaList(MediaMetadataCompat lhs, MediaMetadataCompat rhs) {
        return lhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE).compareTo(rhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
    }

}
