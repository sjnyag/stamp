package com.sjn.stamp.controller

import android.content.Context
import com.sjn.stamp.model.constant.RecordType
import com.sjn.stamp.model.dao.SongDao
import com.sjn.stamp.model.dao.SongHistoryDao
import com.sjn.stamp.utils.RealmHelper

internal enum class SmartStamp(var stampName: String) {
    HEAVY_ROTATION("Heavy Rotation") {
        override fun isTarget(context: Context, songId: Long, playCount: Int, recordType: RecordType): Boolean {
            var result = false
            var counter = 0
            RealmHelper.realmInstance.use { realm ->
                SongDao.findById(realm, songId)?.let { song ->
                    for (songHistory in SongHistoryDao.findAll(realm, RecordType.PLAY.databaseValue)) {
                        if (songHistory.song != song) {
                            break
                        }
                        counter++
                        if (counter >= 10) {
                            result = true
                            break
                        }
                    }
                }
            }
            return result
        }

        override fun register(context: Context, songId: Long, playCount: Int, recordType: RecordType) {
            StampController(context).register(stampName, true)
            SongController(context).registerStamp(songId, stampName, true)
        }
    },
    ARTIST_BEST("Artist Best") {
        override fun isTarget(context: Context, songId: Long, playCount: Int, recordType: RecordType): Boolean =
                playCount >= 10

        override fun register(context: Context, songId: Long, playCount: Int, recordType: RecordType) {
            StampController(context).run {
                register(stampName, true)
                RealmHelper.realmInstance.use { r ->
                    delete(r, stampName, true)
                    SongHistoryController(context).getRankedArtistList(r, null, null, null).forEach { artist ->
                        artist.orderedSongList().filter { it.playCount >= 10 }.take(3).forEach { rankedSong ->
                            SongController(context).registerStamp(rankedSong.song.id, stampName, true)
                        }
                    }
                    notifyStampChange()
                }

            }
        }
    },
    BREAK_SONG("Break Song") {
        override fun isTarget(context: Context, songId: Long, playCount: Int, recordType: RecordType): Boolean = false

        override fun register(context: Context, songId: Long, playCount: Int, recordType: RecordType) {}
    };

    internal abstract fun isTarget(context: Context, songId: Long, playCount: Int, recordType: RecordType): Boolean

    abstract fun register(context: Context, songId: Long, playCount: Int, recordType: RecordType)
}