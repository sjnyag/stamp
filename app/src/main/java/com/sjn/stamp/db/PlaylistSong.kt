package com.sjn.stamp.db

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class PlaylistSong(
        @PrimaryKey var id: Long = 0,
        var artist: String? = null,
        var title: String? = null,
        var songId: String? = null,
        var active: Boolean? = null,
        var createdAt: Date? = null,
        var updatedAt: Date? = null,
        var playlist: Playlist? = null
) : RealmObject()