package com.sjn.stamp.db

import android.content.res.Resources
import android.media.MediaMetadata
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.R
import com.sjn.stamp.db.dao.ArtistDao
import com.sjn.stamp.utils.MediaItemHelper
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

    fun buildMediaMetadataCompat(): MediaMetadataCompat {
        return MediaItemHelper.convertToMetadata(this)
    }
    override fun share(resources: Resources): String {
        return resources.getString(R.string.share_song, title, artist!!.name)
    }
}