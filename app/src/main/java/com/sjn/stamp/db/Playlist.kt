package com.sjn.stamp.db

import android.support.v4.media.MediaMetadataCompat
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Playlist(
        @PrimaryKey var id: Long = 0,
        var name: String? = null,
        var active: Boolean? = null,
        var createdAt: Date? = null,
        var updatedAt: Date? = null,
        var songs: RealmList<PlaylistSong>? = null

) : RealmObject() {

    companion object {

        fun create(name: String, mediaList: List<MediaMetadataCompat>): Playlist {
            val playlist = Playlist()
            playlist.songs = createSong(mediaList)
            playlist.name = name
            return playlist
        }

        private fun createSong(mediaList: List<MediaMetadataCompat>): RealmList<PlaylistSong> {
            val playlistSongList = RealmList<PlaylistSong>()
            for (media in mediaList) {
                val playlistSong = PlaylistSong()
                playlistSong.artist = media.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
                playlistSong.title = media.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                playlistSongList.add(playlistSong)
            }
            return playlistSongList
        }
    }
}
