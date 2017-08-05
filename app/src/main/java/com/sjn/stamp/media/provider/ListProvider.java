package com.sjn.stamp.media.provider;

import android.content.res.Resources;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.utils.MediaItemHelper;

import java.util.List;
import java.util.Locale;
import java.util.Map;


public abstract class ListProvider {

    public enum ProviderState {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    final public MediaBrowserCompat.MediaItem getRootMenu(Resources resources) {
        return MediaItemHelper.createBrowsableItem(getProviderMediaId(), resources.getString(getTitleId()));
    }

    final public boolean matchFilter(String filter, MediaMetadataCompat track) {
        if (track == null || track.getDescription() == null || track.getDescription().getTitle() == null) {
            return false;
        }
        return matchFilter(filter, track.getDescription().getTitle().toString());
    }

    final public boolean matchFilter(String filter, String target) {
        return filter == null || filter.isEmpty() || (target.toUpperCase(Locale.getDefault()).contains(filter.toUpperCase(Locale.getDefault())));
    }

    abstract public void reset();

    abstract public List<MediaBrowserCompat.MediaItem> getListItems(String mediaId, Resources resources, ProviderState state,
                                                                    final Map<String, MediaMetadataCompat> musicListById);

    abstract public List<MediaMetadataCompat> getListByKey(String key, ProviderState state, final Map<String, MediaMetadataCompat> musicListById);

    abstract protected MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata, String key);

    abstract protected String getProviderMediaId();

    abstract protected int getTitleId();
}
