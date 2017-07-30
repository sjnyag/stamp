package com.sjn.stamp.media.provider.multiple;

import android.content.Context;
import android.content.res.Resources;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class MultipleListProvider extends ListProvider {

    private static final String TAG = LogHelper.makeLogTag(MultipleListProvider.class);

    protected Context mContext;
    private Map<String, List<MediaMetadataCompat>> mTrackListMap = new ConcurrentHashMap<>();

    abstract protected String getMediaKey();

    abstract protected int compareMediaList(MediaMetadataCompat lhs, MediaMetadataCompat rhs);

    MultipleListProvider(Context context) {
        mContext = context;
    }

    @Override
    final public void reset() {
        mTrackListMap.clear();
    }

    protected Map<String, List<MediaMetadataCompat>> createTrackListMap(final Map<String, MediaMetadataCompat> musicListById) {
        Map<String, List<MediaMetadataCompat>> trackListMap = new HashMap<>();

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
    final public List<MediaBrowserCompat.MediaItem> getListItems(String mediaId, Resources resources, ProviderState state, final Map<String, MediaMetadataCompat> musicListById) {
        List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
        if (MediaIDHelper.isTrack(mediaId)) {
            return items;
        }
        if (getProviderMediaId().equals(mediaId)) {
            for (String key : getKeys(state, musicListById)) {
                items.add(createBrowsableMediaItemForKey(key));
            }
        } else if (mediaId.startsWith(getProviderMediaId())) {
            String key = MediaIDHelper.getHierarchy(mediaId)[1];
            for (MediaMetadataCompat item : getListByKey(key, state, musicListById)) {
                items.add(createMediaItem(item, key));
            }
        } else {
            LogHelper.w(TAG, "Skipping unmatched mediaId: ", mediaId);
        }
        return items;
    }

    /**
     * Get music tracks of the given key
     */
    @Override
    final public List<MediaMetadataCompat> getListByKey(String key, ProviderState state, final Map<String, MediaMetadataCompat> musicListById) {
        if (state != ProviderState.INITIALIZED || !getTrackListMap(musicListById).containsKey(key)) {
            return Collections.emptyList();
        }
        List<MediaMetadataCompat> list = new ArrayList<>(getTrackListMap(musicListById).get(key));
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

    private List<String> getKeys(ProviderState state, Map<String, MediaMetadataCompat> musicListById) {
        if (state != ProviderState.INITIALIZED) {
            return Collections.emptyList();
        }
        Map<String, List<MediaMetadataCompat>> trackListMap = getTrackListMap(musicListById);
        if (trackListMap == null || trackListMap.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> list = Lists.newArrayList(trackListMap.keySet());
        Collections.sort(list);
        return list;
    }

    private String createHierarchyAwareMediaID(MediaMetadataCompat metadata) {
        //noinspection ResourceType
        String category = metadata.getString(getMediaKey());
        return MediaIDHelper.createMediaID(
                metadata.getDescription().getMediaId(), getProviderMediaId(), category);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForKey(String key) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MediaIDHelper.createMediaID(null, getProviderMediaId(), key))
                .setTitle(MediaIDHelper.unescape(key))
                /*
                .setSubtitle(resources.getString(
                        R.string.browse_musics_by_genre_subtitle, genre))
                */
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private Map<String, List<MediaMetadataCompat>> getTrackListMap(final Map<String, MediaMetadataCompat> musicListById) {
        if (mTrackListMap == null || mTrackListMap.isEmpty()) {
            mTrackListMap = createTrackListMap(musicListById);
        }
        return mTrackListMap;
    }
}
