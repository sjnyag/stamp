package com.sjn.stamp.controller

import com.sjn.stamp.model.dao.UserSettingDao
import com.sjn.stamp.utils.RealmHelper

class UserSettingController {

    var queueIdentifyMediaId: String
        get() {
            return RealmHelper.realmInstance.use { realm ->
                UserSettingDao.getUserSetting(realm).queueIdentifyMediaId
            }
        }
        set(queueIdentifyMediaId) {
            return RealmHelper.realmInstance.use { realm ->
                UserSettingDao.updateQueueIdentifyMediaId(realm, queueIdentifyMediaId)
            }
        }

    var lastMusicId: String
        get() {
            return RealmHelper.realmInstance.use { realm ->
                UserSettingDao.getUserSetting(realm).lastMusicId
            }
        }
        set(lastMediaId) {
            return RealmHelper.realmInstance.use { realm ->
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
            return RealmHelper.realmInstance.use { realm ->
                UserSettingDao.getUserSetting(realm).newSongDays
            }
        }
        set(newSongDays) {
            RealmHelper.realmInstance.use { realm ->
                UserSettingDao.updateNewSongDays(realm, newSongDays)
            }
        }

    var mostPlayedSongSize: Int
        get() {
            return RealmHelper.realmInstance.use { realm ->
                UserSettingDao.getUserSetting(realm).mostPlayedSongSize
            }
        }
        set(mostPlayedSongSize) {
            RealmHelper.realmInstance.use { realm ->
                UserSettingDao.updateMostPlayedSongSize(realm, mostPlayedSongSize)
            }
        }
}
