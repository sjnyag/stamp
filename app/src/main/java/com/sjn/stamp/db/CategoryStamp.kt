package com.sjn.stamp.db

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class CategoryStamp(
        @PrimaryKey var id: Long = 0,
        @Index var name: String = "",
        var isSystem: Boolean = false,
        var value: String = "",
        var type: String = ""
) : RealmObject()