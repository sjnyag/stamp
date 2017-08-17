package com.sjn.stamp.db.dao

import com.sjn.stamp.db.SongHistory
import com.sjn.stamp.db.TotalSongHistory

import io.realm.Realm
import io.realm.Sort

object TotalSongHistoryDao : BaseDao() {

    fun getOrderedList(realm: Realm): List<TotalSongHistory> =
            realm.where(TotalSongHistory::class.java).greaterThanOrEqualTo("playCount", 1).findAll().sort("playCount", Sort.DESCENDING)

    fun saveOrIncrement(realm: Realm, rawTotalSongHistory: TotalSongHistory): Int {
        val playCount: Int
        realm.beginTransaction()
        val totalSongHistory = realm.where(TotalSongHistory::class.java)
                .equalTo("song.mediaId", rawTotalSongHistory.song!!.mediaId)
                .findFirst()
        if (totalSongHistory == null) {
            rawTotalSongHistory.id = getAutoIncrementId(realm, TotalSongHistory::class.java)
            rawTotalSongHistory.song = SongDao.findOrCreate(realm, rawTotalSongHistory.song!!)
            realm.insert(rawTotalSongHistory)
            playCount = rawTotalSongHistory.playCount
        } else {
            totalSongHistory.incrementPlayCount(rawTotalSongHistory.playCount)
            totalSongHistory.incrementSkipCount(rawTotalSongHistory.skipCount)
            totalSongHistory.incrementCompleteCount(rawTotalSongHistory.completeCount)
            playCount = totalSongHistory.playCount
        }
        realm.commitTransaction()
        return playCount
    }

    fun save(realm: Realm, songQueueId: Long, playCount: Int, skipCount: Int, completeCount: Int) {
        realm.beginTransaction()
        val songHistory = realm.where(SongHistory::class.java).equalTo("id", songQueueId).findFirst()
        if (songHistory == null) {
            realm.cancelTransaction()
            return
        }
        var totalSongHistory: TotalSongHistory? = realm.where(TotalSongHistory::class.java)
                .equalTo("song.mediaId", songHistory.song!!.mediaId)
                .findFirst()
        if (totalSongHistory == null) {
            totalSongHistory = realm.createObject(TotalSongHistory::class.java, getAutoIncrementId(realm, TotalSongHistory::class.java))
            totalSongHistory!!.song = SongDao.findOrCreate(realm, songHistory.song!!)
        }
        totalSongHistory.updatePlayCountIfOver(playCount)
        totalSongHistory.updateSkipCountIfOver(skipCount)
        totalSongHistory.updateCompleteCountIfOver(completeCount)
        realm.commitTransaction()
    }

    fun newStandalone(): TotalSongHistory = TotalSongHistory()
}