package com.sjn.stamp.media.provider.single;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.media.provider.ListProvider;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

abstract public class SingleListProvider extends ListProvider {

    private static final String TAG = LogHelper.makeLogTag(SingleListProvider.class);

    protected Context mContext;
    protected List<MediaBrowserCompat.MediaItem> mLastTrackList = new ArrayList<>();
    protected String mLastTrackFilter = "";
    protected int mLastTrackSeek = 0;

    abstract protected List<MediaMetadataCompat> createTrackList(final ConcurrentMap<String, MediaMetadataCompat> musicListById);

    public SingleListProvider(Context context) {
        mContext = context;
    }

    @Override
    final public void reset() {
        mLastTrackList.clear();
        mLastTrackFilter = "";
        mLastTrackSeek = 0;
    }

    @Override
    final public List<MediaBrowserCompat.MediaItem> getListItems(String mediaId, Resources resources, ProviderState state, final ConcurrentMap<String, MediaMetadataCompat> musicListById, String filter, Integer size, Comparator comparator) {
        if (MediaIDHelper.isTrack(mediaId)) {
            return new ArrayList<>();
        }
        if (filter == null) {
            filter = "";
        }
        if (getProviderMediaId().equals(mediaId)) {
            List<MediaMetadataCompat> metadataList = createTrackList(musicListById);
            if (mLastTrackFilter.equals("") || !mLastTrackFilter.equals(filter)) {
                mLastTrackFilter = filter;
                mLastTrackList.clear();
                mLastTrackSeek = 0;
            }
            int startSize = mLastTrackList.size();
            for (; mLastTrackSeek < metadataList.size(); mLastTrackSeek++) {
                if (size != null && mLastTrackList.size() - startSize < size) {
                    break;
                }
                MediaMetadataCompat track = metadataList.get(mLastTrackSeek);
                if (matchFilter(filter, track)) {
                    mLastTrackList.add(createMediaItem(track));
                }
            }
        } else {
            LogHelper.w(TAG, "Skipping unmatched mediaId: ", mediaId);
        }
        return mLastTrackList;
    }

    @Override
    final public List<MediaMetadataCompat> getListByKey(String key, ProviderState state, final ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        return createTrackList(musicListById);
    }

    @Override
    final protected MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata, String key) {
        return createMediaItem(metadata);
    }

    final protected MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata) {
        // Since mediaMetadata fields are immutable, we need to create a copy, so we
        // can set a hierarchy-aware mediaID. We will need to know the media hierarchy
        // when we get a onPlayFromMusicID call, so we can create the proper queue based
        // on where the music was selected from (by artist, by genre, random, etc)
        String hierarchyAwareMediaID = MediaIDHelper.createMediaID(
                metadata.getDescription().getMediaId(), getProviderMediaId());
        MediaMetadataCompat copy = new MediaMetadataCompat.Builder(metadata)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
                .build();
        return new MediaBrowserCompat.MediaItem(createCustomDescription(copy),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    private MediaDescriptionCompat createCustomDescription(MediaMetadataCompat metadata) {
        return new MediaDescriptionCompat.Builder()
                .setMediaId(metadata.getDescription().getMediaId())
                .setTitle(metadata.getDescription().getTitle())
                .setSubtitle(metadata.getDescription().getSubtitle())
                .setDescription(metadata.getDescription().getDescription())
                .setIconBitmap(metadata.getDescription().getIconBitmap())
                .setIconUri(metadata.getDescription().getIconUri())
                .setExtras(metadata.getBundle()).build();
    }
}
