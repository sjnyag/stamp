package com.sjn.stamp.db.dao

import com.sjn.stamp.constant.RecordType
import com.sjn.stamp.db.Song
import com.sjn.stamp.db.SongHistory
import io.realm.Realm
import io.realm.Sort
import java.util.*

object SongHistoryDao : BaseDao() {

    fun timeline(realm: Realm, recordType: String): List<SongHistory> = findAll(realm, recordType)

    fun where(realm: Realm, from: Date?, to: Date?, recordType: String): List<SongHistory> {
        val query = realm.where(SongHistory::class.java).equalTo("recordType", recordType)
        if (from != null) {
            query.greaterThanOrEqualTo("recordedAt", from)
        }
        if (to != null) {
            query.lessThanOrEqualTo("recordedAt", to)
        }
        return query.findAll()
    }

    fun findPlayRecordByArtist(realm: Realm, artistName: String): List<SongHistory> =
            realm.where(SongHistory::class.java).equalTo("recordType", RecordType.PLAY.value).equalTo("song.artist.name", artistName).findAll()

    fun findOldestByArtist(realm: Realm, artistName: String): SongHistory =
            realm.where(SongHistory::class.java).equalTo("recordType", RecordType.PLAY.value).equalTo("song.artist.name", artistName).findAllSorted("recordedAt", Sort.ASCENDING).first()

    fun findOldest(realm: Realm, song: Song): SongHistory =
            realm.where(SongHistory::class.java).equalTo("song.mediaId", song.mediaId).equalTo("recordType", RecordType.PLAY.value).findAllSorted("recordedAt", Sort.ASCENDING).first()

    fun save(realm: Realm, songHistory: SongHistory) {
        realm.executeTransaction { realm ->
            songHistory.id = getAutoIncrementId(realm, SongHistory::class.java)
            songHistory.device = DeviceDao.findOrCreate(realm, songHistory.device!!)
            songHistory.song = SongDao.findOrCreate(realm, songHistory.song!!)
            realm.insert(songHistory)
        }
    }

    fun remove(realm: Realm, id: Long) {
        realm.executeTransactionAsync { realm -> realm.where(SongHistory::class.java).equalTo("id", id).findFirst().deleteFromRealm() }
    }

    fun findAll(realm: Realm): List<SongHistory> = realm.where(SongHistory::class.java).findAll()

    fun findAll(realm: Realm, recordType: String): List<SongHistory> =
            realm.where(SongHistory::class.java).equalTo("recordType", recordType).findAllSorted("recordedAt", Sort.DESCENDING)

    fun newStandalone(): SongHistory = SongHistory()

}