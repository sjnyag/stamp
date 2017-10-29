package com.sjn.stamp.model

import android.os.Build
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Device(
        @PrimaryKey var id: Long = 0,
        var model: String = "",
        var os: String = ""
) : RealmObject() {

    constructor() : this(model = OS_NAME, os = Build.MODEL)

    companion object {
        private val OS_NAME = "android"
    }
}
