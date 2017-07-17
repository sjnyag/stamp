package com.sjn.stamp.media.provider.single;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.R;
import com.sjn.stamp.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AllProvider extends SingleListProvider {

    public AllProvider(Context context) {
        super(context);
    }

    @Override
    protected String getProviderMediaId() {
        return MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL;
    }

    @Override
    protected int getTitleId() {
        return R.string.media_item_label_all_song;
    }

    @Override
    protected List<MediaMetadataCompat> createTrackList(final Map<String, MediaMetadataCompat> musicListById) {
        List<MediaMetadataCompat> list = new ArrayList<>(musicListById.values());
        Collections.sort(list, new Comparator<MediaMetadataCompat>() {
            @Override
            public int compare(MediaMetadataCompat lhs, MediaMetadataCompat rhs) {
                return compareMediaList(lhs, rhs);
            }
        });
        return list;
    }

    protected int compareMediaList(MediaMetadataCompat lhs, MediaMetadataCompat rhs) {
        return lhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE).compareTo(rhs.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
    }
}
