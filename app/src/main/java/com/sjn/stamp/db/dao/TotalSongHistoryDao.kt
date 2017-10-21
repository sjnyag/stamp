package com.sjn.stamp.db.dao

import com.sjn.stamp.constant.RecordType
import com.sjn.stamp.db.TotalSongHistory
import io.realm.Realm
import io.realm.Sort

object TotalSongHistoryDao : BaseDao() {

    fun findPlayed(realm: Realm): List<TotalSongHistory> =
            realm.where(TotalSongHistory::class.java).greaterThanOrEqualTo("playCount", 1).findAll().sort("playCount", Sort.DESCENDING) ?: emptyList()

    fun findOrCreate(realm: Realm, songId: Long): TotalSongHistory {
        var totalSongHistory: TotalSongHistory? = realm.where(TotalSongHistory::class.java).equalTo("song.id", songId).findFirst()
        if (totalSongHistory == null) {
            realm.beginTransaction()
            totalSongHistory = realm.createObject(TotalSongHistory::class.java, getAutoIncrementId(realm, TotalSongHistory::class.java))
            realm.commitTransaction()
            return totalSongHistory
        }
        return totalSongHistory
    }

    fun increment(realm: Realm, songId: Long, recordType: RecordType): Int {
        val totalSongHistory = findOrCreate(realm, songId)
        realm.beginTransaction()
        totalSongHistory.increment(recordType)
        realm.commitTransaction()
        return totalSongHistory.playCount
    }
}