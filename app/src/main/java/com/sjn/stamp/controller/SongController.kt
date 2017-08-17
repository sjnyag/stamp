package com.sjn.stamp.controller

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.constant.CategoryType
import com.sjn.stamp.db.Song
import com.sjn.stamp.db.dao.CategoryStampDao
import com.sjn.stamp.db.dao.SongDao
import com.sjn.stamp.db.dao.SongStampDao
import com.sjn.stamp.media.provider.ProviderType
import com.sjn.stamp.utils.*
import java.util.*

class SongController(private val mContext: Context) {
    private val mCategoryStampDao: CategoryStampDao = CategoryStampDao.getInstance()
    private val mSongDao: SongDao = SongDao.getInstance()
    private val mSongStampDao: SongStampDao = SongStampDao.getInstance()

    fun registerSystemStamp(stampName: String, song: Song) {
        val realm = RealmHelper.getRealmInstance()
        val songStamp = mSongStampDao.newStandalone()
        songStamp.name = stampName
        songStamp.isSystem = true
        mSongStampDao.saveOrAdd(realm, songStamp, song)
        realm.close()
    }

    fun registerStampList(stampNameList: List<String>, mediaId: String) {
        if (MediaIDHelper.isTrack(mediaId)) {
            registerSongStampList(
                    stampNameList,
                    MediaRetrieveHelper.findByMusicId(
                            mContext,
                            java.lang.Long.valueOf(MediaIDHelper.extractMusicIDFromMediaID(mediaId))!!
                    ) { })
        } else {
            val hierarchy = MediaIDHelper.getHierarchy(mediaId)
            if (hierarchy.size <= 1) {
                return
            }
            registerCategoryStampList(stampNameList, ProviderType.of(hierarchy[0]).categoryType, hierarchy[1])
        }
        val stampController = StampController(mContext)
        stampController.notifyStampChange()
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

    fun removeStamp(stampName: String, mediaId: String) {
        if (MediaIDHelper.isTrack(mediaId)) {
            removeSongStamp(stampName, mediaId)
        } else {
            val hierarchy = MediaIDHelper.getHierarchy(mediaId)
            if (hierarchy.size <= 1) {
                return
            }
            removeCategoryStamp(stampName, ProviderType.of(hierarchy[0]).categoryType, hierarchy[1])
        }
        val stampController = StampController(mContext)
        stampController.notifyStampChange()
    }

    private fun removeCategoryStamp(stampName: String, categoryType: CategoryType, categoryValue: String) {
        val realm = RealmHelper.getRealmInstance()
        mCategoryStampDao.remove(realm, stampName, categoryType, categoryValue)
        realm.close()
    }

    private fun removeSongStamp(stampName: String, mediaId: String) {
        //TODO: cache
        val musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId)
        val realm = RealmHelper.getRealmInstance()
        val song = mSongDao.findByMusicId(realm, musicId)
        if (song != null) {
            val list = song.songStampList
            if (list != null) {
                for (songStamp in list) {
                    if (songStamp.name != null && songStamp.name == stampName) {
                        realm.beginTransaction()
                        val songList = songStamp.songList
                        songList?.remove(song)
                        song.songStampList!!.remove(songStamp)
                        realm.commitTransaction()
                        break
                    }
                }
            }
        }
        realm.close()
    }

    private fun findCategoryStampList(categoryType: CategoryType, categoryValue: String): List<String> {
        val realm = RealmHelper.getRealmInstance()
        val stampList = mCategoryStampDao.findCategoryStampList(realm, categoryType, categoryValue).filterNotNull().map { it.name!! }
        realm.close()
        return stampList
    }

    private fun findSongStampList(mediaId: String): List<String> {
        //TODO: cache
        val musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId)
        return findSongStampListByMusicId(musicId)
    }

    private fun findSongStampListByMusicId(musicId: String?): List<String> {
        val stampList = ArrayList<String>()
        val realm = RealmHelper.getRealmInstance()
        val song = mSongDao.findByMusicId(realm, musicId)
        if (song != null) {
            val list = song.songStampList
            if (list != null) {
                for (songStamp in list) {
                    songStamp.name?.let { s -> stampList.add(s) }
                }
            }
        }
        realm.close()
        return stampList
    }

    private fun registerCategoryStampList(stampNameList: List<String>, categoryType: CategoryType?, categoryValue: String) {
        if (categoryType == null) {
            return
        }
        val realm = RealmHelper.getRealmInstance()
        for (stampName in stampNameList) {
            mCategoryStampDao.save(realm, stampName, categoryType, categoryValue)
        }
        realm.close()
    }

    private fun registerSongStampList(stampNameList: List<String>, track: MediaMetadataCompat?) {
        if (track == null) {
            return
        }
        val song = SongDao.getInstance().newStandalone()
        MediaItemHelper.updateSong(song, track)
        val realm = RealmHelper.getRealmInstance()
        for (stampName in stampNameList) {
            val songStamp = mSongStampDao.newStandalone()
            songStamp.name = stampName
            songStamp.isSystem = false
            mSongStampDao.saveOrAdd(realm, songStamp, song)
        }
        realm.close()
    }

    companion object {

        @Suppress("unused")
        private val TAG = LogHelper.makeLogTag(SongController::class.java)
    }

}
