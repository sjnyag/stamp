package com.sjn.stamp.db.dao

import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.db.Song
import com.sjn.stamp.utils.MediaItemHelper
import io.realm.Realm

@Suppress("unused")
object SongDao : BaseDao() {

    fun findById(realm: Realm, id: Long): Song =
            realm.where(Song::class.java).equalTo("id", id).findFirst()

    fun findOrCreate(realm: Realm, rawSong: Song): Song {
        var song: Song? = realm.where(Song::class.java).equalTo("title", rawSong.title).equalTo("artist.name", rawSong.artist!!.name).findFirst()
        if (song == null) {
            val artist = ArtistDao.findOrCreate(realm, rawSong.artist!!)
            rawSong.artist = artist
            rawSong.id = getAutoIncrementId(realm, Song::class.java)
            song = realm.copyToRealm(rawSong)
        }
        return song!!
    }

    fun findByMusicId(realm: Realm, musicId: String): Song =
            realm.where(Song::class.java).equalTo("mediaId", musicId).findFirst()

    fun newStandalone(track: MediaMetadataCompat): Song = MediaItemHelper.createSong(track)
}