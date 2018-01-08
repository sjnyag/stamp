package com.sjn.stamp.controller

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.media.provider.ProviderType
import com.sjn.stamp.model.Song
import com.sjn.stamp.model.Stamp
import com.sjn.stamp.model.constant.CategoryType
import com.sjn.stamp.model.constant.RecordType
import com.sjn.stamp.model.dao.CategoryStampDao
import com.sjn.stamp.model.dao.SongDao
import com.sjn.stamp.model.dao.SongStampDao
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.MediaRetrieveHelper
import com.sjn.stamp.utils.RealmHelper
import io.realm.Realm

class SongController(private val mContext: Context) {

    fun findSong(songId: Long): Song? {
        return RealmHelper.getRealmInstance().use { realm ->
            SongDao.findById(realm, songId)
        }
    }

    fun mergeSong(unknownSong: Song, mediaMetadata: MediaMetadataCompat): Boolean {
        RealmHelper.getRealmInstance().use { realm ->
            SongDao.findByMediaMetadata(realm, mediaMetadata)?.let {
                return SongDao.merge(realm, unknownSong.id, it.id)
            }
        }
        return false
    }

    fun calculateSmartStamp(rawSong: Song, playCount: Int, recordType: RecordType) {
        val song = RealmHelper.getRealmInstance().copyFromRealm(rawSong)
        Thread(Runnable {
            SmartStamp.values()
                    .filter { it.isTarget(mContext, song, playCount, recordType) }
                    .forEach { it.register(mContext, song, playCount, recordType) }
        }).start()
    }

    fun refreshAllSongs(musicList: Collection<MediaMetadataCompat>) {
        RealmHelper.getRealmInstance().use { realm ->
            SongDao.findAll(realm).forEach {
                SongDao.loadLocalMedia(realm, it.id, musicList)
            }
        }
    }

    fun findStampsByMediaId(mediaId: String): List<Stamp> {
        val mediaMetadata = resolveMediaMetadata(mediaId)
        return if (MediaIDHelper.isTrack(mediaId) && mediaMetadata != null) {
            findSongStampListByMediaMetadata(mediaMetadata)
        } else {
            val categoryType = resolveCategory(mediaId)
            if (categoryType == null) {
                ArrayList()
            } else {
                findCategoryStampList(categoryType.first, categoryType.second)
            }
        }
    }

    fun registerStamp(songId: Long, stampName: String, isSystem: Boolean) {
        RealmHelper.getRealmInstance().use { realm ->
            SongStampDao.register(realm, songId, stampName, isSystem)
        }
    }

    fun registerStampList(stampNameList: List<String>, mediaId: String, isSystem: Boolean) {
        val mediaMetadata = resolveMediaMetadata(mediaId)
        if (MediaIDHelper.isTrack(mediaId) && mediaMetadata != null) {
            registerSongStampList(stampNameList, mediaMetadata, isSystem)
        } else {
            val categoryType = resolveCategory(mediaId)
            if (categoryType == null) {
                return
            } else {
                registerCategoryStampList(stampNameList, categoryType.first, categoryType.second, isSystem)
            }
        }
        StampController(mContext).notifyStampChange()
    }

    fun removeStamp(stampName: String, mediaId: String, isSystem: Boolean) {
        val mediaMetadata = resolveMediaMetadata(mediaId)
        if (MediaIDHelper.isTrack(mediaId) && mediaMetadata != null) {
            removeSongStamp(mediaMetadata, stampName, isSystem)
        } else {
            val categoryType = resolveCategory(mediaId)
            if (categoryType == null) {
                return
            } else {
                removeCategoryStamp(stampName, categoryType.first, categoryType.second, isSystem)
            }
        }
        StampController(mContext).notifyStampChange()
    }

    private fun findSongStampListByMediaMetadata(mediaMetadata: MediaMetadataCompat): List<Stamp> {
        val stampList = ArrayList<Stamp>()
        RealmHelper.getRealmInstance().use { realm: Realm ->
            SongDao.findByMediaMetadata(realm, mediaMetadata)?.let { song ->
                for (songStamp in song.songStampList) {
                    songStamp.let { s -> stampList.add(Stamp(s.name, s.isSystem)) }
                }
            }
        }
        return stampList
    }

    private fun registerSongStampList(stampNameList: List<String>, track: MediaMetadataCompat, isSystem: Boolean) {
        RealmHelper.getRealmInstance().use { realm ->
            for (stampName in stampNameList) {
                SongDao.findByMediaMetadata(realm, track)?.let {
                    SongStampDao.register(realm, it.id, stampName, isSystem)
                }
            }
        }
    }

    private fun removeSongStamp(mediaMetadata: MediaMetadataCompat, stampName: String, isSystem: Boolean) {
        RealmHelper.getRealmInstance().use { realm: Realm ->
            SongDao.findByMediaMetadata(realm, mediaMetadata)?.let { song ->
                SongStampDao.remove(realm, song.id, stampName, isSystem)
            }
        }
    }

    private fun findCategoryStampList(categoryType: CategoryType, categoryValue: String): List<Stamp> {
        return RealmHelper.getRealmInstance().use { realm ->
            CategoryStampDao.findCategoryStampList(realm, categoryType, categoryValue).map { Stamp(it.name, it.isSystem) }
        }
    }

    private fun registerCategoryStampList(stampNameList: List<String>, categoryType: CategoryType, categoryValue: String, isSystem: Boolean) {
        RealmHelper.getRealmInstance().use { realm ->
            for (stampName in stampNameList) {
                CategoryStampDao.create(realm, stampName, categoryType, categoryValue, isSystem)
            }
        }
    }

    private fun removeCategoryStamp(stampName: String, categoryType: CategoryType, categoryValue: String, isSystem: Boolean) {
        RealmHelper.getRealmInstance().use { realm ->
            CategoryStampDao.delete(realm, stampName, categoryType, categoryValue, isSystem)
        }
    }

    private fun resolveCategory(mediaId: String): Pair<CategoryType, String>? {
        val hierarchy = MediaIDHelper.getHierarchy(mediaId)
        return if (hierarchy.size <= 1) {
            null
        } else {
            val providerType = ProviderType.of(hierarchy[0])
            if (providerType?.categoryType != null) {
                Pair(providerType.categoryType!!, hierarchy[1])
            } else {
                null
            }
        }
    }

    fun resolveMediaMetadata(musicIdOrMediaId: String): MediaMetadataCompat? {
        val musicId = if (MediaIDHelper.isTrack(musicIdOrMediaId)) MediaIDHelper.extractMusicIDFromMediaID(musicIdOrMediaId) else musicIdOrMediaId
        val longMusicId = try {
            java.lang.Long.valueOf(musicId)
        } catch (e: NumberFormatException) {
            return null
        }
        return MediaRetrieveHelper.findByMusicId(mContext, longMusicId) { }
    }

    companion object {

        @Suppress("unused")
        private val TAG = LogHelper.makeLogTag(SongController::class.java)
    }

}
