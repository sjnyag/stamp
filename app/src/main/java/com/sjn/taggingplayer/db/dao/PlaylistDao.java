package com.sjn.taggingplayer.db.dao;

import com.sjn.taggingplayer.db.Playlist;
import com.sjn.taggingplayer.db.PlaylistSong;

import io.realm.Realm;

public class PlaylistDao extends BaseDao {

    private static PlaylistDao sInstance;

    public static PlaylistDao getInstance() {
        if (sInstance == null) {
            sInstance = new PlaylistDao();
        }
        return sInstance;
    }

    public Playlist save(Realm realm, Playlist rawPlaylist) {
        realm.beginTransaction();
        rawPlaylist.setId(getAutoIncrementId(realm, Playlist.class));
        int id = getAutoIncrementId(realm, PlaylistSong.class);
        for (PlaylistSong playlistSong : rawPlaylist.getSongs()) {
            playlistSong.setId(id++);
        }
        realm.commitTransaction();
        return rawPlaylist;
    }
}