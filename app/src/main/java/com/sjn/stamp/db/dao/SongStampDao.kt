package com.sjn.stamp.db.dao

import com.sjn.stamp.db.Song
import com.sjn.stamp.db.SongStamp

import io.realm.Realm
import io.realm.RealmList

object SongStampDao : BaseDao() {

    fun findAll(realm: Realm): List<SongStamp> =
            realm.where(SongStamp::class.java).findAll().sort("name")

    fun findAll(realm: Realm, isSystem: Boolean): List<SongStamp> =
            realm.where(SongStamp::class.java).equalTo("isSystem", isSystem).findAll().sort("name")

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

    fun remove(realm: Realm, name: String, isSystem: Boolean) {
        realm.beginTransaction()
        realm.where(SongStamp::class.java).equalTo("name", name).equalTo("isSystem", isSystem).findAll().deleteAllFromRealm()
        realm.commitTransaction()
    }

    fun save(realm: Realm, name: String, isSystem: Boolean): Boolean {
        var result = false
        if (name.isEmpty()) {
            return false
        }
        realm.beginTransaction()
        var songStamp: SongStamp? = realm.where(SongStamp::class.java).equalTo("name", name).equalTo("isSystem", isSystem).findFirst()
        if (songStamp == null) {
            songStamp = realm.createObject(SongStamp::class.java, getAutoIncrementId(realm, SongStamp::class.java))
            songStamp!!.name = name
            songStamp.isSystem = isSystem
            result = true
        }
        realm.commitTransaction()
        return result
    }

    fun newStandalone(name: String, isSystem: Boolean): SongStamp = SongStamp(name = name, isSystem = isSystem)

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
