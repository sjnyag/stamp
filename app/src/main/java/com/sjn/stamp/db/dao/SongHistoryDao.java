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
        return realm.where(SongHistory.class).equalTo("mRecordType", recordType).findAllSorted("mRecordedAt", Sort.DESCENDING);
    }

    public List<SongHistory> where(Realm realm, Date from, Date to, String recordType) {
        RealmQuery<SongHistory> query = realm.where(SongHistory.class).equalTo("mRecordType", recordType);
        if (from != null) {
            query.greaterThanOrEqualTo("mRecordedAt", from);
        }
        if (to != null) {
            query.lessThanOrEqualTo("mRecordedAt", to);
        }
        return query.findAll();
    }

    public List<SongHistory> findPlayRecordByArtist(Realm realm, String artistName) {
        return realm.where(SongHistory.class).equalTo("mRecordType", RecordType.PLAY.getValue()).equalTo("mSong.mArtist.mName", artistName).findAll();
    }

    public SongHistory findOldestByArtist(Realm realm, String artistName) {
        return realm.where(SongHistory.class).equalTo("mRecordType", RecordType.PLAY.getValue()).equalTo("mSong.mArtist.mName", artistName).findAllSorted("mRecordedAt", Sort.ASCENDING).first();
    }

    public SongHistory findOldest(Realm realm, final Song song) {
        return realm.where(SongHistory.class).equalTo("mSong.mMediaId", song.getMediaId()).equalTo("mRecordType", RecordType.PLAY.getValue()).findAllSorted("mRecordedAt", Sort.ASCENDING).first();
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
                realm.where(SongHistory.class).equalTo("mId", id).findFirst().deleteFromRealm();
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