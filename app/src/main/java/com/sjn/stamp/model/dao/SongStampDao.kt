package com.sjn.stamp.model.dao

import com.sjn.stamp.model.SongStamp
import io.realm.Realm
import io.realm.RealmList

object SongStampDao : BaseDao<SongStamp>() {

    fun isMyStampExists(realm: Realm): Boolean = realm.where(SongStamp::class.java).equalTo("isSystem", false).findFirst() != null

    fun isSmartStampExists(realm: Realm): Boolean = realm.where(SongStamp::class.java).equalTo("isSystem", true).findFirst() != null

    fun find(realm: Realm, name: String, isSystem: Boolean): SongStamp? =
            realm.where(SongStamp::class.java).equalTo("name", name).equalTo("isSystem", isSystem).findFirst()

    fun findAll(realm: Realm): List<SongStamp> =
            realm.where(SongStamp::class.java).findAll().sort("name") ?: emptyList()

    fun findAll(realm: Realm, isSystem: Boolean): List<SongStamp> =
            realm.where(SongStamp::class.java).equalTo("isSystem", isSystem).findAll().sort("name")
                    ?: emptyList()

    fun register(realm: Realm, songId: Long, name: String, isSystem: Boolean) {
        realm.executeTransaction {
            val song = SongDao.findById(realm, songId)
            if (song != null) {
                val songStamp = findOrCreate(realm, name, isSystem)
                songStamp.addSong(song)
            }
        }
    }

    fun remove(realm: Realm, songId: Long, name: String, isSystem: Boolean): Boolean {
        var result = false
        realm.executeTransaction {
            val song = SongDao.findById(realm, songId)
            val songStamp = findOrCreate(realm, name, isSystem)
            if (song != null && songStamp.songList.contains(song)) {
                songStamp.removeSong(song)
                result = true
            }
        }
        return result
    }

    fun findOrCreate(realm: Realm, name: String, isSystem: Boolean): SongStamp {
        if (name.isEmpty()) {
            throw RuntimeException("Stamp name is empty.")
        }
        var songStamp: SongStamp? = find(realm, name, isSystem)
        if (songStamp == null) {
            if (realm.isInTransaction) {
                return create(realm, name, isSystem)
            } else {
                realm.beginTransaction()
                songStamp = create(realm, name, isSystem)
                realm.commitTransaction()
            }
        }
        return songStamp
    }

    fun delete(realm: Realm, name: String, isSystem: Boolean) {
        realm.executeTransaction {
            realm.where(SongStamp::class.java).equalTo("name", name).equalTo("isSystem", isSystem).findAll().deleteAllFromRealm()
        }
    }

    private fun create(realm: Realm, name: String, isSystem: Boolean): SongStamp {
        val songStamp = realm.createObject(SongStamp::class.java, getAutoIncrementId(realm))
        songStamp.name = name
        songStamp.isSystem = isSystem
        songStamp.songList = RealmList()
        return songStamp
    }
}
