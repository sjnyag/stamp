package com.sjn.taggingplayer.db.dao;

import com.sjn.taggingplayer.db.SongHistory;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;

public class SongHistoryDao extends BaseDao {

    private static SongHistoryDao sInstance;

    public static SongHistoryDao getInstance() {
        if (sInstance == null) {
            sInstance = new SongHistoryDao();
        }
        return sInstance;
    }

    public List<SongHistory> where(Realm realm, Date from, Date to) {
        RealmQuery query = realm.where(SongHistory.class);
        if(from != null){
            query.greaterThanOrEqualTo("mRecordedAt", from);
        }
        if(to != null){
            query.lessThanOrEqualTo("mRecordedAt", to);
        }
        return query.findAll();
    }

    public void save(Realm realm, final SongHistory songHistory) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                songHistory.setId(getAutoIncrementId(realm, SongHistory.class));
                songHistory.setDevice(DeviceDao.getInstance().findOrCreate(realm, songHistory.getDevice()));
                songHistory.setSong(SongDao.getInstance().findOrCreate(realm, songHistory.getSong()));
                realm.copyToRealm(songHistory);
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