package com.sjn.stamp.model

import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.Index
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey

open class Artist(
        @PrimaryKey var id: Long = 0,
        @Index var name: String = "",
        var albumArtUri: String = "",
        @LinkingObjects("artist") val song: RealmResults<Song>? = null
) : RealmObject() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Artist) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int = name.hashCode()
}