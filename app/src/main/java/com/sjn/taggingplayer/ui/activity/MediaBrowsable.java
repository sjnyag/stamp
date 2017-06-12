package com.sjn.taggingplayer.ui.activity;


import android.support.v4.media.MediaBrowserCompat;


public interface MediaBrowsable {
    MediaBrowserCompat getMediaBrowser();

    void onMediaItemSelected(MediaBrowserCompat.MediaItem item);
    void onMediaItemSelected(String musicId);
}
