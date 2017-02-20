package com.sjn.taggingplayer.db.dao;

import com.sjn.taggingplayer.db.DailySongHistory;

import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class DailySongHistoryDao extends BaseDao {

    private static DailySongHistoryDao sInstance;

    public static DailySongHistoryDao getInstance() {
        if (sInstance == null) {
            sInstance = new DailySongHistoryDao();
        }
        return sInstance;
    }

    public List<DailySongHistory> findAll(Realm realm) {
        return realm.where(DailySongHistory.class).greaterThanOrEqualTo("mPlayCount", 1).findAll();
    }

    public List<DailySongHistory> findByDate(Realm realm, Date date) {
        return realm.where(DailySongHistory.class).greaterThanOrEqualTo("mDate", date).greaterThanOrEqualTo("mPlayCount", 1).findAll();
    }

    public void saveOrIncrement(Realm realm, final DailySongHistory rawDailySongHistory) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DailySongHistory dailySongHistory = realm.where(DailySongHistory.class)
                        .equalTo("mDate", rawDailySongHistory.getDate())
                        .equalTo("mSong.mMediaId", rawDailySongHistory.getSong().getMediaId())
                        .findFirst();
                if (dailySongHistory == null) {
                    rawDailySongHistory.setId(getAutoIncrementId(realm, DailySongHistory.class));
                    rawDailySongHistory.setSong(SongDao.getInstance().findOrCreate(realm, rawDailySongHistory.getSong()));
                    realm.copyToRealm(rawDailySongHistory);
                } else {
                    dailySongHistory.incrementPlayCount(rawDailySongHistory.getPlayCount());
                    dailySongHistory.incrementSkipCount(rawDailySongHistory.getSkipCount());
                }
            }
        });
    }

    public DailySongHistory newStandalone() {
        return new DailySongHistory();
    }
}