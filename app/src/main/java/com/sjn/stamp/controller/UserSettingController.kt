package com.sjn.stamp.controller

import com.sjn.stamp.model.constant.RepeatState
import com.sjn.stamp.model.constant.ShuffleState
import com.sjn.stamp.model.dao.UserSettingDao
import com.sjn.stamp.utils.RealmHelper

class UserSettingController {

    var shuffleState: ShuffleState
        get() {
            return RealmHelper.getRealmInstance().use { realm ->
                UserSettingDao.getUserSetting(realm).fetchShuffleStateValue()
            }
        }
        set(shuffleState) {
            RealmHelper.getRealmInstance().use { realm ->
                UserSettingDao.updateShuffleState(realm, shuffleState)
            }
        }

    var repeatState: RepeatState
        get() {
            return RealmHelper.getRealmInstance().use { realm ->
                UserSettingDao.getUserSetting(realm).fetchRepeatState()
            }
        }
        set(repeatState) {
            return RealmHelper.getRealmInstance().use { realm ->
                UserSettingDao.updateRepeatState(realm, repeatState)
            }
        }

    var queueIdentifyMediaId: String
        get() {
            return RealmHelper.getRealmInstance().use { realm ->
                UserSettingDao.getUserSetting(realm).queueIdentifyMediaId
            }
        }
        set(queueIdentifyMediaId) {
            return RealmHelper.getRealmInstance().use { realm ->
                UserSettingDao.updateQueueIdentifyMediaId(realm, queueIdentifyMediaId)
            }
        }

    var lastMusicId: String
        get() {
            return RealmHelper.getRealmInstance().use { realm ->
                UserSettingDao.getUserSetting(realm).lastMusicId
            }
        }
        set(lastMediaId) {
            return RealmHelper.getRealmInstance().use { realm ->
                UserSettingDao.updateLastMusicId(realm, lastMediaId)
            }
        }

    fun stopOnAudioLostFocus(): Boolean = false
    /*
    Realm realm = RealmHelper.getRealmInstance();
    UserSetting userSetting = UserSettingDao.getUserSetting(realm);
    boolean result = userSetting.getStopOnAudioLostFocus();
    realm.close();
    return result;
    */

    var newSongDays: Int
        get() {
            return RealmHelper.getRealmInstance().use { realm ->
                UserSettingDao.getUserSetting(realm).newSongDays
            }
        }
        set(newSongDays) {
            RealmHelper.getRealmInstance().use { realm ->
                UserSettingDao.updateNewSongDays(realm, newSongDays)
            }
        }

    var mostPlayedSongSize: Int
        get() {
            return RealmHelper.getRealmInstance().use { realm ->
                UserSettingDao.getUserSetting(realm).mostPlayedSongSize
            }
        }
        set(mostPlayedSongSize) {
            RealmHelper.getRealmInstance().use { realm ->
                UserSettingDao.updateMostPlayedSongSize(realm, mostPlayedSongSize)
            }
        }
}
