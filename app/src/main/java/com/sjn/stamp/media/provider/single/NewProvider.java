package com.sjn.stamp.media.provider.single;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.R;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.TimeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewProvider extends AllProvider {

    public NewProvider(Context context) {
        super(context);
    }

    @Override
    protected String getProviderMediaId() {
        return MediaIDHelper.MEDIA_ID_MUSICS_BY_NEW;
    }

    @Override
    protected int getTitleId() {
        return R.string.media_item_label_new;
    }

    @Override
    protected List<MediaMetadataCompat> createTrackList(final Map<String, MediaMetadataCompat> musicListById) {
        return subList(super.createTrackList(musicListById), 14);
    }

    @Override
    protected int compareMediaList(MediaMetadataCompat lhs, MediaMetadataCompat rhs) {
        return rhs.getString(MediaMetadataCompat.METADATA_KEY_DATE).compareTo(lhs.getString(MediaMetadataCompat.METADATA_KEY_DATE));
    }

    private List<MediaMetadataCompat> subList(List<MediaMetadataCompat> list, int days) {
        for (int i = 0; i < list.size(); i++) {
            if (TimeHelper.toDateTime(list.get(i).getString(MediaMetadataCompat.METADATA_KEY_DATE)).isBefore(TimeHelper.getJapanNow().minusDays(days))) {
                return i == 0 ? new ArrayList<MediaMetadataCompat>() : list.subList(0, i);
            }
        }
        return list;
    }
}
