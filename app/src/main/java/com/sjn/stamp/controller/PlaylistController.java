package com.sjn.stamp.controller;

import android.content.ContentResolver;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.utils.LocalPlaylistHelper;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings({"unused"})
public class PlaylistController {

    private Context mContext;
    private ContentResolver mContentResolver;

    public PlaylistController(Context context) {
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
    }

    public ConcurrentMap<String, List<MediaMetadataCompat>> getAllPlaylist() {
        return LocalPlaylistHelper.findAllPlaylist(mContentResolver);
    }

    public boolean isExistAudioId(int audioId, int playlistId) {
        return LocalPlaylistHelper.isExistAudioId(mContentResolver, audioId, playlistId);
    }

    public boolean isExistPlayListName(String name) {
        return LocalPlaylistHelper.isExistPlayListName(mContentResolver, name);
    }

    public boolean addToPlaylist(int audioId, int playlistId) {
        return LocalPlaylistHelper.add(mContentResolver, audioId, playlistId);
    }

    public boolean removeFromPlaylist(int audioId, int playlistId) {
        return LocalPlaylistHelper.remove(mContentResolver, audioId, playlistId);
    }

    public String getPlaylistName(int playlistId) {
        return LocalPlaylistHelper.findPlaylistName(mContentResolver, playlistId);
    }

    public int getPlaylistId(String name) {
        return LocalPlaylistHelper.findPlaylistId(mContentResolver, name);
    }

    public boolean createPlaylist(String name) {
        return LocalPlaylistHelper.create(mContentResolver, name);
    }

    public int updatePlaylist(String srcValue, String dstValue) {
        return LocalPlaylistHelper.update(mContentResolver, srcValue, dstValue);
    }

    public int deletePlaylist(String name) {
        return LocalPlaylistHelper.delete(mContentResolver, name);
    }

    public List<MediaMetadataCompat> getMediaList(int playlistId) {
        return getMediaList(playlistId, LocalPlaylistHelper.findPlaylistName(mContentResolver, playlistId));
    }

    public List<MediaMetadataCompat> getMediaList(int playlistId, String playlistTitle) {
        return LocalPlaylistHelper.findAllPlaylistMedia(mContentResolver, playlistId, playlistTitle);
    }

}