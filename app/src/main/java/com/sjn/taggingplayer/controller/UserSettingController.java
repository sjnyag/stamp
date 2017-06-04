package com.sjn.taggingplayer.controller;

import android.content.Context;

import com.sjn.taggingplayer.constant.RepeatState;
import com.sjn.taggingplayer.constant.ShuffleState;
import com.sjn.taggingplayer.db.UserSetting;
import com.sjn.taggingplayer.db.dao.UserSettingDao;
import com.sjn.taggingplayer.utils.MediaIDHelper;
import com.sjn.taggingplayer.utils.RealmHelper;

import io.realm.Realm;

public class UserSettingController {

    private Context mContext;
    private UserSettingDao mUserSettingDao;

    public UserSettingController(Context context) {
        mUserSettingDao = UserSettingDao.getInstance();
        mContext = context;
    }

    public ShuffleState getShuffleState() {
        Realm realm = RealmHelper.getRealmInstance();
        UserSetting userSetting = mUserSettingDao.getUserSetting(realm);
        ShuffleState shuffleState = userSetting == null || userSetting.getShuffleState() == null ? ShuffleState.getDefault() : userSetting.getShuffleState();
        realm.close();
        return shuffleState;
    }

    public RepeatState getRepeatState() {
        Realm realm = RealmHelper.getRealmInstance();
        UserSetting userSetting = mUserSettingDao.getUserSetting(realm);
        RepeatState repeatState = userSetting == null || userSetting.getRepeatState() == null ? RepeatState.getDefault() : userSetting.getRepeatState();
        realm.close();
        return repeatState;
    }

    public String getQueueIdentifyMediaId() {
        Realm realm = RealmHelper.getRealmInstance();
        UserSetting userSetting = mUserSettingDao.getUserSetting(realm);
        String queueIdentifyMediaId = userSetting == null || userSetting.getQueueIdentifyMediaId() == null ? MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL : userSetting.getQueueIdentifyMediaId();
        realm.close();
        return queueIdentifyMediaId;
    }

    public String getLastMusicId() {
        Realm realm = RealmHelper.getRealmInstance();
        UserSetting userSetting = mUserSettingDao.getUserSetting(realm);
        String lastMusicId = userSetting == null ? null : userSetting.getLastMusicId();
        realm.close();
        return lastMusicId;
    }

    public void setShuffleState(ShuffleState shuffleState) {
        Realm realm = RealmHelper.getRealmInstance();
        mUserSettingDao.updateShuffleState(realm, shuffleState);
        realm.close();
    }

    public void setRepeatState(RepeatState repeatState) {
        Realm realm = RealmHelper.getRealmInstance();
        mUserSettingDao.updateRepeatState(realm, repeatState);
        realm.close();
    }

    public void setQueueIdentifyMediaId(String queueIdentifyMediaId) {
        Realm realm = RealmHelper.getRealmInstance();
        mUserSettingDao.updateQueueIdentifyMediaId(realm, queueIdentifyMediaId);
        realm.close();
    }

    public void setLastMusicId(String lastMediaId) {
        Realm realm = RealmHelper.getRealmInstance();
        mUserSettingDao.updateLastMusicId(realm, lastMediaId);
        realm.close();
    }

    //TODO: implement
    public boolean stopOnAudioLostFocus() {
        return false;
    }

    //TODO: implement
    public boolean stopOnLongTimeUnuse() {
        return false;
    }
}
