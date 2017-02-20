package com.sjn.taggingplayer.db.dao;

import com.sjn.taggingplayer.constant.RepeatState;
import com.sjn.taggingplayer.constant.ShuffleState;
import com.sjn.taggingplayer.db.UserSetting;

import io.realm.Realm;

public class UserSettingDao extends BaseDao {

    private static UserSettingDao sInstance;

    public static UserSettingDao getInstance() {
        if (sInstance == null) {
            sInstance = new UserSettingDao();
        }
        return sInstance;
    }

    public UserSetting getUserSetting(Realm realm) {
        return find(realm);
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
                userSetting.setShuffleState(shuffleState);
            }
        });
    }

    public void updateRepeatState(Realm realm, final RepeatState repeatState) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                UserSetting userSetting = findOrCreate(realm);
                userSetting.setRepeatState(repeatState);
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

    private UserSetting find(Realm realm) {
        return realm.where(UserSetting.class).findFirst();
    }

    private UserSetting findOrCreate(Realm realm) {
        UserSetting userSetting = realm.where(UserSetting.class).findFirst();
        if (userSetting == null) {
            userSetting = realm.createObject(UserSetting.class);
        }
        return userSetting;
    }

}
