package com.sjn.taggingplayer.db.dao;

import com.sjn.taggingplayer.db.Song;
import com.sjn.taggingplayer.db.SongTag;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

public class SongTagDao extends BaseDao {

    private static SongTagDao sInstance;

    public static SongTagDao getInstance() {
        if (sInstance == null) {
            sInstance = new SongTagDao();
        }
        return sInstance;
    }

    public List<SongTag> findAll(Realm realm) {
        return realm.where(SongTag.class).findAll().sort("mName");
    }

    public void saveOrAdd(Realm realm, final SongTag rawSongTag, final Song rawSong) {
        if (rawSong.getMediaId() == null) {
            return;
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                SongTag managedSongTag = realm.where(SongTag.class).equalTo("mName", rawSongTag.getName()).findFirst();
                Song managedSong = SongDao.getInstance().findOrCreate(realm, rawSong);
                if (managedSongTag == null) {
                    rawSongTag.setId(getAutoIncrementId(realm, SongTag.class));
                    RealmList<Song> songList = new RealmList<>();
                    songList.add(managedSong);
                    rawSongTag.setSongList(songList);
                    addSongTag(managedSong, realm.copyToRealm(rawSongTag));
                } else {
                    if (managedSongTag.getSongList() == null) {
                        managedSongTag.setSongList(new RealmList<Song>());
                    }
                    for (Song song : managedSongTag.getSongList()) {
                        if (managedSong.getMediaId().equals(song.getMediaId())) {
                            return;
                        }
                    }
                    managedSongTag.getSongList().add(managedSong);
                    addSongTag(managedSong, managedSongTag);
                }
            }
        });
    }

    public void remove(Realm realm, final String name) {
        realm.beginTransaction();
        realm.where(SongTag.class).equalTo("mName", name).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public boolean save(Realm realm, String name) {
        boolean result = false;
        if (name == null || name.isEmpty()) {
            return false;
        }
        realm.beginTransaction();
        SongTag songTag = realm.where(SongTag.class).equalTo("mName", name).findFirst();
        if (songTag == null) {
            songTag = realm.createObject(SongTag.class);
            songTag.setId(getAutoIncrementId(realm, SongTag.class));
            songTag.setName(name);
            result = true;
        }
        realm.commitTransaction();
        return result;
    }

    public SongTag newStandalone() {
        return new SongTag();
    }

    private void addSongTag(final Song song, final SongTag songTag) {
        if (songTag == null || songTag.getName() == null) {
            return;
        }
        if (song.getSongTagList() == null) {
            song.setSongTagList(new RealmList<SongTag>());
        }
        for (SongTag registeredSongTag : song.getSongTagList()) {
            if (registeredSongTag.getName().equals(songTag.getName())) {
                return;
            }
        }
        song.getSongTagList().add(songTag);
    }

}
