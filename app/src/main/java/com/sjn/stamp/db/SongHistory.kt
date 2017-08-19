package com.sjn.stamp.db

import android.content.res.Resources
import com.sjn.stamp.R
import com.sjn.stamp.constant.RecordType
import com.sjn.stamp.utils.TimeHelper
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import java.util.*

@Suppress("unused")
open class SongHistory(
        @PrimaryKey var id: Long = 0,
        var song: Song = Song(),
        var recordedAt: Date = Date(),
        @Index private var recordType: String = "",
        var device: Device = Device(),
        var count: Int = 0,
        var latitude: Float = 0.toFloat(),
        var longitude: Float = 0.toFloat(),
        var accuracy: Float = 0.toFloat(),
        var altitude: Float = 0.toFloat()
) : RealmObject() {

    fun applyValues(song: Song, recordType: RecordType, device: Device, date: Date, count: Int): SongHistory {
        this.song = song
        this.recordType = recordType.databaseValue
        this.device = device
        this.recordedAt = date
        this.count = count
        return this
    }

    fun toLabel(resources: Resources): String =
            resources.getString(R.string.song_history_label, song.title, song.artist.name, TimeHelper.formatYYYYMMDDHHMMSS(recordedAt))
}
