package com.sjn.stamp.controller

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.model.CategoryStamp
import com.sjn.stamp.model.SongStamp
import com.sjn.stamp.model.constant.CategoryType
import com.sjn.stamp.model.dao.CategoryStampDao
import com.sjn.stamp.model.dao.SongDao
import com.sjn.stamp.model.dao.SongStampDao
import com.sjn.stamp.utils.AnalyticsHelper
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.RealmHelper
import io.realm.Realm
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class StampController(private val context: Context) {
    private val listenerSet = ArrayList<Listener>()

    interface Listener {
        fun onStampChange()
    }

    val isMyStampExists: Boolean
        get() =
            RealmHelper.realmInstance.use { realm ->
                SongStampDao.isMyStampExists(realm)
            }

    val isSmartStampExists: Boolean
        get() =
            RealmHelper.realmInstance.use { realm ->
                SongStampDao.isSmartStampExists(realm)
            }

    fun addListener(listener: Listener) {
        listenerSet.add(listener)
    }

    fun removeListener(listener: Listener) {
        listenerSet.remove(listener)
    }

    fun register(name: String, isSystem: Boolean): Boolean {
        if (name.isEmpty()) {
            return false
        }
        if (RealmHelper.realmInstance.use { SongStampDao.find(it, name, isSystem) } != null) {
            return false
        }
        RealmHelper.realmInstance.use { realm -> SongStampDao.findOrCreate(realm, name, isSystem) }
        notifyStampChange()
        AnalyticsHelper.trackStamp(context, name)
        return true
    }

    fun isCategoryStamp(name: String, isSystem: Boolean, mediaId: String): Boolean {
        val mediaMetadata = SongController(context).resolveMediaMetadata(mediaId) ?: return false
        RealmHelper.realmInstance.use { realm ->
            return CategoryStampDao.findByName(realm, name, isSystem).find { categoryStamp -> categoryStamp.contain(mediaMetadata) } != null
        }
    }

    fun removeSong(name: String, isSystem: Boolean, mediaId: String): Boolean {
        val mediaMetadata = SongController(context).resolveMediaMetadata(mediaId) ?: return false
        RealmHelper.realmInstance.use { realm ->
            val song = SongDao.findByMediaMetadata(realm, mediaMetadata) ?: return false
            return SongStampDao.remove(realm, song.id, name, isSystem)
        }
    }

    fun delete(stamp: String, isSystem: Boolean) {
        RealmHelper.realmInstance.use { realm ->
            delete(realm, stamp, isSystem)
        }
        notifyStampChange()
    }

    fun delete(realm: Realm, stamp: String, isSystem: Boolean) {
        SongStampDao.delete(realm, stamp, isSystem)
        CategoryStampDao.delete(realm, stamp, isSystem)
    }

    fun findAll(): List<String> =
            RealmHelper.realmInstance.use { realm ->
                SongStampDao.findAll(realm).map { it.name }
            }

    fun findAllMyStamps(): List<String> {
        return RealmHelper.realmInstance.use { realm ->
            SongStampDao.findAll(realm, false).map { it.name }
        }
    }

    fun createStampMap(musicListById: MutableMap<String, MediaMetadataCompat>, stampToList: MutableMap<String, MutableList<MediaMetadataCompat>>, isSystem: Boolean): MutableMap<String, MutableList<MediaMetadataCompat>> {
        val stampToMap = findSongStamp(isSystem)
        if (!isSystem) {
            stampToMap.merge(findCategoryStamp(musicListById))
        }
        for ((key, value) in stampToMap) {
            stampToList[key] = ArrayList(value.values)
        }
        return stampToList
    }

    internal fun notifyStampChange() {
        for (listener in listenerSet) {
            listener.onStampChange()
        }
    }

    private fun findSongStamp(isSystem: Boolean): MutableMap<String, MutableMap<String, MediaMetadataCompat>> {
        val songStampMap = ConcurrentHashMap<String, MutableMap<String, MediaMetadataCompat>>()
        RealmHelper.realmInstance.use { realm ->
            for (songStamp in SongStampDao.findAll(realm, isSystem)) {
                songStampMap.putSongMap(songStamp.name, songStamp.songMap())
            }
        }
        return songStampMap
    }

    private fun findCategoryStamp(musicListById: MutableMap<String, MediaMetadataCompat>): MutableMap<String, MutableMap<String, MediaMetadataCompat>> {
        val stampQueryMap = ConcurrentHashMap<String, MutableMap<CategoryType, MutableList<String>>>()
        RealmHelper.realmInstance.use { realm ->
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
            songMap[song.description.mediaId!!] = song
            this[stampName] = songMap
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
            this[stampName] = songMap
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
            this[categoryStamp.name] = queryMap
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
            this[categoryType] = ArrayList(listOf(query))
        }
    }

    private fun SongStamp.songMap(): MutableMap<String, MediaMetadataCompat> {
        val songMap = ConcurrentHashMap<String, MediaMetadataCompat>()
        for (song in this.songList) {
            songMap[song.mediaId] = song.buildMediaMetadataCompat()
        }
        return songMap
    }

    companion object {
        @Suppress("unused")
        private val TAG = LogHelper.makeLogTag(StampController::class.java)
    }
}
