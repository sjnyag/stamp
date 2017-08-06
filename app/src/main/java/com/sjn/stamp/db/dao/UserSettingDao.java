package com.sjn.stamp.db.dao;

import android.support.annotation.NonNull;

import com.sjn.stamp.constant.RepeatState;
import com.sjn.stamp.constant.ShuffleState;
import com.sjn.stamp.db.UserSetting;

import io.realm.Realm;

public class UserSettingDao extends BaseDao {

    private static UserSettingDao sInstance;

    public static UserSettingDao getInstance() {
        if (sInstance == null) {
            sInstance = new UserSettingDao();
        }
        return sInstance;
    }

    @NonNull
    public UserSetting getUserSetting(Realm realm) {
        return findOrCreate(realm);
    }

    public void updateAutoLogin(Realm realm, final boolean isAutoLogin) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                UserSetting userSetting = findOrCreate(realm);
                userSetting.setAutoLogin(isAutoLogin);
            }
        });
    }

    public void updateShuffleState(Realm realm, final ShuffleState shuffleState) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                UserSetting userSetting = findOrCreate(realm);
                userSetting.applyShuffleState(shuffleState);
            }
        });
    }

    public void updateRepeatState(Realm realm, final RepeatState repeatState) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                UserSetting userSetting = findOrCreate(realm);
                userSetting.applyRepeatState(repeatState);
            }
        });
    }

    public void updateQueueIdentifyMediaId(Realm realm, final String queueIdentifyMediaId) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                UserSetting userSetting = findOrCreate(realm);
                userSetting.setQueueIdentifyMediaId(queueIdentifyMediaId);
            }
        });
    }

    public void updateLastMusicId(Realm realm, final String lastMusicId) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                UserSetting userSetting = findOrCreate(realm);
                userSetting.setLastMusicId(lastMusicId);
            }
        });
    }

    public void updateNewSongDays(Realm realm, final int newSongDays) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                UserSetting userSetting = findOrCreate(realm);
                userSetting.setNewSongDays(newSongDays);
            }
        });
    }

    public void updateMostPlayedSongSize(Realm realm, final int mostPlayedSongSize) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                UserSetting userSetting = findOrCreate(realm);
                userSetting.setMostPlayedSongSize(mostPlayedSongSize);
            }
        });
    }

    private UserSetting findOrCreate(Realm realm) {
        UserSetting userSetting = realm.where(UserSetting.class).findFirst();
        if (userSetting == null) {
            realm.beginTransaction();
            userSetting = realm.createObject(UserSetting.class);
            realm.commitTransaction();
        }
        return userSetting;
    }
}
