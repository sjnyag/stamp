package com.sjn.stamp.db

import com.sjn.stamp.constant.RecordType
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class TotalSongHistory(
        @PrimaryKey
        var id: Long = 0,
        var song: Song = Song(),
        var playCount: Int = 0,
        var skipCount: Int = 0,
        var completeCount: Int = 0
) : RealmObject() {

    fun updatePlayCountIfOver(playCount: Int) {
        if (playCount <= this.playCount) {
            return
        }
        this.playCount = playCount
    }

    fun updateSkipCountIfOver(skipCount: Int) {
        if (skipCount <= this.skipCount) {
            return
        }
        this.skipCount = skipCount
    }

    fun updateCompleteCountIfOver(completeCount: Int) {
        if (completeCount <= this.completeCount) {
            return
        }
        this.completeCount = completeCount
    }

    fun incrementPlayCount(playCount: Int) {
        this.playCount += playCount
    }

    fun incrementSkipCount(skipCount: Int) {
        this.skipCount += skipCount
    }

    fun incrementCompleteCount(completeCount: Int) {
        this.completeCount += completeCount
    }

    fun applySongQueue(song: Song, recordType: RecordType): TotalSongHistory {
        when (recordType) {
            RecordType.PLAY -> applyValues(song, 1, 0, 0)
            RecordType.SKIP -> applyValues(song, 0, 1, 0)
            RecordType.COMPLETE -> applyValues(song, 0, 0, 1)
            else -> applyValues(song, 0, 0, 0)
        }
        return this
    }

    private fun applyValues(song: Song, playCount: Int, skipCount: Int, completeCount: Int) {
        this.song = song
        this.playCount = playCount
        this.skipCount = skipCount
        this.completeCount = completeCount
    }
}
