package com.sjn.stamp.controller

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.constant.CategoryType
import com.sjn.stamp.db.Song
import com.sjn.stamp.db.Stamp
import com.sjn.stamp.db.dao.CategoryStampDao
import com.sjn.stamp.db.dao.SongDao
import com.sjn.stamp.db.dao.SongStampDao
import com.sjn.stamp.media.provider.ProviderType
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.MediaRetrieveHelper
import com.sjn.stamp.utils.RealmHelper
import io.realm.Realm
import java.util.*

class SongController(private val mContext: Context) {


    fun registerStampWithoutTransaction(realm: Realm, stampName: String, song: Song, isSystem: Boolean) {
        SongStampDao.saveOrAddWithoutTransaction(realm, SongStampDao.newStandalone(name = stampName, isSystem = isSystem), song)
    }

    fun registerStamp(stampName: String, song: Song, isSystem: Boolean) {
        RealmHelper.getRealmInstance().use { realm ->
            SongStampDao.saveOrAdd(realm, SongStampDao.newStandalone(name = stampName, isSystem = isSystem), song)
        }
    }

    fun registerStampList(stampNameList: List<String>, mediaId: String, isSystem: Boolean) {
        val song = findSongByMusicIdOrMediaId(mediaId)
        if (song != null) {
            registerSongStampList(stampNameList, song, isSystem)
        } else {
            val hierarchy = MediaIDHelper.getHierarchy(mediaId)
            if (hierarchy.size <= 1) {
                return
            }
            registerCategoryStampList(stampNameList, ProviderType.of(hierarchy[0]).categoryType, hierarchy[1], isSystem)
        }
        StampController(mContext).notifyStampChange()
    }

    fun removeStamp(stampName: String, mediaId: String, isSystem: Boolean) {
        if (MediaIDHelper.isTrack(mediaId)) {
            removeSongStamp(stampName, mediaId, isSystem)
        } else {
            val hierarchy = MediaIDHelper.getHierarchy(mediaId)
            if (hierarchy.size <= 1) {
                return
            }
            removeCategoryStamp(stampName, ProviderType.of(hierarchy[0]).categoryType, hierarchy[1], isSystem)
        }
        StampController(mContext).notifyStampChange()
    }

    fun findStampsByMediaId(mediaId: String): List<Stamp> {
        return if (MediaIDHelper.isTrack(mediaId)) {
            findSongStampListByMusicId(mediaId)
        } else {
            val hierarchy = MediaIDHelper.getHierarchy(mediaId)
            if (hierarchy.size <= 1) {
                ArrayList()
            } else {
                findCategoryStampList(ProviderType.of(hierarchy[0]).categoryType, hierarchy[1])
            }
        }
    }

    private fun registerSongStampList(stampNameList: List<String>, track: MediaMetadataCompat, isSystem: Boolean) {
        RealmHelper.getRealmInstance().use { realm ->
            for (stampName in stampNameList) {
                SongStampDao.saveOrAdd(realm, SongStampDao.newStandalone(stampName, isSystem), SongDao.newStandalone(track))
            }
        }
    }

    private fun registerCategoryStampList(stampNameList: List<String>, categoryType: CategoryType, categoryValue: String, isSystem: Boolean) {
        RealmHelper.getRealmInstance().use { realm ->
            for (stampName in stampNameList) {
                CategoryStampDao.save(realm, stampName, categoryType, categoryValue, isSystem)
            }
        }
    }

    private fun removeCategoryStamp(stampName: String, categoryType: CategoryType, categoryValue: String, isSystem: Boolean) {
        RealmHelper.getRealmInstance().use { realm ->
            CategoryStampDao.remove(realm, stampName, categoryType, categoryValue, isSystem)
        }
    }

    //TODO: cache
    private fun removeSongStamp(stampName: String, mediaId: String, isSystem: Boolean) {
        RealmHelper.getRealmInstance().use { realm: Realm ->
            SongDao.findByMusicId(realm, MediaIDHelper.extractMusicIDFromMediaID(mediaId))?.let { song ->
                for (songStamp in song.songStampList) {
                    if (songStamp.name == stampName && songStamp.isSystem == isSystem) {
                        realm.beginTransaction()
                        songStamp.songList.remove(song)
                        song.songStampList.remove(songStamp)
                        realm.commitTransaction()
                        break
                    }
                }
            }
        }
    }

    private fun findCategoryStampList(categoryType: CategoryType, categoryValue: String): List<Stamp> {
        return RealmHelper.getRealmInstance().use { realm ->
            CategoryStampDao.findCategoryStampList(realm, categoryType, categoryValue).map { Stamp(it.name, it.isSystem) }
        }
    }

    private fun findSongStampListByMusicId(mediaId: String): List<Stamp> {
        val musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId)
        val stampList = ArrayList<Stamp>()
        RealmHelper.getRealmInstance().use { realm: Realm ->
            SongDao.findByMusicId(realm, musicId)?.let { song ->
                for (songStamp in song.songStampList) {
                    songStamp.let { s -> stampList.add(Stamp(s.name, s.isSystem)) }
                }
            }
        }
        return stampList
    }

    private fun findSongByMusicIdOrMediaId(musicIdOrMediaId: String): MediaMetadataCompat? {
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
