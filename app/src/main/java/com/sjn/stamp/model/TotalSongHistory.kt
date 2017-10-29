package com.sjn.stamp.model

import com.sjn.stamp.model.constant.RecordType
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey

open class TotalSongHistory(
        @PrimaryKey var id: Long = 0,
        @LinkingObjects("totalSongHistory") val song: RealmResults<Song>? = null,
        var playCount: Int = 0,
        private var skipCount: Int = 0,
        private var completeCount: Int = 0
) : RealmObject() {

    fun merge(src: TotalSongHistory): TotalSongHistory {
        incrementPlayCount(src.playCount)
        incrementSkipCount(src.skipCount)
        incrementCompleteCount(src.completeCount)
        return this
    }

    fun increment(recordType: RecordType): TotalSongHistory {
        when (recordType) {
            RecordType.PLAY -> incrementPlayCount(1)
            RecordType.SKIP -> incrementSkipCount(1)
            RecordType.COMPLETE -> incrementCompleteCount(1)
            else -> {
            }
        }
        return this
    }

    private fun incrementPlayCount(playCount: Int) {
        this.playCount += playCount
    }

    private fun incrementSkipCount(skipCount: Int) {
        this.skipCount += skipCount
    }

    private fun incrementCompleteCount(completeCount: Int) {
        this.completeCount += completeCount
    }
}
