package com.sjn.stamp.db.dao

import com.sjn.stamp.constant.RepeatState
import com.sjn.stamp.constant.ShuffleState
import com.sjn.stamp.db.UserSetting

import io.realm.Realm

object UserSettingDao : BaseDao() {

    fun getUserSetting(realm: Realm): UserSetting = findOrCreate(realm)

    fun updateShuffleState(realm: Realm, shuffleState: ShuffleState) {
        realm.executeTransactionAsync { r ->
            val userSetting = findOrCreate(r)
            userSetting.applyShuffleState(shuffleState)
        }
    }

    fun updateRepeatState(realm: Realm, repeatState: RepeatState) {
        realm.executeTransactionAsync { r ->
            val userSetting = findOrCreate(r)
            userSetting.applyRepeatState(repeatState)
        }
    }

    fun updateQueueIdentifyMediaId(realm: Realm, queueIdentifyMediaId: String) {
        realm.executeTransactionAsync { r ->
            val userSetting = findOrCreate(r)
            userSetting.queueIdentifyMediaId = queueIdentifyMediaId
        }
    }

    fun updateLastMusicId(realm: Realm, lastMusicId: String) {
        realm.executeTransactionAsync { r ->
            val userSetting = findOrCreate(r)
            userSetting.lastMusicId = lastMusicId
        }
    }

    fun updateNewSongDays(realm: Realm, newSongDays: Int) {
        realm.executeTransactionAsync { r ->
            val userSetting = findOrCreate(r)
            userSetting.newSongDays = newSongDays
        }
    }

    fun updateMostPlayedSongSize(realm: Realm, mostPlayedSongSize: Int) {
        realm.executeTransactionAsync { r ->
            val userSetting = findOrCreate(r)
            userSetting.mostPlayedSongSize = mostPlayedSongSize
        }
    }

    private fun findOrCreate(realm: Realm): UserSetting {
        var userSetting: UserSetting? = realm.where(UserSetting::class.java).findFirst()
        if (userSetting == null) {
            realm.beginTransaction()
            userSetting = realm.createObject(UserSetting::class.java)
            realm.commitTransaction()
        }
        return userSetting!!
    }
}
