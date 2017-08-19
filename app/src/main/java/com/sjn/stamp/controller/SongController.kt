package com.sjn.stamp.controller

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.constant.CategoryType
import com.sjn.stamp.db.Song
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

    fun registerStamp(stampName: String, song: Song, isSystem: Boolean) {
        RealmHelper.getRealmInstance().use { realm ->
            SongStampDao.saveOrAdd(realm, SongStampDao.newStandalone(name = stampName, isSystem = isSystem), song)
        }
    }

    fun registerStampList(stampNameList: List<String>, mediaId: String, isSystem: Boolean) {
        if (MediaIDHelper.isTrack(mediaId)) {
            registerSongStampList(
                    stampNameList,
                    MediaRetrieveHelper.findByMusicId(
                            mContext,
                            java.lang.Long.valueOf(MediaIDHelper.extractMusicIDFromMediaID(mediaId))!!
                    ) { },
                    isSystem)
        } else {
            val hierarchy = MediaIDHelper.getHierarchy(mediaId)
            if (hierarchy.size <= 1) {
                return
            }
            registerCategoryStampList(stampNameList, ProviderType.of(hierarchy[0]).categoryType, hierarchy[1], isSystem)
        }
        StampController().notifyStampChange()
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
        StampController().notifyStampChange()
    }

    fun findStampsByMediaId(mediaId: String): List<String> {
        return if (MediaIDHelper.isTrack(mediaId)) {
            findSongStampList(mediaId)
        } else {
            val hierarchy = MediaIDHelper.getHierarchy(mediaId)
            if (hierarchy.size <= 1) {
                ArrayList()
            } else findCategoryStampList(ProviderType.of(hierarchy[0]).categoryType, hierarchy[1])
        }
    }

    fun findStampsByMusicId(musicId: String): List<String> = findSongStampListByMusicId(musicId)

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

    private fun findCategoryStampList(categoryType: CategoryType, categoryValue: String): List<String> {
        return RealmHelper.getRealmInstance().use { realm ->
            CategoryStampDao.findCategoryStampList(realm, categoryType, categoryValue).map { it.name }
        }
    }

    //TODO: cache
    private fun findSongStampList(mediaId: String): List<String> = findSongStampListByMusicId(MediaIDHelper.extractMusicIDFromMediaID(mediaId))

    private fun findSongStampListByMusicId(musicId: String): List<String> {
        val stampList = ArrayList<String>()
        RealmHelper.getRealmInstance().use { realm: Realm ->
            SongDao.findByMusicId(realm, musicId)?.let { song ->
                for (songStamp in song.songStampList) {
                    songStamp.name.let { s -> stampList.add(s) }
                }
            }
        }
        return stampList
    }

    companion object {

        @Suppress("unused")
        private val TAG = LogHelper.makeLogTag(SongController::class.java)
    }

}
