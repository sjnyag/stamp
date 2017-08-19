package com.sjn.stamp.db

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class SongStamp(
        @PrimaryKey var id: Long = 0,
        @Index var name: String = "",
        var isSystem: Boolean = false,
        var songList: RealmList<Song> = RealmList()
) : RealmObject()