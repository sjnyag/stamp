package com.sjn.stamp.controller

import android.content.Context
import com.sjn.stamp.constant.RecordType
import com.sjn.stamp.db.Song
import com.sjn.stamp.db.dao.SongHistoryDao
import com.sjn.stamp.utils.RealmHelper

internal class SmartStampController(private val mContext: Context) {
    internal enum class SmartStamp(var mStamp: String) {
        HEAVY_ROTATION("Heavy Rotation") {
            override fun isTarget(context: Context, song: Song, playCount: Int, recordType: RecordType): Boolean {
                var result = false
                var counter = 0
                RealmHelper.getRealmInstance().use { realm ->
                    val songHistoryList = SongHistoryDao.findAll(realm, RecordType.PLAY.databaseValue)
                    for (songHistory in songHistoryList) {
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
                registerStamp(mStamp, context)
                val songController = SongController(context)
                songController.registerStamp(mStamp, song, true)
            }
        },
        ARTIST_BEST("Artist Best") {
            override fun isTarget(context: Context, song: Song, playCount: Int, recordType: RecordType): Boolean =
                    true

            override fun register(context: Context, song: Song, playCount: Int, recordType: RecordType) {
                val stampController = StampController(context)
                val songController = SongController(context)
                registerStamp(mStamp, context)
                RealmHelper.getRealmInstance().use { realm ->
                    realm.executeTransaction { r ->
                        stampController.removeWithoutTransaction(r, mStamp, true)
                        val artistList = SongHistoryController(context).getRankedArtistList(r, null, null, null)
                        artistList.forEach { artist ->
                            artist.orderedSongList().filter { it.playCount >= 10 }.take(3).forEach { rankedSong ->
                                songController.registerStampWithoutTransaction(realm, mStamp, rankedSong.song, true)
                            }
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
                registerStamp(mStamp, context)

            }
        };

        internal abstract fun isTarget(context: Context, song: Song, playCount: Int, recordType: RecordType): Boolean

        abstract fun register(context: Context, song: Song, playCount: Int, recordType: RecordType)

        internal fun registerStamp(stamp: String, context: Context) {
            val stampController = StampController(context)
            stampController.register(stamp, true)
        }
    }

    fun calculateAsync(song: Song, playCount: Int, recordType: RecordType) {
        Thread(Runnable { calculate(song, playCount, recordType) }).start()
    }

    private fun calculate(song: Song, playCount: Int, recordType: RecordType) {
        SmartStamp.values()
                .filter { it.isTarget(mContext, song, playCount, recordType) }
                .forEach { it.register(mContext, song, playCount, recordType) }
    }
}