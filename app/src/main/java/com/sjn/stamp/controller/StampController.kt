package com.sjn.stamp.controller

import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.constant.CategoryType
import com.sjn.stamp.db.CategoryStamp
import com.sjn.stamp.db.SongStamp
import com.sjn.stamp.db.dao.CategoryStampDao
import com.sjn.stamp.db.dao.SongStampDao
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.RealmHelper
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class StampController {
    private val mListenerSet = ArrayList<Listener>()

    fun addListener(listener: Listener) {
        mListenerSet.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListenerSet.remove(listener)
    }

    fun remove(stamp: String) {
        val realm = RealmHelper.getRealmInstance()
        SongStampDao.remove(realm, stamp)
        CategoryStampDao.remove(realm, stamp)
        realm.close()
        notifyStampChange()
    }

    interface Listener {
        fun onStampChange()
    }

    internal fun notifyStampChange() {
        for (listener in mListenerSet) {
            listener.onStampChange()
        }
    }

    fun findAll(): List<String> {
        val realm = RealmHelper.getRealmInstance()
        val stampList = SongStampDao.findAll(realm).map { it.name }
        realm.close()
        return stampList
    }

    fun register(name: String): Boolean {
        val realm = RealmHelper.getRealmInstance()
        val result = SongStampDao.save(realm, name)
        realm.close()
        notifyStampChange()
        return result
    }

    fun getAllSongList(musicListById: MutableMap<String, MediaMetadataCompat>): MutableMap<String, MutableList<MediaMetadataCompat>> {
        val songStampMap = ConcurrentHashMap<String, MutableMap<String, MediaMetadataCompat>>()
        val realm = RealmHelper.getRealmInstance()
        for (songStamp in SongStampDao.findAll(realm)) {
            put(songStampMap, songStamp.name, createTrackMap(songStamp))
        }
        val stampQueryMap = ConcurrentHashMap<String, MutableMap<CategoryType, MutableList<String>>>()
        for (categoryStamp in CategoryStampDao.findAll(realm)) {
            put(stampQueryMap, categoryStamp)
        }
        realm.close()

        val categoryStampMap = searchMusic(musicListById, stampQueryMap)
        merge(songStampMap, categoryStampMap)

        val stampSongMap = ConcurrentHashMap<String, MutableList<MediaMetadataCompat>>()
        for ((key, value) in songStampMap) {
            stampSongMap.put(key, ArrayList(value.values))
        }
        return stampSongMap
    }

    private fun merge(songStampMap: MutableMap<String, MutableMap<String, MediaMetadataCompat>>, categoryStampMap: MutableMap<String, MutableMap<String, MediaMetadataCompat>>) {
        for (stamp in categoryStampMap.keys) {
            if (songStampMap.containsKey(stamp)) {
                songStampMap[stamp]?.putAll(categoryStampMap[stamp]!!)
            } else {
                songStampMap.put(stamp, categoryStampMap[stamp]!!)
            }
        }
    }

    private fun createTrackMap(songStamp: SongStamp): MutableMap<String, MediaMetadataCompat> {
        val trackMap = ConcurrentHashMap<String, MediaMetadataCompat>()
        for (song in songStamp.songList) {
            trackMap.put(song.mediaId, song.buildMediaMetadataCompat())
        }
        return trackMap
    }

    private fun searchMusic(musicListById: MutableMap<String, MediaMetadataCompat>?, queryMap: MutableMap<String, MutableMap<CategoryType, MutableList<String>>>?): MutableMap<String, MutableMap<String, MediaMetadataCompat>> {
        val result = ConcurrentHashMap<String, MutableMap<String, MediaMetadataCompat>>()
        if (musicListById == null || queryMap == null) {
            return result
        }
        for (track in musicListById.values) {
            for ((key, value) in queryMap) {
                for ((key1, value1) in value) {
                    if (value1.contains(track.getString(key1.key).toLowerCase(Locale.getDefault()))) {
                        put(result, key, track)
                    }
                }
            }
        }
        return result
    }

    private fun put(stampMap: MutableMap<String, MutableMap<String, MediaMetadataCompat>>?, stampName: String, track: MediaMetadataCompat) {
        if (stampMap == null) {
            return
        }
        if (stampMap.containsKey(stampName) && !stampMap[stampName]!!.isEmpty()) {
            stampMap[stampName]?.put(track.description.mediaId!!, track)
        } else {
            val trackMap = ConcurrentHashMap<String, MediaMetadataCompat>()
            trackMap.put(track.description.mediaId!!, track)
            stampMap.put(stampName, trackMap)
        }
    }

    private fun put(stampMap: MutableMap<String, MutableMap<String, MediaMetadataCompat>>?, stampName: String, trackMap: MutableMap<String, MediaMetadataCompat>?) {
        if (stampMap == null || trackMap == null) {
            return
        }
        if (stampMap.containsKey(stampName)) {
            stampMap[stampName]?.putAll(trackMap)
        } else {
            stampMap.put(stampName, trackMap)
        }
    }

    private fun put(stampQueryMap: MutableMap<String, MutableMap<CategoryType, MutableList<String>>>?, categoryStamp: CategoryStamp?) {
        if (stampQueryMap == null || categoryStamp == null) {
            return
        }
        if (stampQueryMap.containsKey(categoryStamp.name)) {
            putQuery(stampQueryMap[categoryStamp.name], categoryStamp)
        } else {
            val queryMap = ConcurrentHashMap<CategoryType, MutableList<String>>()
            putQuery(queryMap, categoryStamp)
            stampQueryMap.put(categoryStamp.name, queryMap)
        }
    }

    private fun putQuery(queryMap: MutableMap<CategoryType, MutableList<String>>?, categoryStamp: CategoryStamp?) {
        if (queryMap == null || categoryStamp == null) {
            return
        }
        val categoryType = CategoryType.of(categoryStamp.type) ?: return
        val query = categoryStamp.value.toLowerCase(Locale.getDefault())
        if (queryMap.containsKey(categoryType) && !queryMap[categoryType]!!.isEmpty()) {
            queryMap[categoryType]!!.add(query)
        } else {
            queryMap.put(categoryType, ArrayList(listOf(query)))
        }
    }

    companion object {
        @Suppress("unused")
        private val TAG = LogHelper.makeLogTag(StampController::class.java)
    }
}
