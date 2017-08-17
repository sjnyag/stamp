package com.sjn.stamp.controller

import com.sjn.stamp.constant.RepeatState
import com.sjn.stamp.constant.ShuffleState
import com.sjn.stamp.db.dao.UserSettingDao
import com.sjn.stamp.utils.RealmHelper

class UserSettingController {
    private val mUserSettingDao: UserSettingDao = UserSettingDao.getInstance()

    var shuffleState: ShuffleState?
        get() {
            val realm = RealmHelper.getRealmInstance()
            val userSetting = mUserSettingDao.getUserSetting(realm)
            val shuffleState = userSetting.fetchShuffleStateValue()
            realm.close()
            return shuffleState
        }
        set(shuffleState) {
            val realm = RealmHelper.getRealmInstance()
            mUserSettingDao.updateShuffleState(realm, shuffleState)
            realm.close()
        }

    var repeatState: RepeatState?
        get() {
            val realm = RealmHelper.getRealmInstance()
            val userSetting = mUserSettingDao.getUserSetting(realm)
            val repeatState = userSetting.fetchRepeatState()
            realm.close()
            return repeatState
        }
        set(repeatState) {
            val realm = RealmHelper.getRealmInstance()
            mUserSettingDao.updateRepeatState(realm, repeatState)
            realm.close()
        }

    var queueIdentifyMediaId: String
        get() {
            val realm = RealmHelper.getRealmInstance()
            val userSetting = mUserSettingDao.getUserSetting(realm)
            val queueIdentifyMediaId = userSetting.queueIdentifyMediaId
            realm.close()
            return queueIdentifyMediaId
        }
        set(queueIdentifyMediaId) {
            val realm = RealmHelper.getRealmInstance()
            mUserSettingDao.updateQueueIdentifyMediaId(realm, queueIdentifyMediaId)
            realm.close()
        }

    var lastMusicId: String?
        get() {
            val realm = RealmHelper.getRealmInstance()
            val userSetting = mUserSettingDao.getUserSetting(realm)
            val lastMusicId = userSetting.lastMusicId
            realm.close()
            return lastMusicId
        }
        set(lastMediaId) {
            val realm = RealmHelper.getRealmInstance()
            mUserSettingDao.updateLastMusicId(realm, lastMediaId)
            realm.close()
        }

    fun stopOnAudioLostFocus(): Boolean = false
    /*
    Realm realm = RealmHelper.getRealmInstance();
    UserSetting userSetting = mUserSettingDao.getUserSetting(realm);
    boolean result = userSetting.getStopOnAudioLostFocus();
    realm.close();
    return result;
    */

    var newSongDays: Int
        get() {
            val realm = RealmHelper.getRealmInstance()
            val userSetting = mUserSettingDao.getUserSetting(realm)
            val newSongDays = userSetting.newSongDays
            realm.close()
            return newSongDays
        }
        set(newSongDays) {
            val realm = RealmHelper.getRealmInstance()
            mUserSettingDao.updateNewSongDays(realm, newSongDays)
            realm.close()
        }

    var mostPlayedSongSize: Int
        get() {
            val realm = RealmHelper.getRealmInstance()
            val userSetting = mUserSettingDao.getUserSetting(realm)
            val topSongSize = userSetting.mostPlayedSongSize
            realm.close()
            return topSongSize
        }
        set(mostPlayedSongSize) {
            val realm = RealmHelper.getRealmInstance()
            mUserSettingDao.updateMostPlayedSongSize(realm, mostPlayedSongSize)
            realm.close()
        }
}
