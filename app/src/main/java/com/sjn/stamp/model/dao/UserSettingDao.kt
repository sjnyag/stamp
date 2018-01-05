package com.sjn.stamp.model.dao

import com.sjn.stamp.model.constant.RepeatState
import com.sjn.stamp.model.constant.ShuffleState
import com.sjn.stamp.model.UserSetting

import io.realm.Realm

object UserSettingDao : BaseDao<UserSetting>() {

    fun getUserSetting(realm: Realm): UserSetting = findOrCreate(realm)

    fun updateShuffleState(realm: Realm, shuffleState: ShuffleState) {
        realm.executeTransactionAsync { r -> findOrCreate(r).applyShuffleState(shuffleState) }
    }

    fun updateRepeatState(realm: Realm, repeatState: RepeatState) {
        realm.executeTransactionAsync { r -> findOrCreate(r).applyRepeatState(repeatState) }
    }

    fun updateQueueIdentifyMediaId(realm: Realm, queueIdentifyMediaId: String) {
        realm.executeTransactionAsync { r -> findOrCreate(r).queueIdentifyMediaId = queueIdentifyMediaId }
    }

    fun updateLastMusicId(realm: Realm, lastMusicId: String) {
        realm.executeTransactionAsync { r -> findOrCreate(r).lastMusicId = lastMusicId }
    }

    fun updateNewSongDays(realm: Realm, newSongDays: Int) {
        realm.executeTransactionAsync { r -> findOrCreate(r).newSongDays = newSongDays }
    }

    fun updateMostPlayedSongSize(realm: Realm, mostPlayedSongSize: Int) {
        realm.executeTransactionAsync { r -> findOrCreate(r).mostPlayedSongSize = mostPlayedSongSize }
    }

    private fun findOrCreate(realm: Realm): UserSetting {
        var userSetting: UserSetting? = realm.where(UserSetting::class.java).findFirst()
        if (userSetting == null) {
            realm.beginTransaction()
            userSetting = realm.createObject(UserSetting::class.java)
            realm.commitTransaction()
            return userSetting
        }
        return userSetting
    }
}
