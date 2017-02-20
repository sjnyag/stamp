package com.sjn.taggingplayer.media.provider.multiple;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.controller.PlaylistController;
import com.sjn.taggingplayer.utils.MediaIDHelper;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class PlaylistProvider extends MultipleListProvider {

    public PlaylistProvider(Context context) {
        super(context);
    }

    @Override
    protected String getMediaKey() {
        return MediaMetadataCompat.METADATA_KEY_GENRE;
    }

    @Override
    protected String getProviderMediaId() {
        return MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST;
    }

    @Override
    protected int getTitleId() {
        return R.string.media_item_label_playlist;
    }

    @Override
    protected ConcurrentMap<String, List<MediaMetadataCompat>> createTrackListMap(final ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        PlaylistController playlistController = new PlaylistController(mContext);
        return playlistController.getAllPlaylist();
    }

    @Override
    protected int compareMediaList(MediaMetadataCompat lhs, MediaMetadataCompat rhs) {
        return 1;
    }

}
