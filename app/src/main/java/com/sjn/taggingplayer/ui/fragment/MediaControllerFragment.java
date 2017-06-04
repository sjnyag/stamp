package com.sjn.taggingplayer.ui.fragment;

import android.support.v4.media.MediaBrowserCompat;

import com.sjn.taggingplayer.ui.MediaBrowserProvider;

import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

abstract public class MediaControllerFragment extends AbstractFragment {

    abstract public void onConnected();

    abstract public List<AbstractFlexibleItem> getCurrentMediaItems();

    abstract public int getMenuResourceId();

    abstract public String getMediaId();

    public interface MediaFragmentListener extends MediaBrowserProvider {
        void onMediaItemSelected(MediaBrowserCompat.MediaItem item);

        void setToolbarTitle(CharSequence title);
    }
}