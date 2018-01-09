package com.sjn.stamp.model.dao

import com.sjn.stamp.model.Song
import com.sjn.stamp.model.SongHistory
import com.sjn.stamp.model.constant.RecordType
import io.realm.Realm
import io.realm.Sort
import java.util.*

object SongHistoryDao : BaseDao<SongHistory>() {

    fun where(realm: Realm, from: Date?, to: Date?, recordType: String): List<SongHistory> {
        val query = realm.where(SongHistory::class.java).equalTo("recordType", recordType)
        if (from != null) {
            query.greaterThanOrEqualTo("recordedAt", from)
        }
        if (to != null) {
            query.lessThanOrEqualTo("recordedAt", to)
        }
        return query.findAll() ?: emptyList()
    }

    fun timeline(realm: Realm, recordType: String): List<SongHistory> =
            findAll(realm, recordType)

    fun findAll(realm: Realm, recordType: String): List<SongHistory> =
            realm.where(SongHistory::class.java).equalTo("recordType", recordType).findAllSorted("recordedAt", Sort.DESCENDING) ?: emptyList()

    fun findPlayRecordByArtist(realm: Realm, artistName: String): List<SongHistory> =
            realm.where(SongHistory::class.java).equalTo("recordType", RecordType.PLAY.databaseValue).equalTo("song.artist.name", artistName).findAll() ?: emptyList()

    fun findOldestByArtist(realm: Realm, artistName: String): SongHistory? =
            realm.where(SongHistory::class.java).equalTo("recordType", RecordType.PLAY.databaseValue).equalTo("song.artist.name", artistName).findAllSorted("recordedAt", Sort.ASCENDING).first()

    fun findOldest(realm: Realm, songId: Long): SongHistory? =
            realm.where(SongHistory::class.java).equalTo("song.id", songId).equalTo("recordType", RecordType.PLAY.databaseValue).findAllSorted("recordedAt", Sort.ASCENDING).first()

    fun create(realm: Realm, rawSong: Song, recordType: RecordType, date: Date, count: Int) {
        SongDao.findById(realm, rawSong.id)?.let { song ->
            realm.executeTransaction({
                realm.copyToRealm(SongHistory().apply {
                    this.id = getAutoIncrementId(realm)
                    this.device = DeviceDao.findOrCreate(realm)
                    this.recordType = recordType.databaseValue
                    this.recordedAt = date
                    this.count = count
                    this.song = song
                })
            })
        }
    }

    fun delete(realm: Realm, songHistoryId: Long) {
        realm.executeTransactionAsync { r -> r.where(SongHistory::class.java).equalTo("id", songHistoryId).findFirst().deleteFromRealm() }
    }

}