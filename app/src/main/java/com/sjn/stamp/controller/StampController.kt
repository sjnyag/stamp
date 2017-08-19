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

    interface Listener {
        fun onStampChange()
    }

    fun addListener(listener: Listener) {
        mListenerSet.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListenerSet.remove(listener)
    }

    fun register(name: String, isSystem: Boolean): Boolean {
        val result = RealmHelper.getRealmInstance().use { realm ->
            SongStampDao.save(realm, name, isSystem)
        }
        notifyStampChange()
        return result
    }

    fun remove(stamp: String, isSystem: Boolean) {
        RealmHelper.getRealmInstance().use { realm ->
            SongStampDao.remove(realm, stamp, isSystem)
            CategoryStampDao.remove(realm, stamp, isSystem)
        }
        notifyStampChange()
    }

    fun findAll(): List<String> {
        return RealmHelper.getRealmInstance().use { realm ->
            SongStampDao.findAll(realm).map { it.name }
        }
    }

    fun createStampMap(musicListById: MutableMap<String, MediaMetadataCompat>, isSystem: Boolean): MutableMap<String, MutableList<MediaMetadataCompat>> {
        val stampToMap = findSongStamp(isSystem)
        if (!isSystem) {
            stampToMap.merge(findCategoryStamp(musicListById))
        }
        val stampToList = ConcurrentHashMap<String, MutableList<MediaMetadataCompat>>()
        for ((key, value) in stampToMap) {
            stampToList.put(key, ArrayList(value.values))
        }
        return stampToList
    }

    internal fun notifyStampChange() {
        for (listener in mListenerSet) {
            listener.onStampChange()
        }
    }

    private fun findSongStamp(isSystem: Boolean): MutableMap<String, MutableMap<String, MediaMetadataCompat>> {
        val songStampMap = ConcurrentHashMap<String, MutableMap<String, MediaMetadataCompat>>()
        RealmHelper.getRealmInstance().use { realm ->
            for (songStamp in SongStampDao.findAll(realm, isSystem)) {
                songStampMap.putSongMap(songStamp.name, songStamp.songMap())
            }
        }
        return songStampMap
    }

    private fun findCategoryStamp(musicListById: MutableMap<String, MediaMetadataCompat>): MutableMap<String, MutableMap<String, MediaMetadataCompat>> {
        val stampQueryMap = ConcurrentHashMap<String, MutableMap<CategoryType, MutableList<String>>>()
        RealmHelper.getRealmInstance().use { realm ->
            for (categoryStamp in CategoryStampDao.findAll(realm)) {
                stampQueryMap.putCategoryStamp(categoryStamp)
            }
        }
        return stampQueryMap.executeQuery(musicListById)
    }

    private fun MutableMap<String, MutableMap<String, MediaMetadataCompat>>.merge(newMap: MutableMap<String, MutableMap<String, MediaMetadataCompat>>): MutableMap<String, MutableMap<String, MediaMetadataCompat>> {
        for ((key, value) in newMap) {
            if (containsKey(key)) {
                this[key]?.putAll(value)
            } else {
                put(key, value)
            }
        }
        return this
    }

    /*
        'favorite' ->  {'mediaId_1' -> song1, 'mediaId_2' -> song2}
        'recent'   ->  {'mediaId_1' -> song1, 'mediaId_2' -> song2}
     */
    private fun MutableMap<String, MutableMap<CategoryType, MutableList<String>>>.executeQuery(musicListById: MutableMap<String, MediaMetadataCompat>): MutableMap<String, MutableMap<String, MediaMetadataCompat>> {
        val result = ConcurrentHashMap<String, MutableMap<String, MediaMetadataCompat>>()
        for (song in musicListById.values) {
            for ((stampName, categoryQueryMap) in this) {
                for ((categoryType, queryList) in categoryQueryMap) {
                    if (queryList.contains(song.getString(categoryType.key).toLowerCase(Locale.getDefault()))) {
                        result.putSong(stampName, song)
                    }
                }
            }
        }
        return result
    }

    /*
        'favorite' ->  {'mediaId_1' -> song1, 'mediaId_2' -> song2}
        'recent'   ->  {'mediaId_1' -> song1, 'mediaId_2' -> song2}
     */
    private fun MutableMap<String, MutableMap<String, MediaMetadataCompat>>.putSong(stampName: String, song: MediaMetadataCompat) {
        if (this.containsKey(stampName) && !this[stampName]!!.isEmpty()) {
            this[stampName]?.put(song.description.mediaId!!, song)
        } else {
            val songMap = ConcurrentHashMap<String, MediaMetadataCompat>()
            songMap.put(song.description.mediaId!!, song)
            this.put(stampName, songMap)
        }
    }

    /*
        'favorite' ->  {'mediaId_1' -> song1, 'mediaId_2' -> song2}
        'recent'   ->  {'mediaId_1' -> song1, 'mediaId_2' -> song2}
     */
    private fun MutableMap<String, MutableMap<String, MediaMetadataCompat>>.putSongMap(stampName: String, songMap: MutableMap<String, MediaMetadataCompat>) {
        if (this.containsKey(stampName)) {
            this[stampName]?.putAll(songMap)
        } else {
            this.put(stampName, songMap)
        }
    }

    /*
        'favorite' ->  {CategoryType.ALBUM -> ['title1', 'title2'], CategoryType.ARTIST -> ['artist1', 'artist2']}
        'recent'   ->  {CategoryType.ALBUM -> ['title3', 'title4'], CategoryType.ARTIST -> ['artist5', 'artist6']}
     */
    private fun MutableMap<String, MutableMap<CategoryType, MutableList<String>>>.putCategoryStamp(categoryStamp: CategoryStamp) {
        if (this.containsKey(categoryStamp.name)) {
            this[categoryStamp.name]?.putQuery(categoryStamp)
        } else {
            val queryMap = ConcurrentHashMap<CategoryType, MutableList<String>>()
            queryMap.putQuery(categoryStamp)
            this.put(categoryStamp.name, queryMap)
        }
    }

    /*
        CategoryType.ALBUM  -> ['title1', 'title2']
        CategoryType.ARTIST -> ['artist1', 'artist2']
     */
    private fun MutableMap<CategoryType, MutableList<String>>.putQuery(categoryStamp: CategoryStamp) {
        val categoryType = CategoryType.of(categoryStamp.type) ?: return
        val query = categoryStamp.value.toLowerCase(Locale.getDefault())
        if (this.containsKey(categoryType) && !this[categoryType]!!.isEmpty()) {
            this[categoryType]?.add(query)
        } else {
            this.put(categoryType, ArrayList(listOf(query)))
        }
    }

    private fun SongStamp.songMap(): MutableMap<String, MediaMetadataCompat> {
        val songMap = ConcurrentHashMap<String, MediaMetadataCompat>()
        for (song in this.songList) {
            songMap.put(song.mediaId, song.buildMediaMetadataCompat())
        }
        return songMap
    }

    companion object {
        @Suppress("unused")
        private val TAG = LogHelper.makeLogTag(StampController::class.java)
    }
}
