package com.sjn.stamp.model.dao

import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.model.Song
import com.sjn.stamp.model.constant.RecordType
import com.sjn.stamp.model.SongHistory
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

    fun create(realm: Realm, song: Song, recordType: RecordType, date: Date, count: Int) {
        val songHistory = SongHistory()
        songHistory.id = getAutoIncrementId(realm)
        songHistory.device = DeviceDao.findOrCreate(realm)
        songHistory.song = song
        songHistory.recordType = recordType.databaseValue
        songHistory.recordedAt = date
        songHistory.count = count
        realm.beginTransaction()
        realm.copyToRealm(songHistory)
        realm.commitTransaction()
    }

    fun delete(realm: Realm, songHistoryId: Long) {
        realm.executeTransactionAsync { r -> r.where(SongHistory::class.java).equalTo("id", songHistoryId).findFirst().deleteFromRealm() }
    }

}