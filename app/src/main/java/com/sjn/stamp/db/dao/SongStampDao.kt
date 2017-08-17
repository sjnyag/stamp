package com.sjn.stamp.db.dao

import com.sjn.stamp.db.Song
import com.sjn.stamp.db.SongStamp

import io.realm.Realm
import io.realm.RealmList

object SongStampDao : BaseDao() {

    fun findAll(realm: Realm): List<SongStamp> =
            realm.where(SongStamp::class.java).findAll().sort("name")

    fun saveOrAdd(realm: Realm, rawSongStamp: SongStamp, rawSong: Song) {
        if (rawSong.mediaId == null) {
            return
        }
        realm.executeTransaction(Realm.Transaction { realm ->
            val managedSongStamp = realm.where(SongStamp::class.java).equalTo("name", rawSongStamp.name).findFirst()
            val managedSong = SongDao.findOrCreate(realm, rawSong)
            if (managedSongStamp == null) {
                rawSongStamp.id = getAutoIncrementId(realm, SongStamp::class.java)
                val songList = RealmList<Song>()
                songList.add(managedSong)
                rawSongStamp.songList = songList
                addSongStamp(managedSong, realm.copyToRealm(rawSongStamp))
            } else {
                if (managedSongStamp.songList == null) {
                    managedSongStamp.songList = RealmList()
                }
                for (song in managedSongStamp.songList!!) {
                    if (managedSong.mediaId == song.mediaId) {
                        return@Transaction
                    }
                }
                managedSongStamp.songList!!.add(managedSong)
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
        if (song.songStampList == null) {
            song.songStampList = RealmList()
        }
        song.songStampList!!
                .filter { it.name == songStamp.name }
                .forEach { return }
        song.songStampList!!.add(songStamp)
    }

}
