package com.sjn.stamp.db.dao;

import com.sjn.stamp.db.Song;

import io.realm.Realm;

public class SongDao extends BaseDao {

    private static SongDao sInstance;

    public static SongDao getInstance() {
        if (sInstance == null) {
            sInstance = new SongDao();
        }
        return sInstance;
    }

    public Song findById(Realm realm, long id) {
        return realm.where(Song.class).equalTo("mId", id).findFirst();
    }

    public Song findOrCreate(Realm realm, Song rawSong) {
        Song song = realm.where(Song.class).equalTo("mTitle", rawSong.getTitle()).equalTo("mArtist", rawSong.getArtist()).findFirst();
        if (song == null) {
            rawSong.setId(getAutoIncrementId(realm, Song.class));
            song = realm.copyToRealm(rawSong);
        }
        return song;
    }

    public Song findByTitleArtist(Realm realm, String title, String artis) {
        return realm.where(Song.class).equalTo("mTitle", title).equalTo("mArtist", artis).findFirst();
    }

    public Song findByMusicId(Realm realm, String musicId) {
        return realm.where(Song.class).equalTo("mMediaId", musicId).findFirst();
    }

    public Song newStandalone() {
        return new Song();
    }
}