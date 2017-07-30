package com.sjn.stamp.media.provider;

import android.content.res.Resources;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;


public abstract class ListProvider {

    //noinspection ResourceType
    public static String CUSTOM_METADATA_TRACK_PREFIX = "__PREFIX__";

    public enum ProviderState {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    final public MediaBrowserCompat.MediaItem getRootMenu(Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(getProviderMediaId())
                .setTitle(resources.getString(getTitleId()))
                /*

                .setSubtitle(resources.getString(R.string.browse_genre_subtitle))
                .setIconUri(Uri.parse("android.resource://" +
                        "com.sjn.stamp/drawable/ic_by_genre"))
                 */
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    final public boolean matchFilter(String filter, MediaMetadataCompat track) {
        if (track == null || track.getDescription() == null || track.getDescription().getTitle() == null) {
            return false;
        }
        return matchFilter(filter, track.getDescription().getTitle().toString());
    }

    final public boolean matchFilter(String filter, String target) {
        return filter == null || filter.isEmpty() || (target.toUpperCase().contains(filter.toUpperCase()));
    }

    abstract public void reset();

    abstract public List<MediaBrowserCompat.MediaItem> getListItems(String mediaId, Resources resources, ProviderState state,
                                                                    final Map<String, MediaMetadataCompat> musicListById);

    abstract public List<MediaMetadataCompat> getListByKey(String key, ProviderState state, final Map<String, MediaMetadataCompat> musicListById);

    abstract protected MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata, String key);

    abstract protected String getProviderMediaId();

    abstract protected int getTitleId();
}
