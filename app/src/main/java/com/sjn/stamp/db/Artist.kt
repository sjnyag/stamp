package com.sjn.stamp.db

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class Artist(
        @PrimaryKey var id: Long = 0,
        @Index var name: String? = null,
        var albumArtUri: String? = null
) : RealmObject() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Artist) return false

        val artist = other as Artist?

        return if (name != null) name == artist!!.name else artist!!.name == null

    }

    override fun hashCode(): Int = if (name != null) name!!.hashCode() else 0
}