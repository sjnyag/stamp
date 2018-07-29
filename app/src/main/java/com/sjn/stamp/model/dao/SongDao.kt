package com.sjn.stamp.model.dao

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.model.Song
import com.sjn.stamp.utils.MediaItemHelper
import com.sjn.stamp.utils.MediaRetrieveHelper
import io.realm.Realm

object SongDao : BaseDao<Song>() {

    fun findById(realm: Realm, id: Long): Song? =
            realm.where(Song::class.java).equalTo("id", id).findFirst()

    private fun findByMediaId(realm: Realm, mediaId: String): Song? =
            realm.where(Song::class.java).equalTo("mediaId", mediaId).findFirst()

    @Suppress("unused")
    fun findByTitleArtistAlbum(realm: Realm, title: String, artist: String, album: String): Song? =
            realm.where(Song::class.java).equalTo("title", title).equalTo("album", album).equalTo("artist.name", artist).findFirst()

    fun findByMediaMetadata(realm: Realm, metadata: MediaMetadataCompat): Song? =
            realm.where(Song::class.java).equalTo("title", MediaItemHelper.getTitle(metadata)).equalTo("album", MediaItemHelper.getAlbum(metadata)).equalTo("artist.name", MediaItemHelper.getArtist(metadata)).findFirst()

    fun findUnknown(realm: Realm): List<Song> {
        val mediaId: String? = null
        return realm.where(Song::class.java).equalTo("mediaId", "").or().equalTo("mediaId", mediaId).findAll()
                ?: emptyList()
    }

    fun findAll(realm: Realm): List<Song> =
            realm.where(Song::class.java).findAll() ?: emptyList()

    fun findOrCreate(realm: Realm, metadata: MediaMetadataCompat): Song {
        var song: Song? = findByMediaMetadata(realm, metadata)
        if (song == null) {
            song = Song()
            song.id = getAutoIncrementId(realm)
            val artist = ArtistDao.findOrCreate(realm, MediaItemHelper.getArtist(metadata), MediaItemHelper.getAlbumArtUri(metadata))
            val totalSongHistory = TotalSongHistoryDao.findOrCreate(realm, song.id)
            song.artist = artist
            song.totalSongHistory = totalSongHistory
            song.loadMediaMetadataCompat(metadata)
            realm.beginTransaction()
            realm.copyToRealm(song)
            realm.commitTransaction()
            return song
        }
        return song
    }

    fun findOrCreateByMusicId(realm: Realm, mediaId: String, context: Context): Song? {
        var song: Song? = findByMediaId(realm, mediaId)
        if (song == null) {
            try {
                val metadata = MediaRetrieveHelper.findByMusicId(context, mediaId.toLong(), null)
                metadata ?: return null
                song = Song()
                song.id = getAutoIncrementId(realm)
                val artist = ArtistDao.findOrCreate(realm, MediaItemHelper.getArtist(metadata), MediaItemHelper.getAlbumArtUri(metadata))
                val totalSongHistory = TotalSongHistoryDao.findOrCreate(realm, song.id)
                song.artist = artist
                song.totalSongHistory = totalSongHistory
                song.loadMediaMetadataCompat(metadata)
                realm.beginTransaction()
                realm.copyToRealm(song)
                realm.commitTransaction()
                return song
            } catch (e: NumberFormatException) {
                return null
            }
        }
        return song
    }

    fun loadLocalMedia(realm: Realm, songId: Long, musicList: Collection<MediaMetadataCompat>): Boolean {
        val song = findById(realm, songId) ?: return false
        val localMedia = musicList.find { MediaItemHelper.isSameSong(it, song) }
        if (localMedia == null) {
            saveUnknown(realm, song.id)
            return false
        }
        val realmSong = findById(realm, song.id) ?: return false
        realm.beginTransaction()
        realmSong.loadMediaMetadataCompat(localMedia)
        realm.commitTransaction()
        return true
    }

    fun merge(realm: Realm, unknownSongId: Long, songId: Long): Boolean {
        val unknownSong = findById(realm, unknownSongId) ?: return false
        val newSong = findById(realm, songId) ?: return false
        realm.beginTransaction()
        newSong.merge(unknownSong)
        unknownSong.deleteFromRealm()
        realm.commitTransaction()
        return true
    }

    fun delete(realm: Realm, id: Long) {
        realm.executeTransactionAsync { r -> r.where(Song::class.java).equalTo("id", id).findFirst().deleteFromRealm() }
    }

    private fun saveUnknown(realm: Realm, songId: Long) {
        val realmSong = findById(realm, songId) ?: return
        realm.beginTransaction()
        realmSong.mediaId = ""
        realm.commitTransaction()
    }

}