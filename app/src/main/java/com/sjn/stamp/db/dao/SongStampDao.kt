package com.sjn.stamp.db.dao

import com.sjn.stamp.db.Song
import com.sjn.stamp.db.SongStamp

import io.realm.Realm
import io.realm.RealmList

object SongStampDao : BaseDao() {

    fun findAll(realm: Realm): List<SongStamp> =
            realm.where(SongStamp::class.java).findAll().sort("name")

    fun saveOrAdd(realm: Realm, rawSongStamp: SongStamp, rawSong: Song) {
        realm.executeTransaction(Realm.Transaction { r ->
            val managedSongStamp = r.where(SongStamp::class.java).equalTo("name", rawSongStamp.name).findFirst()
            val managedSong = SongDao.findOrCreate(r, rawSong)
            if (managedSongStamp == null) {
                rawSongStamp.id = getAutoIncrementId(r, SongStamp::class.java)
                val songList = RealmList<Song>()
                songList.add(managedSong)
                rawSongStamp.songList = songList
                addSongStamp(managedSong, r.copyToRealm(rawSongStamp))
            } else {
                managedSongStamp.songList
                        .filter { managedSong.mediaId == it.mediaId }
                        .forEach { return@Transaction }
                managedSongStamp.songList.add(managedSong)
                addSongStamp(managedSong, managedSongStamp)
            }
        })
    }

    fun remove(realm: Realm, name: String) {
        realm.beginTransaction()
        realm.where(SongStamp::class.java).equalTo("name", name).findAll().deleteAllFromRealm()
        realm.commitTransaction()
    }

    fun save(realm: Realm, name: String?): Boolean {
        var result = false
        if (name == null || name.isEmpty()) {
            return false
        }
        realm.beginTransaction()
        var songStamp: SongStamp? = realm.where(SongStamp::class.java).equalTo("name", name).findFirst()
        if (songStamp == null) {
            songStamp = realm.createObject(SongStamp::class.java, getAutoIncrementId(realm, SongStamp::class.java))
            songStamp!!.name = name
            songStamp.isSystem = false
            result = true
        }
        realm.commitTransaction()
        return result
    }

    fun newStandalone(): SongStamp = SongStamp()

    private fun addSongStamp(song: Song, songStamp: SongStamp?) {
        if (songStamp?.name == null) {
            return
        }
        song.songStampList
                .filter { it.name == songStamp.name }
                .forEach { return }
        song.songStampList.add(songStamp)
    }

}
