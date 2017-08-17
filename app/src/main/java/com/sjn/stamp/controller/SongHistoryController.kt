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

    fun deleteSongHistory(songHistoryId: Long) {
        val realm = RealmHelper.getRealmInstance()
        SongHistoryDao.remove(realm, songHistoryId)
        realm.close()
    }

    fun onPlay(track: MediaMetadataCompat, date: Date) {
        LogHelper.d(TAG, "insertPLAY ", track.description.title)
        registerHistory(track, RecordType.PLAY, date)
    }

    fun onSkip(track: MediaMetadataCompat, date: Date) {
        LogHelper.d(TAG, "insertSKIP ", track.description.title)
        registerHistory(track, RecordType.SKIP, date)
    }

    fun onStart(track: MediaMetadataCompat, date: Date) {
        LogHelper.d(TAG, "insertSTART ", track.description.title)
        registerHistory(track, RecordType.START, date)
    }

    fun onComplete(track: MediaMetadataCompat, date: Date) {
        LogHelper.d(TAG, "insertComplete ", track.description.title)
        registerHistory(track, RecordType.COMPLETE, date)
    }

    private fun registerHistory(track: MediaMetadataCompat, recordType: RecordType, date: Date) {
        val song = createSong(track)
        val realm = RealmHelper.getRealmInstance()
        val playCount = TotalSongHistoryDao.saveOrIncrement(realm, createTotalSongHistory(song, recordType))
        SongHistoryDao.save(realm, createSongHistory(song, createDevice(), recordType, date, playCount))
        if (recordType === RecordType.PLAY) {
            sendNotificationBySongCount(realm, song, playCount)
            sendNotificationByArtistCount(song)
        }
        SmartStampController(mContext).calculateAsync(createSong(track), playCount, recordType)
        realm.close()
    }

    private fun sendNotificationBySongCount(realm: Realm, song: Song, playCount: Int) {
        if (NotificationHelper.isSendPlayedNotification(playCount)) {
            val oldestSongHistory = SongHistoryDao.findOldest(realm, song)
            NotificationHelper.sendPlayedNotification(mContext, song.title, song.albumArtUri, playCount, oldestSongHistory.recordedAt)
        }
    }

    private fun sendNotificationByArtistCount(song: Song) {
        song.artist?.let { artist ->
            ArtistCountAsyncTask(artist.name).execute()
        }
    }

    private fun createTotalSongHistory(song: Song, recordType: RecordType): TotalSongHistory {
        val totalSongHistory = TotalSongHistoryDao.newStandalone()
        totalSongHistory.parseSongQueue(song, recordType)
        return totalSongHistory
    }

    private fun createDevice(): Device {
        val device = DeviceDao.newStandalone()
        device.configure()
        return device
    }

    private fun createSong(track: MediaMetadataCompat): Song {
        val song = SongDao.newStandalone()
        MediaItemHelper.updateSong(song, track)
        return song
    }

    private fun createSongHistory(song: Song, device: Device, recordType: RecordType, date: Date, count: Int): SongHistory {
        val songHistory = SongHistoryDao.newStandalone()
        songHistory.applyValues(song, recordType, device, date, count)
        return songHistory
    }

    val topSongList: List<MediaMetadataCompat>
        get() {
            val realm = RealmHelper.getRealmInstance()
            val trackList = ArrayList<MediaMetadataCompat>()
            val historyList = TotalSongHistoryDao.getOrderedList(realm)
            historyList
                    .takeWhile { !(it.playCount == 0 || trackList.size >= UserSettingController().mostPlayedSongSize) }
                    .forEach { trackList.add(MediaItemHelper.convertToMetadata(it.song)) }
            realm.close()
            return trackList
        }

    fun getManagedTimeline(realm: Realm): List<SongHistory> =
            SongHistoryDao.timeline(realm, RecordType.PLAY.value)

    fun getRankedSongList(realm: Realm, period: PeriodSelectLayout.Period): List<RankedSong> {
        var from: Date? = if (period.from() == null) null else period.from().toDateTimeAtStartOfDay().toDate()
        var to: Date? = if (period.to() == null) null else period.to().toDateTimeAtStartOfDay().plusDays(1).toDate()
        if (period.periodType == PeriodSelectLayout.PeriodType.TOTAL) {
            from = null
            to = null
        }
        return getRankedSongList(realm, from, to)
    }

    fun getRankedArtistList(realm: Realm, period: PeriodSelectLayout.Period): List<RankedArtist> {
        var from: Date? = if (period.from() == null) null else period.from().toDateTimeAtStartOfDay().toDate()
        var to: Date? = if (period.to() == null) null else period.to().toDateTimeAtStartOfDay().plusDays(1).toDate()
        if (period.periodType == PeriodSelectLayout.PeriodType.TOTAL) {
            from = null
            to = null
        }
        return getRankedArtistList(realm, from, to)
    }

    private fun getRankedSongList(realm: Realm, from: Date?, to: Date?): List<RankedSong> {
        LogHelper.d(TAG, "getRankedSongList start")
        LogHelper.d(TAG, "calc historyList")
        val historyList = SongHistoryDao.where(realm, from, to, RecordType.PLAY.value)
        val songCountMap = HashMap<Song, Int>()
        LogHelper.d(TAG, "put songCountMap")
        for (songHistory in historyList) {
            songHistory.song?.let { song ->
                if (songCountMap.containsKey(song)) {
                    songCountMap.put(song, songCountMap[song]!! + 1)
                } else {
                    songCountMap.put(song, 1)
                }
            }
        }
        LogHelper.d(TAG, "create rankedSongList")
        var rankedSongList: MutableList<RankedSong> = ArrayList()
        for ((key, value) in songCountMap) {
            rankedSongList.add(RankedSong(value, key))
        }
        LogHelper.d(TAG, "sort rankedSongList")
        Collections.sort(rankedSongList) { t1, t2 -> t2.playCount - t1.playCount }
        if (rankedSongList.size > 30) {
            rankedSongList = rankedSongList.subList(0, 30)
        }
        LogHelper.d(TAG, "getRankedSongList end")
        return rankedSongList
    }

    private fun getRankedArtistList(realm: Realm, from: Date?, to: Date?): List<RankedArtist> {
        LogHelper.d(TAG, "getRankedArtistList start")
        val historyList = SongHistoryDao.where(realm, from, to, RecordType.PLAY.value)
        val artistMap = HashMap<Artist, ArtistCounter>()
        for (songHistory in historyList) {
            ArtistCounter.count(artistMap, songHistory.song?.artist!!, songHistory.song!!)
        }
        var rankedArtistList: MutableList<RankedArtist> = ArrayList()
        for ((key, value) in artistMap) {
            rankedArtistList.add(RankedArtist(value.mCount, key, value.mSongCountMap))
        }
        Collections.sort(rankedArtistList) { t1, t2 -> t2.playCount - t1.playCount }
        if (rankedArtistList.size > 30) {
            rankedArtistList = rankedArtistList.subList(0, 30)
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

    private inner class ArtistCountAsyncTask internal constructor(internal var mArtistName: String?) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
            var realm: Realm? = null
            try {
                realm = RealmHelper.getRealmInstance()

                val historyList = SongHistoryDao.findPlayRecordByArtist(realm, mArtistName!!)
                val playCount = historyList.size
                if (NotificationHelper.isSendPlayedNotification(playCount)) {
                    val oldestSongHistory = SongHistoryDao.findOldestByArtist(realm, mArtistName!!)
                    val song = oldestSongHistory.song
                    if (song != null) {
                        NotificationHelper.sendPlayedNotification(
                                mContext,
                                mArtistName,
                                song.albumArtUri,
                                playCount,
                                oldestSongHistory.recordedAt
                        )
                    }
                }
            } finally {
                if (realm != null) {
                    realm.close()
                }
            }
            return null
        }
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(SongHistoryController::class.java)
    }

}
