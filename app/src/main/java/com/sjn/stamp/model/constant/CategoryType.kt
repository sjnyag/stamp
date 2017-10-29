package com.sjn.stamp.model.constant

import android.support.v4.media.MediaMetadataCompat

enum class CategoryType constructor(val databaseValue: String, val key: String) {
    ALBUM("album", MediaMetadataCompat.METADATA_KEY_ALBUM),
    ARTIST("artist", MediaMetadataCompat.METADATA_KEY_ARTIST),
    GENRE("genre", MediaMetadataCompat.METADATA_KEY_GENRE);

    companion object {
        fun of(databaseValue: String?): CategoryType? = CategoryType.values().firstOrNull { it.databaseValue == databaseValue }
    }
}