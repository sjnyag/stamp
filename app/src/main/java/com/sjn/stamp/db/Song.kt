package com.sjn.stamp.db

import android.content.res.Resources
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.R
import com.sjn.stamp.utils.MediaItemHelper
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class Song(
        @PrimaryKey var id: Long = 0,
        @Index var mediaId: String = "",
        var trackSource: String = "",
        @Index var album: String = "",
        var duration: Long? = 0,
        var genre: String = "",
        var albumArtUri: String = "",
        @Index var title: String = "",
        var trackNumber: Long? = 0,
        var numTracks: Long? = 0,
        var dateAdded: String = "",
        var songStampList: RealmList<SongStamp> = RealmList(),
        var artist: Artist = Artist()
) : RealmObject(), Shareable {


    fun buildMediaMetadataCompat(): MediaMetadataCompat = MediaItemHelper.convertToMetadata(this)

    override fun share(resources: Resources): String =
            resources.getString(R.string.share_song, title, artist.name)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Song) return false

        if (title != other.title) return false
        if (artist != other.artist) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + artist.hashCode()
        return result
    }
}