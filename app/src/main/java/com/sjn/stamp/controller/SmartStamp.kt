package com.sjn.stamp.controller

import android.content.Context
import com.sjn.stamp.constant.RecordType
import com.sjn.stamp.db.Song
import com.sjn.stamp.db.dao.SongHistoryDao
import com.sjn.stamp.utils.RealmHelper

internal enum class SmartStamp(var mStampName: String) {
    HEAVY_ROTATION("Heavy Rotation") {
        override fun isTarget(context: Context, song: Song, playCount: Int, recordType: RecordType): Boolean {
            var result = false
            var counter = 0
            RealmHelper.getRealmInstance().use { realm ->
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
            return result
        }

        override fun register(context: Context, song: Song, playCount: Int, recordType: RecordType) {
            StampController(context).register(mStampName, true)
            SongController(context).registerStamp(song.id, mStampName, true)
        }
    },
    ARTIST_BEST("Artist Best") {
        override fun isTarget(context: Context, song: Song, playCount: Int, recordType: RecordType): Boolean =
                playCount >= 10

        override fun register(context: Context, song: Song, playCount: Int, recordType: RecordType) {
            val stampController = StampController(context)
            val songController = SongController(context)
            StampController(context).register(mStampName, true)
            RealmHelper.getRealmInstance().use { r ->
                stampController.delete(r, mStampName, true)
                val artistList = SongHistoryController(context).getRankedArtistList(r, null, null, null)
                artistList.forEach { artist ->
                    artist.orderedSongList().filter { it.playCount >= 10 }.take(3).forEach { rankedSong ->
                        songController.registerStamp(rankedSong.song.id, mStampName, true)
                    }
                }
                stampController.notifyStampChange()
            }
        }
    },
    BREAK_SONG("Break Song") {
        override fun isTarget(context: Context, song: Song, playCount: Int, recordType: RecordType): Boolean =
                false

        override fun register(context: Context, song: Song, playCount: Int, recordType: RecordType) {

        }
    };

    internal abstract fun isTarget(context: Context, song: Song, playCount: Int, recordType: RecordType): Boolean

    abstract fun register(context: Context, song: Song, playCount: Int, recordType: RecordType)
}