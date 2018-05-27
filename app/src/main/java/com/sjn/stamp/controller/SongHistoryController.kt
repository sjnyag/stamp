package com.sjn.stamp.controller

import android.content.Context
import android.os.AsyncTask
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.model.*
import com.sjn.stamp.model.constant.RecordType
import com.sjn.stamp.model.dao.SongDao
import com.sjn.stamp.model.dao.SongHistoryDao
import com.sjn.stamp.model.dao.TotalSongHistoryDao
import com.sjn.stamp.ui.custom.PeriodSelectLayout
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaIDHelper.resolveMusicId
import com.sjn.stamp.utils.MediaItemHelper
import com.sjn.stamp.utils.NotificationHelper
import com.sjn.stamp.utils.RealmHelper
import io.realm.Realm
import java.lang.ref.WeakReference
import java.util.*

class SongHistoryController(private val context: Context) {

    val topSongList: List<MediaMetadataCompat>
        get() =
             RealmHelper.realmInstance.use { realm ->
                val songList = ArrayList<MediaMetadataCompat>()
                TotalSongHistoryDao.findPlayed(realm).forEach loop@ { songHistory ->
                    if (songHistory.playCount == 0 || songList.size >= UserSettingController().mostPlayedSongSize) {
                        return@loop
                    }
                    songHistory.song?.let {
                        if (it.isNotEmpty()) {
                            songList.add(MediaItemHelper.convertToMetadata(songHistory.song.first()))
                        }
                    }
                }
                songList
            }

    val hasHistory: Boolean
        get() =
            RealmHelper.realmInstance.use { realm ->
                SongHistoryDao.isExists(realm)
            }

    fun onPlay(mediaId: String, date: Date) {
        LogHelper.d(TAG, "insertPLAY ", mediaId)
        register(mediaId, RecordType.PLAY, date)
    }

    fun onSkip(mediaId: String, date: Date) {
        LogHelper.d(TAG, "insertSKIP ", mediaId)
        register(mediaId, RecordType.SKIP, date)
    }

    fun onStart(mediaId: String, date: Date) {
        LogHelper.d(TAG, "insertSTART ", mediaId)
        register(mediaId, RecordType.START, date)
    }

    fun onComplete(mediaId: String, date: Date) {
        LogHelper.d(TAG, "insertComplete ", mediaId)
        register(mediaId, RecordType.COMPLETE, date)
    }

    fun delete(songHistoryId: Long) {
        RealmHelper.realmInstance.use { realm ->
            SongHistoryDao.delete(realm, songHistoryId)
        }
    }

    fun getManagedTimeline(realm: Realm): List<SongHistory> = SongHistoryDao.timeline(realm, RecordType.PLAY.databaseValue)

    fun getRankedSongList(realm: Realm, period: PeriodSelectLayout.Period): List<RankedSong> = getRankedSongList(realm, period.from(), period.to(), 30)

    fun getRankedArtistList(realm: Realm, period: PeriodSelectLayout.Period): List<RankedArtist> = getRankedArtistList(realm, period.from(), period.to(), 30)

    private fun register(mediaId: String, recordType: RecordType, date: Date) {
        val musicId = resolveMusicId(mediaId) ?: return
        RealmHelper.realmInstance.use { realm ->
            val song = SongDao.findOrCreateByMusicId(realm, musicId, context)
            song ?: return
            val playCount = TotalSongHistoryDao.increment(realm, song.id, recordType)
            SongHistoryDao.create(realm, song, recordType, date, playCount)
            if (recordType === RecordType.PLAY) {
                sendNotificationBySongCount(realm, song, playCount)
                sendNotificationByArtistCount(song)
            }
            SongController(context).calculateSmartStamp(song.id, playCount, recordType)
        }
    }

    private fun sendNotificationBySongCount(realm: Realm, song: Song, playCount: Int) {
        if (NotificationHelper.isSendPlayedNotification(playCount)) {
            NotificationHelper.sendPlayedNotification(context, song, song.albumArtUri, playCount, SongHistoryDao.findOldest(realm, song.id)!!.recordedAt)
        }
    }

    private fun sendNotificationByArtistCount(song: Song) {
        ArtistCountAsyncTask(context, song.artist.name).execute()
    }

    private fun getRankedSongList(realm: Realm, from: Date?, to: Date?, count: Int?): List<RankedSong> {
        LogHelper.d(TAG, "getRankedSongList start")
        LogHelper.d(TAG, "calc historyList")
        val songCountMap = HashMap<Song, Int>()
        LogHelper.d(TAG, "put songCountMap")
        for (songHistory in SongHistoryDao.where(realm, from, to, RecordType.PLAY.databaseValue)) {
            if (songCountMap.containsKey(songHistory.song)) {
                songCountMap[songHistory.song] = songCountMap[songHistory.song]!! + 1
            } else {
                songCountMap[songHistory.song] = 1
            }
        }
        LogHelper.d(TAG, "create rankedSongList")
        var rankedSongList: MutableList<RankedSong> = ArrayList()
        for ((key, value) in songCountMap) {
            rankedSongList.add(RankedSong(value, key))
        }
        LogHelper.d(TAG, "sort rankedSongList")
        rankedSongList.sortWith(Comparator { t1, t2 -> t2.playCount - t1.playCount })
        count?.let {
            if (rankedSongList.size > count) {
                rankedSongList = rankedSongList.subList(0, count)
            }
        }
        LogHelper.d(TAG, "getRankedSongList end")
        return rankedSongList
    }

    fun getRankedArtistList(realm: Realm, from: Date?, to: Date?, count: Int?): List<RankedArtist> {
        LogHelper.d(TAG, "getRankedArtistList start")
        val historyList = SongHistoryDao.where(realm, from, to, RecordType.PLAY.databaseValue)
        val artistMap = HashMap<Artist, ArtistCounter>()
        for (songHistory in historyList) {
            ArtistCounter.count(artistMap, songHistory.song.artist, songHistory.song)
        }
        var rankedArtistList: MutableList<RankedArtist> = ArrayList()
        for ((key, value) in artistMap) {
            rankedArtistList.add(RankedArtist(value.count, key, value.songCountMap))
        }
        rankedArtistList.sortWith(Comparator { t1, t2 -> t2.playCount - t1.playCount })
        count?.let {
            if (rankedArtistList.size > count) {
                rankedArtistList = rankedArtistList.subList(0, count)
            }
        }
        LogHelper.d(TAG, "getRankedArtistList end")
        return rankedArtistList
    }

    private class ArtistCounter {
        internal var count = 0
        internal var songCountMap: MutableMap<Song, Int> = HashMap()

        internal fun increment(song: Song) {
            count++
            if (songCountMap.containsKey(song)) {
                songCountMap[song] = songCountMap[song]!! + 1
            } else {
                songCountMap[song] = 1
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

    companion object {

        private val TAG = LogHelper.makeLogTag(SongHistoryController::class.java)

        private class ArtistCountAsyncTask constructor(context: Context, internal var artistName: String) : AsyncTask<Void, Void, Void>() {

            val context = WeakReference(context)
            override fun doInBackground(vararg params: Void): Void? {
                RealmHelper.realmInstance.use { realm ->
                    val historyList = SongHistoryDao.findPlayRecordByArtist(realm, artistName)
                    if (!NotificationHelper.isSendPlayedNotification(historyList.size)) {
                        return null
                    }
                    val oldestSongHistory = SongHistoryDao.findOldestByArtist(realm, artistName)
                    context.get()?.let {
                        NotificationHelper.sendPlayedNotification(it, artistName, oldestSongHistory!!.song.albumArtUri, historyList.size, oldestSongHistory.recordedAt)
                    }
                }
                return null
            }
        }
    }

}
