package com.sjn.stamp.db.dao

import com.sjn.stamp.db.SongStamp
import io.realm.Realm
import io.realm.RealmList

object SongStampDao : BaseDao() {

    fun find(realm: Realm, name: String, isSystem: Boolean): SongStamp? =
            realm.where(SongStamp::class.java).equalTo("name", name).equalTo("isSystem", isSystem).findFirst()

    fun findAll(realm: Realm): List<SongStamp> =
            realm.where(SongStamp::class.java).findAll().sort("name") ?: emptyList()

    fun findAll(realm: Realm, isSystem: Boolean): List<SongStamp> =
            realm.where(SongStamp::class.java).equalTo("isSystem", isSystem).findAll().sort("name") ?: emptyList()

    fun register(realm: Realm, songId: Long, name: String, isSystem: Boolean) {
        val song = SongDao.findById(realm, songId) ?: return
        val songStamp = findOrCreate(realm, name, isSystem)
        realm.beginTransaction()
        songStamp.addSong(song)
        realm.commitTransaction()
    }

    fun remove(realm: Realm, songId: Long, name: String, isSystem: Boolean): Boolean {
        val song = SongDao.findById(realm, songId) ?: return false
        val songStamp = findOrCreate(realm, name, isSystem)
        if (!songStamp.songList.contains(song)) {
            return false
        }
        realm.beginTransaction()
        songStamp.removeSong(song)
        realm.commitTransaction()
        return true
    }

    fun findOrCreate(realm: Realm, name: String, isSystem: Boolean): SongStamp {
        if (name.isEmpty()) {
            throw RuntimeException("Stamp name is empty.")
        }
        var songStamp: SongStamp? = find(realm, name, isSystem)
        if (songStamp == null) {
            realm.beginTransaction()
            songStamp = realm.createObject(SongStamp::class.java, getAutoIncrementId(realm, SongStamp::class.java))
            songStamp.name = name
            songStamp.isSystem = isSystem
            songStamp.songList = RealmList()
            realm.commitTransaction()
            return songStamp
        }
        return songStamp
    }

    fun delete(realm: Realm, name: String, isSystem: Boolean) {
        realm.beginTransaction()
        realm.where(SongStamp::class.java).equalTo("name", name).equalTo("isSystem", isSystem).findAll().deleteAllFromRealm()
        realm.commitTransaction()
    }
}
