package com.sjn.stamp.db.dao;

import com.sjn.stamp.constant.RecordType;
import com.sjn.stamp.db.Song;
import com.sjn.stamp.db.SongHistory;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.Sort;

public class SongHistoryDao extends BaseDao {

    private static SongHistoryDao sInstance;

    public static SongHistoryDao getInstance() {
        if (sInstance == null) {
            sInstance = new SongHistoryDao();
        }
        return sInstance;
    }

    public List<SongHistory> timeline(Realm realm, String recordType) {
        return realm.where(SongHistory.class).equalTo("recordType", recordType).findAllSorted("recordedAt", Sort.DESCENDING);
    }

    public List<SongHistory> where(Realm realm, Date from, Date to, String recordType) {
        RealmQuery<SongHistory> query = realm.where(SongHistory.class).equalTo("recordType", recordType);
        if (from != null) {
            query.greaterThanOrEqualTo("recordedAt", from);
        }
        if (to != null) {
            query.lessThanOrEqualTo("recordedAt", to);
        }
        return query.findAll();
    }

    public List<SongHistory> findPlayRecordByArtist(Realm realm, String artistName) {
        return realm.where(SongHistory.class).equalTo("recordType", RecordType.PLAY.getValue()).equalTo("song.artist.name", artistName).findAll();
    }

    public SongHistory findOldestByArtist(Realm realm, String artistName) {
        return realm.where(SongHistory.class).equalTo("recordType", RecordType.PLAY.getValue()).equalTo("song.artist.name", artistName).findAllSorted("recordedAt", Sort.ASCENDING).first();
    }

    public SongHistory findOldest(Realm realm, final Song song) {
        return realm.where(SongHistory.class).equalTo("song.mediaId", song.getMediaId()).equalTo("recordType", RecordType.PLAY.getValue()).findAllSorted("recordedAt", Sort.ASCENDING).first();
    }

    public void save(Realm realm, final SongHistory songHistory) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                songHistory.setId(getAutoIncrementId(realm, SongHistory.class));
                songHistory.setDevice(DeviceDao.getInstance().findOrCreate(realm, songHistory.getDevice()));
                songHistory.setSong(SongDao.getInstance().findOrCreate(realm, songHistory.getSong()));
                realm.insert(songHistory);
            }
        });
    }

    public void remove(Realm realm, final long id) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(SongHistory.class).equalTo("id", id).findFirst().deleteFromRealm();
            }
        });
    }

    public List<SongHistory> findAll(Realm realm) {
        return realm.where(SongHistory.class).findAll();
    }

    public SongHistory newStandalone() {
        return new SongHistory();
    }
}