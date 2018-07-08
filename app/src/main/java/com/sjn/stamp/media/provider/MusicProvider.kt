/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sjn.stamp.media.provider

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.AsyncTask
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.media.provider.single.QueueProvider
import com.sjn.stamp.media.source.MusicProviderSource
import com.sjn.stamp.ui.observer.MusicListObserver
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.MediaIDHelper.MEDIA_ID_ROOT
import com.sjn.stamp.utils.MediaItemHelper
import java.util.*

/**
 * Simple data provider for music tracks. The actual metadata source is delegated to a
 * MusicProviderSource defined by a constructor argument of this class.
 */
class MusicProvider(private val context: Context, private val source: MusicProviderSource) {

    private var musicMap: HashMap<String, MediaMetadataCompat> = hashMapOf()
    private val providerMap = ProviderType.setUp(context)
    private var asyncTask: RetrieveMediaAsyncTask? = null
    @Volatile
    private var currentState: ListProvider.ProviderState = ListProvider.ProviderState.NON_INITIALIZED

    /**
     * Get an iterator over a shuffled collection of all songs
     */
    val shuffledMusic: Iterable<MediaMetadataCompat>
        get() {
            if (currentState != ListProvider.ProviderState.INITIALIZED) {
                return emptyList()
            }
            return ArrayList<MediaMetadataCompat>(musicMap.size).apply {
                this += musicMap.values
                this.shuffle()
            }
        }

    val isInitialized: Boolean
        get() = currentState == ListProvider.ProviderState.INITIALIZED

    interface Callback {
        fun onMusicCatalogReady(success: Boolean)
    }

    fun setQueueListener(queueListener: QueueProvider.QueueListener) {
        getProvider(ProviderType.QUEUE)?.let {
            if (it is QueueProvider) {
                it.queueListener = queueListener
            }
        }
    }

    fun getMusicsHierarchy(categoryType: String, categoryValue: String?): List<MediaMetadataCompat>? {
        LogHelper.d(TAG, "getMusicsHierarchy categoryType: ", categoryType, ", categoryValue: ", categoryValue)
        return providerMap[ProviderType.of(categoryType)]?.getListByKey(categoryValue, currentState, musicMap)
    }

    /**
     * Very basic implementation of a search that filter music tracks with title containing
     * the given query.
     */
    fun searchMusicBySongTitle(query: String): Iterable<MediaMetadataCompat> {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_TITLE, query)
    }

    /**
     * Very basic implementation of a search that filter music tracks with album containing
     * the given query.
     */
    fun searchMusicByAlbum(query: String): Iterable<MediaMetadataCompat> {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ALBUM, query)
    }

    /**
     * Very basic implementation of a search that filter music tracks with artist containing
     * the given query.
     */
    fun searchMusicByArtist(query: String): Iterable<MediaMetadataCompat> {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ARTIST, query)
    }

    /**
     * Very basic implementation of a search that filter music tracks with artist containing
     * the given query.
     */
    fun searchMusicByGenre(query: String): Iterable<MediaMetadataCompat> {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_GENRE, query)
    }

    private fun searchMusic(metadataField: String, query: String?): Iterable<MediaMetadataCompat> {
        if (currentState != ListProvider.ProviderState.INITIALIZED || query == null || query.isEmpty()) {
            return emptyList()
        }
        return query.toLowerCase(Locale.getDefault()).run {
            musicMap.values.filter { it.getString(metadataField)?.toLowerCase(Locale.getDefault())?.contains(this) == true }
        }
    }

    /**
     * Return the MediaMetadataCompat for the given musicID.
     *
     * @param musicId The unique, non-hierarchical music ID.
     */
    fun getMusicByMusicId(musicId: String): MediaMetadataCompat? {
        return if (musicMap.containsKey(musicId)) musicMap[musicId] else null
    }

    @Synchronized
    fun updateMusicArt(musicId: String, albumArt: Bitmap, icon: Bitmap) {
        getMusicByMusicId(musicId)?.let {
            musicMap[musicId] = MediaItemHelper.updateMusicArt(it, albumArt, icon)
        }
    }

    fun getChildren(mediaId: String, resources: Resources): List<MediaBrowserCompat.MediaItem> {
        LogHelper.i(TAG, "getChildren mediaId: $mediaId")
        val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()

        if (!MediaIDHelper.isBrowseable(mediaId)) {
            return mediaItems
        }
        if (MEDIA_ID_ROOT == mediaId) {
            providerMap.values.mapTo(mediaItems) { it.getRootMenu(resources) }
        } else {
            ProviderType.of(mediaId)?.let { getProvider(it)?.let { return it.getListItems(mediaId, resources, currentState, musicMap) } }
        }
        return mediaItems
    }

    private fun getProvider(type: ProviderType?): ListProvider? {
        return if (type == null || !providerMap.containsKey(type)) {
            null
        } else providerMap[type]
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     */
    @Synchronized
    fun retrieveMediaAsync(callback: Callback?) {
        LogHelper.d(TAG, "retrieveMediaAsync called")
        if (currentState == ListProvider.ProviderState.INITIALIZED) {
            callback?.onMusicCatalogReady(true)
            return
        }
        asyncTask?.cancel(true)
        asyncTask = RetrieveMediaAsyncTask(this, callback)
        asyncTask?.execute()
    }

    fun cacheAndNotifyLatestMusicMap() {
        Thread(Runnable {
            musicMap = createLatestMusicMap()
//            ProviderCache.saveCache(context, musicMap)
//            LogHelper.i(TAG, "Write " + musicMap.size + " songs to cache")
            MusicListObserver.notifyMediaListUpdated()
        }).start()
    }

    private fun createLatestMusicMap(): HashMap<String, MediaMetadataCompat> {
        val musicListById = HashMap<String, MediaMetadataCompat>()
        val iterator = source.iterator()
        while (iterator.hasNext()) {
            iterator.next().run {
                this.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)?.let {
                    musicListById[it] = this
                }
            }
        }
        return musicListById
    }

    companion object {

        private class RetrieveMediaAsyncTask internal constructor(val musicProvider: MusicProvider, internal val callback: Callback?) : AsyncTask<Void, Void, ListProvider.ProviderState>() {

            override fun doInBackground(vararg params: Void): ListProvider.ProviderState {
                retrieveMedia()
                LogHelper.i(TAG, "RetrieveMediaAsyncTask.doInBackground end")
                return musicProvider.currentState
            }

            override fun onPostExecute(current: ListProvider.ProviderState) {
                callback?.onMusicCatalogReady(current == ListProvider.ProviderState.INITIALIZED)
                musicProvider.cacheAndNotifyLatestMusicMap()
            }

            override fun onCancelled(current: ListProvider.ProviderState) {
                callback?.onMusicCatalogReady(current == ListProvider.ProviderState.INITIALIZED)
                musicProvider.currentState = ListProvider.ProviderState.NON_INITIALIZED
            }

            @Synchronized
            private fun retrieveMedia() {
                try {
                    if (musicProvider.currentState == ListProvider.ProviderState.NON_INITIALIZED) {
                        musicProvider.currentState = ListProvider.ProviderState.INITIALIZING
//                        musicProvider.musicMap = ProviderCache.readCache(musicProvider.context)
//                        LogHelper.i(TAG, "Read " + musicProvider.musicMap.size + " songs from cache")
//                        if (musicProvider.musicMap.size == 0) {
                        musicProvider.musicMap = musicProvider.createLatestMusicMap()
//                        }
                        musicProvider.currentState = ListProvider.ProviderState.INITIALIZED
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (musicProvider.currentState != ListProvider.ProviderState.INITIALIZED) {
                        // Something bad happened, so we reset ProviderState to NON_INITIALIZED to allow
                        // retries (eg if the network connection is temporary unavailable)
                        musicProvider.currentState = ListProvider.ProviderState.NON_INITIALIZED
                    }
                }
            }
        }

        private val TAG = LogHelper.makeLogTag(MusicProvider::class.java)
    }

}
