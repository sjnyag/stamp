package com.sjn.stamp.db.dao

import com.sjn.stamp.db.Playlist
import com.sjn.stamp.db.PlaylistSong

import io.realm.Realm

@Suppress("unused")
object PlaylistDao : BaseDao() {

    fun save(realm: Realm, rawPlaylist: Playlist): Playlist {
        realm.beginTransaction()
        rawPlaylist.id = getAutoIncrementId(realm, Playlist::class.java)
        var id = getAutoIncrementId(realm, PlaylistSong::class.java)!!.toInt()
        for (playlistSong in rawPlaylist.songs!!) {
            playlistSong.id = id++.toLong()
        }
        realm.commitTransaction()
        return rawPlaylist
    }
}