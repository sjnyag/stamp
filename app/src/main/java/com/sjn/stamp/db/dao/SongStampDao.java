package com.sjn.stamp.db.dao;

import com.sjn.stamp.db.Song;
import com.sjn.stamp.db.SongStamp;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

public class SongStampDao extends BaseDao {

    private static SongStampDao sInstance;

    public static SongStampDao getInstance() {
        if (sInstance == null) {
            sInstance = new SongStampDao();
        }
        return sInstance;
    }

    public List<SongStamp> findAll(Realm realm) {
        return realm.where(SongStamp.class).findAll().sort("name");
    }

    public void saveOrAdd(Realm realm, final SongStamp rawSongStamp, final Song rawSong) {
        if (rawSong.getMediaId() == null) {
            return;
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                SongStamp managedSongStamp = realm.where(SongStamp.class).equalTo("name", rawSongStamp.getName()).findFirst();
                Song managedSong = SongDao.getInstance().findOrCreate(realm, rawSong);
                if (managedSongStamp == null) {
                    rawSongStamp.setId(getAutoIncrementId(realm, SongStamp.class));
                    RealmList<Song> songList = new RealmList<>();
                    songList.add(managedSong);
                    rawSongStamp.setSongList(songList);
                    addSongStamp(managedSong, realm.copyToRealm(rawSongStamp));
                } else {
                    if (managedSongStamp.getSongList() == null) {
                        managedSongStamp.setSongList(new RealmList<Song>());
                    }
                    for (Song song : managedSongStamp.getSongList()) {
                        if (managedSong.getMediaId().equals(song.getMediaId())) {
                            return;
                        }
                    }
                    managedSongStamp.getSongList().add(managedSong);
                    addSongStamp(managedSong, managedSongStamp);
                }
            }
        });
    }

    public void remove(Realm realm, final String name) {
        realm.beginTransaction();
        realm.where(SongStamp.class).equalTo("name", name).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public boolean save(Realm realm, String name) {
        boolean result = false;
        if (name == null || name.isEmpty()) {
            return false;
        }
        realm.beginTransaction();
        SongStamp songStamp = realm.where(SongStamp.class).equalTo("name", name).findFirst();
        if (songStamp == null) {
            songStamp = realm.createObject(SongStamp.class, getAutoIncrementId(realm, SongStamp.class));
            songStamp.setName(name);
            songStamp.setSystem(false);
            result = true;
        }
        realm.commitTransaction();
        return result;
    }

    public SongStamp newStandalone() {
        return new SongStamp();
    }

    private void addSongStamp(final Song song, final SongStamp songStamp) {
        if (songStamp == null || songStamp.getName() == null) {
            return;
        }
        if (song.getSongStampList() == null) {
            song.setSongStampList(new RealmList<SongStamp>());
        }
        for (SongStamp registeredSongStamp : song.getSongStampList()) {
            if (registeredSongStamp.getName().equals(songStamp.getName())) {
                return;
            }
        }
        song.getSongStampList().add(songStamp);
    }

}
