package com.sjn.stamp.ui.activity;


import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;


public interface MediaBrowsable {
    MediaBrowserCompat getMediaBrowser();

    void onMediaItemSelected(String mediaId, boolean isPlayable, boolean isBrowsable);
    void onMediaItemSelected(MediaBrowserCompat.MediaItem item);
    void onMediaItemSelected(String musicId);
    void sendCustomAction(String action, Bundle extras, MediaBrowserCompat.CustomActionCallback callback);
}
