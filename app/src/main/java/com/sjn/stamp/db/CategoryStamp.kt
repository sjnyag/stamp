package com.sjn.stamp.db

import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.constant.CategoryType
import com.sjn.stamp.utils.MediaItemHelper
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class CategoryStamp(
        @PrimaryKey var id: Long = 0,
        @Index var name: String = "",
        var isSystem: Boolean = false,
        var value: String = "",
        var type: String = ""
) : RealmObject() {
    fun contain(mediaMetadata: MediaMetadataCompat): Boolean =
            MediaItemHelper.fetch(mediaMetadata, CategoryType.of(type)) == value
}