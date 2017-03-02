package com.sjn.taggingplayer.media.provider.single;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.controller.SongHistoryController;
import com.sjn.taggingplayer.db.TotalSongHistory;
import com.sjn.taggingplayer.media.provider.ListProvider;
import com.sjn.taggingplayer.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class TopSongProvider extends SingleListProvider {

    public TopSongProvider(Context context) {
        super(context);
    }

    @Override
    protected String getProviderMediaId() {
        return MediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_SONG;
    }

    @Override
    protected int getTitleId() {
        return R.string.media_item_label_top_song;
    }

    @Override
    protected List<MediaMetadataCompat> createTrackList(final ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        SongHistoryController songHistoryController = new SongHistoryController(mContext);
        return songHistoryController.getTopSongList();
    }

}
