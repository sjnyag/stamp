package com.sjn.stamp.media.provider.multiple;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.google.common.collect.Lists;
import com.sjn.stamp.media.provider.ListProvider;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

abstract public class MultipleListProvider extends ListProvider {

    private static final String TAG = LogHelper.makeLogTag(MultipleListProvider.class);

    protected Context mContext;
    protected ConcurrentMap<String, List<MediaMetadataCompat>> mTrackListMap = new ConcurrentHashMap<>();
    protected List<MediaBrowserCompat.MediaItem> mLastTrackList = new ArrayList<>();
    protected String mLastMediaKey = "";
    protected String mLastMediaKeyFilter = "";
    protected String mLastTrackFilter = "";
    protected int mLastMediaKeySeek = 0;
    protected int mLastTrackSeek = 0;

    protected List<MediaBrowserCompat.MediaItem> mLastMediaKeyList = new ArrayList<>();

    abstract protected String getMediaKey();

    abstract protected int compareMediaList(MediaMetadataCompat lhs, MediaMetadataCompat rhs);

    public MultipleListProvider(Context context) {
        mContext = context;
    }

    @Override
    final public void reset() {
        mTrackListMap.clear();
        mLastMediaKeyList.clear();
        mLastMediaKeySeek = 0;
        mLastMediaKey = "";
        mLastMediaKeyFilter = "";
        mLastTrackList.clear();
        mLastTrackSeek = 0;
        mLastTrackFilter = "";
    }

    protected ConcurrentMap<String, List<MediaMetadataCompat>> createTrackListMap(final ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        ConcurrentMap<String, List<MediaMetadataCompat>> trackListMap = new ConcurrentHashMap<>();

        for (MediaMetadataCompat m : musicListById.values()) {
            //noinspection ResourceType
            String key = MediaIDHelper.escape(m.getString(getMediaKey()));
            if (key != null && !key.isEmpty()) {
                List<MediaMetadataCompat> list = trackListMap.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                    trackListMap.put(key, list);
                }
                list.add(m);
            }
        }
        return trackListMap;
    }

    @Override
    final public List<MediaBrowserCompat.MediaItem> getListItems(String mediaId, Resources resources, ProviderState state, final ConcurrentMap<String, MediaMetadataCompat> musicListById, String filter, int size, Comparator comparator) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        if (MediaIDHelper.isTrack(mediaId)) {
            return mediaItems;
        }
        if (filter == null) {
            filter = "";
        }
        if (getProviderMediaId().equals(mediaId)) {
            List<String> keyList = getKeys(state, musicListById);
            if (mLastMediaKeyFilter == null || !mLastMediaKeyFilter.equals(filter)) {
                mLastMediaKeyFilter = filter;
                mLastMediaKeyList.clear();
                mLastMediaKeySeek = 0;
            }
            int startSize = mLastMediaKeyList.size();
            for (; mLastMediaKeyList.size() - startSize < size && mLastMediaKeySeek < keyList.size(); mLastMediaKeySeek++) {
                String key = keyList.get(mLastMediaKeySeek);
                if (matchFilter(filter, key)) {
                    mLastMediaKeyList.add(createBrowsableMediaItemForKey(key, findIconUri(key, state, musicListById)));
                }
            }
            LogHelper.d(TAG, "getListItems", ", mediaId: ", mediaId, ", filter: ", filter, ", comparator: ", comparator, ", mLastMediaKeyList.size: ", mLastMediaKeyList.size());
            return mLastMediaKeyList;
        } else if (mediaId.startsWith(getProviderMediaId())) {
            String key = MediaIDHelper.getHierarchy(mediaId)[1];
            if (mLastMediaKey == null || !mLastMediaKey.equals(key) || mLastMediaKeyFilter == null || !mLastMediaKeyFilter.equals(filter)) {
                mLastMediaKey = key;
                mLastMediaKeyFilter = filter;
                mLastTrackList.clear();
                mLastTrackSeek = 0;
            }
            List<MediaMetadataCompat> metadataList = getListByKey(mLastMediaKey, state, musicListById);
            int startSize = mLastTrackList.size();
            for (; mLastTrackList.size() - startSize < size && mLastTrackSeek < metadataList.size(); mLastTrackSeek++) {
                MediaMetadataCompat track = metadataList.get(mLastTrackSeek);
                if (matchFilter(filter, track)) {
                    mLastTrackList.add(createMediaItem(track, mLastMediaKey));
                }
            }
            return mLastTrackList;
        } else {
            LogHelper.w(TAG, "Skipping unmatched mediaId: ", mediaId);
        }
        return mediaItems;
    }

    /**
     * Get music tracks of the given key
     */
    @Override
    final public List<MediaMetadataCompat> getListByKey(String key, ProviderState state, final ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        if (state != ProviderState.INITIALIZED || !getTrackListMap(musicListById).containsKey(key)) {
            return Collections.emptyList();
        }
        List<MediaMetadataCompat> list = getTrackListMap(musicListById).get(key);
        Collections.sort(list, new Comparator<MediaMetadataCompat>() {
            @Override
            public int compare(MediaMetadataCompat lhs, MediaMetadataCompat rhs) {
                return compareMediaList(lhs, rhs);
            }
        });
        return list;
    }

    @Override
    protected MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata, String key) {
        // Since mediaMetadata fields are immutable, we need to create a copy, so we
        // can set a hierarchy-aware mediaID. We will need to know the media hierarchy
        // when we get a onPlayFromMusicID call, so we can create the proper queue based
        // on where the music was selected from (by artist, by genre, random, etc)
        MediaMetadataCompat copy = new MediaMetadataCompat.Builder(metadata)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, createHierarchyAwareMediaID(metadata))
                .build();
        return new MediaBrowserCompat.MediaItem(copy.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    protected Uri findIconUri(String key, ProviderState state, final ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        List<MediaMetadataCompat> metadataList = getListByKey(key, state, musicListById);

        for (final MediaMetadataCompat metadata : metadataList) {
            if (metadata.getDescription().getIconUri() == null) {
                continue;
            }
            return metadata.getDescription().getIconUri();
        }
        return null;
    }

    final protected List<String> getKeys(ProviderState state, ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        if (state != ProviderState.INITIALIZED) {
            return Collections.emptyList();
        }
        ConcurrentMap<String, List<MediaMetadataCompat>> trackListMap = getTrackListMap(musicListById);
        if (trackListMap == null || trackListMap.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> list = Lists.newArrayList(trackListMap.keySet());
        Collections.sort(list);
        return list;
    }

    final protected String createHierarchyAwareMediaID(MediaMetadataCompat metadata) {
        //noinspection ResourceType
        String category = metadata.getString(getMediaKey());
        return MediaIDHelper.createMediaID(
                metadata.getDescription().getMediaId(), getProviderMediaId(), category);
    }

    final protected MediaBrowserCompat.MediaItem createBrowsableMediaItemForKey(String key, Uri iconUrl) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MediaIDHelper.createMediaID(null, getProviderMediaId(), key))
                .setTitle(MediaIDHelper.unescape(key))
                .setIconUri(iconUrl)
                /*
                .setSubtitle(resources.getString(
                        R.string.browse_musics_by_genre_subtitle, genre))
                */
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    final protected ConcurrentMap<String, List<MediaMetadataCompat>> getTrackListMap(final ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        if (mTrackListMap == null || mTrackListMap.isEmpty()) {
            mTrackListMap = createTrackListMap(musicListById);
        }
        return mTrackListMap;
    }
}
