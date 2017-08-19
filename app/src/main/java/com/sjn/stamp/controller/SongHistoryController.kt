package com.sjn.stamp.controller

import android.content.Context
import android.os.AsyncTask
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.constant.RecordType
import com.sjn.stamp.db.*
import com.sjn.stamp.db.dao.DeviceDao
import com.sjn.stamp.db.dao.SongDao
import com.sjn.stamp.db.dao.SongHistoryDao
import com.sjn.stamp.db.dao.TotalSongHistoryDao
import com.sjn.stamp.ui.custom.PeriodSelectLayout
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaItemHelper
import com.sjn.stamp.utils.NotificationHelper
import com.sjn.stamp.utils.RealmHelper
import io.realm.Realm
import java.util.*

class SongHistoryController(private val mContext: Context) {

    val topSongList: List<MediaMetadataCompat>
        get() {
            return RealmHelper.getRealmInstance().use { realm ->
                val songList = ArrayList<MediaMetadataCompat>()
                TotalSongHistoryDao.getOrderedList(realm)
                        .takeWhile { !(it.playCount == 0 || songList.size >= UserSettingController().mostPlayedSongSize) }
                        .forEach { songList.add(MediaItemHelper.convertToMetadata(it.song)) }
                songList
            }
        }

    fun onPlay(song: MediaMetadataCompat, date: Date) {
        LogHelper.d(TAG, "insertPLAY ", song.description.title)
        register(song, RecordType.PLAY, date)
    }

    fun onSkip(song: MediaMetadataCompat, date: Date) {
        LogHelper.d(TAG, "insertSKIP ", song.description.title)
        register(song, RecordType.SKIP, date)
    }

    fun onStart(song: MediaMetadataCompat, date: Date) {
        LogHelper.d(TAG, "insertSTART ", song.description.title)
        register(song, RecordType.START, date)
    }

    fun onComplete(song: MediaMetadataCompat, date: Date) {
        LogHelper.d(TAG, "insertComplete ", song.description.title)
        register(song, RecordType.COMPLETE, date)
    }

    fun delete(songHistoryId: Long) {
        RealmHelper.getRealmInstance().use { realm ->
            SongHistoryDao.remove(realm, songHistoryId)
        }
    }

    fun getManagedTimeline(realm: Realm): List<SongHistory> = SongHistoryDao.timeline(realm, RecordType.PLAY.databaseValue)

    fun getRankedSongList(realm: Realm, period: PeriodSelectLayout.Period): List<RankedSong> = getRankedSongList(realm, period.from(), period.to(), 30)

    fun getRankedArtistList(realm: Realm, period: PeriodSelectLayout.Period): List<RankedArtist> = getRankedArtistList(realm, period.from(), period.to(), 30)

    private fun register(metadata: MediaMetadataCompat, recordType: RecordType, date: Date) {
        val song = createSong(metadata)
        RealmHelper.getRealmInstance().use { realm ->
            val playCount = TotalSongHistoryDao.saveOrIncrement(realm, createTotalSongHistory(song, recordType))
            SongHistoryDao.save(realm, createSongHistory(song, createDevice(), recordType, date, playCount))
            if (recordType === RecordType.PLAY) {
                sendNotificationBySongCount(realm, song, playCount)
                sendNotificationByArtistCount(song)
            }
            SmartStampController(mContext).calculateAsync(createSong(metadata), playCount, recordType)
        }
    }

    private fun sendNotificationBySongCount(realm: Realm, song: Song, playCount: Int) {
        if (NotificationHelper.isSendPlayedNotification(playCount)) {
            NotificationHelper.sendPlayedNotification(mContext, song.title, song.albumArtUri, playCount, SongHistoryDao.findOldest(realm, song).recordedAt)
        }
    }

    private fun sendNotificationByArtistCount(song: Song) {
        ArtistCountAsyncTask(song.artist.name).execute()
    }

    private fun createTotalSongHistory(song: Song, recordType: RecordType): TotalSongHistory = TotalSongHistoryDao.newStandalone().applySongQueue(song, recordType)

    private fun createDevice(): Device = DeviceDao.newStandalone()

    private fun createSong(song: MediaMetadataCompat): Song = SongDao.newStandalone(song)

    private fun createSongHistory(song: Song, device: Device, recordType: RecordType, date: Date, count: Int): SongHistory = SongHistoryDao.newStandalone().applyValues(song, recordType, device, date, count)

    private fun getRankedSongList(realm: Realm, from: Date?, to: Date?, count: Int): List<RankedSong> {
        LogHelper.d(TAG, "getRankedSongList start")
        LogHelper.d(TAG, "calc historyList")
        val songCountMap = HashMap<Song, Int>()
        LogHelper.d(TAG, "put songCountMap")
        for (songHistory in SongHistoryDao.where(realm, from, to, RecordType.PLAY.databaseValue)) {
            if (songCountMap.containsKey(songHistory.song)) {
                songCountMap.put(songHistory.song, songCountMap[songHistory.song]!! + 1)
            } else {
                songCountMap.put(songHistory.song, 1)
            }
        }
        LogHelper.d(TAG, "create rankedSongList")
        var rankedSongList: MutableList<RankedSong> = ArrayList()
        for ((key, value) in songCountMap) {
            rankedSongList.add(RankedSong(value, key))
        }
        LogHelper.d(TAG, "sort rankedSongList")
        Collections.sort(rankedSongList) { t1, t2 -> t2.playCount - t1.playCount }
        if (rankedSongList.size > count) {
            rankedSongList = rankedSongList.subList(0, count)
        }
        LogHelper.d(TAG, "getRankedSongList end")
        return rankedSongList
    }

    private fun getRankedArtistList(realm: Realm, from: Date?, to: Date?, count: Int): List<RankedArtist> {
        LogHelper.d(TAG, "getRankedArtistList start")
        val historyList = SongHistoryDao.where(realm, from, to, RecordType.PLAY.databaseValue)
        val artistMap = HashMap<Artist, ArtistCounter>()
        for (songHistory in historyList) {
            ArtistCounter.count(artistMap, songHistory.song.artist, songHistory.song)
        }
        var rankedArtistList: MutableList<RankedArtist> = ArrayList()
        for ((key, value) in artistMap) {
            rankedArtistList.add(RankedArtist(value.mCount, key, value.mSongCountMap))
        }
        Collections.sort(rankedArtistList) { t1, t2 -> t2.playCount - t1.playCount }
        if (rankedArtistList.size > count) {
            rankedArtistList = rankedArtistList.subList(0, count)
        }
        LogHelper.d(TAG, "getRankedArtistList end")
        return rankedArtistList
    }

    private class ArtistCounter {
        internal var mCount = 0
        internal var mSongCountMap: MutableMap<Song, Int> = HashMap()

        internal fun increment(song: Song) {
            mCount++
            if (mSongCountMap.containsKey(song)) {
                mSongCountMap.put(song, mSongCountMap[song]!! + 1)
            } else {
                mSongCountMap.put(song, 1)
            }
        }

        companion object {

            fun count(artistMap: MutableMap<Artist, ArtistCounter>, artist: Artist, song: Song) {
                val counter = if (artistMap.containsKey(artist)) artistMap[artist] else ArtistCounter()
                counter?.let { artistCounter ->
                    artistCounter.increment(song)
                    artistMap.put(artist, artistCounter)
                }

            }
        }
    }

    private inner class ArtistCountAsyncTask internal constructor(internal var mArtistName: String) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
            RealmHelper.getRealmInstance().use { realm ->
                val historyList = SongHistoryDao.findPlayRecordByArtist(realm, mArtistName)
                if (!NotificationHelper.isSendPlayedNotification(historyList.size)) {
                    return null
                }
                val oldestSongHistory = SongHistoryDao.findOldestByArtist(realm, mArtistName)
                NotificationHelper.sendPlayedNotification(mContext, mArtistName, oldestSongHistory.song.albumArtUri, historyList.size, oldestSongHistory.recordedAt)
            }
            return null
        }
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(SongHistoryController::class.java)
    }

}
