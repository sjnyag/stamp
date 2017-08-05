package com.sjn.stamp.db

import android.content.res.Resources
import android.media.MediaMetadata
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.R
import com.sjn.stamp.db.dao.ArtistDao
import com.sjn.stamp.media.source.MusicProviderSource
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class Song(
        @PrimaryKey var id: Long = 0,
        @Index var mediaId: String? = null,
        var trackSource: String? = null,
        @Index var album: String? = null,
        var duration: Long? = null,
        var genre: String? = null,
        var albumArtUri: String? = null,
        @Index var title: String? = null,
        var trackNumber: Long? = null,
        var numTracks: Long? = null,
        var dateAdded: String? = null,
        var songStampList: RealmList<SongStamp>? = null,
        var artist: Artist? = null
) : RealmObject(), Shareable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Song) return false

        val song = other as Song?

        if (if (artist != null) !artist!!.name.equals(song!!.artist!!.name) else song!!.artist != null)
            return false
        return if (title != null) title == song.title else song.title == null

    }

    override fun hashCode(): Int {
        var result = if (artist != null) artist!!.name!!.hashCode() else 0
        result = 31 * result + if (title != null) title!!.hashCode() else 0
        return result
    }

    fun parseMetadata(metadata: MediaMetadataCompat) {
        mediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
        trackSource = metadata.getString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE)
        album = fetchString(metadata, MediaMetadata.METADATA_KEY_ALBUM)
        val artistName = fetchString(metadata, MediaMetadata.METADATA_KEY_ARTIST)
        val artist = ArtistDao.getInstance().newStandalone()
        artist!!.name = artistName
        artist.albumArtUri = fetchString(metadata, MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
        this.artist = artist
        duration = fetchLong(metadata, MediaMetadata.METADATA_KEY_DURATION)
        genre = fetchString(metadata, MediaMetadata.METADATA_KEY_GENRE)
        albumArtUri = fetchString(metadata, MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
        title = fetchString(metadata, MediaMetadata.METADATA_KEY_TITLE)
        trackNumber = fetchLong(metadata, MediaMetadata.METADATA_KEY_TRACK_NUMBER)
        numTracks = fetchLong(metadata, MediaMetadata.METADATA_KEY_NUM_TRACKS)
        dateAdded = fetchString(metadata, MediaMetadata.METADATA_KEY_DATE)
    }

    fun mediaMetadataCompatBuilder(): MediaMetadataCompat.Builder {

        return MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, trackSource)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist!!.name)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration!!)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArtUri)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber!!)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, trackNumber!!)
                .putString(MediaMetadataCompat.METADATA_KEY_DATE, dateAdded)
    }

    fun buildMediaMetadataCompat(): MediaMetadataCompat {
        return mediaMetadataCompatBuilder().build()
    }

    internal fun fetchString(metadata: MediaMetadataCompat, key: String): String? {
        if (metadata.containsKey(key)) {
            return metadata.getString(key)
        }
        return null
    }

    internal fun fetchLong(metadata: MediaMetadataCompat, key: String): Long? {
        if (metadata.containsKey(key)) {
            return metadata.getLong(key)
        }
        return null
    }

    override fun share(resources: Resources): String {
        return resources.getString(R.string.share_song, title, artist!!.name)
    }
}