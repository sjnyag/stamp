package com.sjn.stamp.db

import android.os.Build
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Device(
        @PrimaryKey var id: Long = 0,
        var model: String? = null,
        var os: String? = null
) : RealmObject() {

    fun configure() {
        os = OS_NAME
        model = Build.MODEL
    }

    companion object {
        private val OS_NAME = "android"
    }
}
