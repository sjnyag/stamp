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
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.MediaRetrieveHelper
import com.sjn.stamp.utils.RealmHelper
import io.realm.Realm

class SongController(private val context: Context) {

    fun findSong(songId: Long): Song? {
        return RealmHelper.realmInstance.use { realm ->
            SongDao.findById(realm, songId)
        }
    }

    fun mergeSong(unknownSong: Song, mediaMetadata: MediaMetadataCompat): Boolean {
        RealmHelper.realmInstance.use { realm ->
            return SongDao.merge(realm, unknownSong.id, SongDao.findOrCreate(realm, mediaMetadata).id)
        }
    }

    fun calculateSmartStamp(songId: Long, playCount: Int, recordType: RecordType) {
        Thread(Runnable {
            SmartStamp.values()
                    .filter { it.isTarget(context, songId, playCount, recordType) }
                    .forEach { it.register(context, songId, playCount, recordType) }
        }).start()
    }

    fun refreshAllSongs(musicList: Collection<MediaMetadataCompat>) {
        RealmHelper.realmInstance.use { realm ->
            SongDao.findAll(realm).forEach {
                SongDao.loadLocalMedia(realm, it.id, musicList)
            }
        }
    }

    fun findStampsByMediaId(mediaId: String): List<Stamp> {
        val mediaMetadata = resolveMediaMetadata(mediaId)
        if (MediaIDHelper.isTrack(mediaId) && mediaMetadata != null) {
            return findSongStampListByMediaMetadata(mediaMetadata)
        } else {
            resolveCategory(mediaId)?.let {
                return findCategoryStampList(it.first, it.second)
            }
            return ArrayList()
        }
    }

    fun registerStamp(songId: Long, stampName: String, isSystem: Boolean) {
        RealmHelper.realmInstance.use { realm ->
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
        StampController(context).notifyStampChange()
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
        StampController(context).notifyStampChange()
    }

    private fun findSongStampListByMediaMetadata(mediaMetadata: MediaMetadataCompat): List<Stamp> {
        val stampList = ArrayList<Stamp>()
        RealmHelper.realmInstance.use { realm: Realm ->
            SongDao.findByMediaMetadata(realm, mediaMetadata)?.let { song ->
                for (songStamp in song.songStampList) {
                    songStamp.let { s -> stampList.add(Stamp(s.name, s.isSystem)) }
                }
            }
        }
        return stampList
    }

    private fun registerSongStampList(stampNameList: List<String>, track: MediaMetadataCompat, isSystem: Boolean) {
        RealmHelper.realmInstance.use { realm ->
            for (stampName in stampNameList) {
                SongDao.findOrCreate(realm, track).let {
                    SongStampDao.register(realm, it.id, stampName, isSystem)
                }
            }
        }
    }

    private fun removeSongStamp(mediaMetadata: MediaMetadataCompat, stampName: String, isSystem: Boolean) {
        RealmHelper.realmInstance.use { realm: Realm ->
            SongDao.findByMediaMetadata(realm, mediaMetadata)?.let { song ->
                SongStampDao.remove(realm, song.id, stampName, isSystem)
            }
        }
    }

    private fun findCategoryStampList(categoryType: CategoryType, categoryValue: String): List<Stamp> {
        return RealmHelper.realmInstance.use { realm ->
            CategoryStampDao.findCategoryStampList(realm, categoryType, categoryValue).map { Stamp(it.name, it.isSystem) }
        }
    }

    private fun registerCategoryStampList(stampNameList: List<String>, categoryType: CategoryType, categoryValue: String, isSystem: Boolean) {
        RealmHelper.realmInstance.use { realm ->
            for (stampName in stampNameList) {
                CategoryStampDao.create(realm, stampName, categoryType, categoryValue, isSystem)
            }
        }
    }

    private fun removeCategoryStamp(stampName: String, categoryType: CategoryType, categoryValue: String, isSystem: Boolean) {
        RealmHelper.realmInstance.use { realm ->
            CategoryStampDao.delete(realm, stampName, categoryType, categoryValue, isSystem)
        }
    }

    private fun resolveCategory(mediaId: String): Pair<CategoryType, String>? {
        MediaIDHelper.getHierarchy(mediaId).run {
            if (this.size > 1) {
                ProviderType.of(this[0])?.categoryType?.let {
                    return Pair(it, this[1])
                }
            }
        }
        return null
    }

    fun resolveMediaMetadata(musicIdOrMediaId: String): MediaMetadataCompat? {
        val musicId = if (MediaIDHelper.isTrack(musicIdOrMediaId)) MediaIDHelper.extractMusicIDFromMediaID(musicIdOrMediaId) else musicIdOrMediaId
        val longMusicId = try {
            java.lang.Long.valueOf(musicId)
        } catch (e: NumberFormatException) {
            return null
        }
        return MediaRetrieveHelper.findByMusicId(context, longMusicId, object : MediaRetrieveHelper.PermissionRequiredCallback {
            override fun onPermissionRequired() {
            }
        })
    }

}
