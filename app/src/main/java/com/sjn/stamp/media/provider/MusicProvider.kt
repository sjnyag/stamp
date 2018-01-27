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
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple data provider for music tracks. The actual metadata source is delegated to a
 * MusicProviderSource defined by a constructor argument of this class.
 */
class MusicProvider(private val mContext: Context, private val mSource: MusicProviderSource) {

    // Categorized caches for music track data:
    private var mMusicListById: HashMap<String, MediaMetadataCompat> = hashMapOf()
    private val mListProviderMap = HashMap<ProviderType, ListProvider>()
    private val mFavoriteTracks: MutableSet<String>
    private var mAsyncTask: RetrieveMediaAsyncTask? = null

    @Volatile
    private var mCurrentState: ListProvider.ProviderState = ListProvider.ProviderState.NON_INITIALIZED

    /**
     * Get an iterator over a shuffled collection of all songs
     */
    val shuffledMusic: Iterable<MediaMetadataCompat>
        get() {
            if (mCurrentState != ListProvider.ProviderState.INITIALIZED) {
                return emptyList()
            }
            val shuffled = ArrayList<MediaMetadataCompat>(mMusicListById.size)
            shuffled += mMusicListById.values
            Collections.shuffle(shuffled)
            return shuffled
        }

    val isInitialized: Boolean
        get() = mCurrentState == ListProvider.ProviderState.INITIALIZED

    interface Callback {
        fun onMusicCatalogReady(success: Boolean)
    }

    init {
        mMusicListById = HashMap()
        mFavoriteTracks = Collections.newSetFromMap(ConcurrentHashMap())
        for (providerType in ProviderType.values()) {
            val listProvider = providerType.newProvider(mContext)
            if (listProvider != null) {
                mListProviderMap[providerType] = listProvider
            }
        }
    }

    fun setQueueListener(queueListener: QueueProvider.QueueListener) {
        val provider = getProvider(ProviderType.QUEUE)
        if (provider != null && provider is QueueProvider) {
            provider.queueListener = queueListener
        }
    }

    fun getMusicsHierarchy(categoryType: String, categoryValue: String?): List<MediaMetadataCompat>? {
        LogHelper.d(TAG, "getMusicsHierarchy categoryType: ", categoryType, ", categoryValue: ", categoryValue)
        val listProvider = mListProviderMap[ProviderType.of(categoryType)] ?: return null
        return listProvider.getListByKey(categoryValue, mCurrentState, mMusicListById)
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
        if (mCurrentState != ListProvider.ProviderState.INITIALIZED || query == null || query.isEmpty()) {
            return emptyList()
        }
        val lowerQuery = query.toLowerCase(Locale.getDefault())
        return mMusicListById.values.filter { it.getString(metadataField).toLowerCase(Locale.getDefault()).contains(lowerQuery) }
    }


    /**
     * Return the MediaMetadataCompat for the given musicID.
     *
     * @param musicId The unique, non-hierarchical music ID.
     */
    fun getMusicByMusicId(musicId: String): MediaMetadataCompat? {
        return if (mMusicListById.containsKey(musicId)) mMusicListById[musicId] else null
    }

    @Synchronized
    fun updateMusicArt(musicId: String, albumArt: Bitmap, icon: Bitmap) {
        mMusicListById[musicId] = MediaItemHelper.updateMusicArt(getMusicByMusicId(musicId), albumArt, icon)
    }

    fun getChildren(mediaId: String, resources: Resources): List<MediaBrowserCompat.MediaItem> {
        LogHelper.i(TAG, "getChildren mediaId: " + mediaId)
        val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()

        if (!MediaIDHelper.isBrowseable(mediaId)) {
            return mediaItems
        }

        if (MEDIA_ID_ROOT == mediaId) {
            mListProviderMap.values.mapTo(mediaItems) { it.getRootMenu(resources) }
        } else {
            val type = ProviderType.of(mediaId)
            if (type != null) {
                return getProvider(type)!!.getListItems(mediaId, resources, mCurrentState, mMusicListById)
            }
        }
        return mediaItems
    }

    private fun getProvider(type: ProviderType?): ListProvider? {
        return if (type == null || !mListProviderMap.containsKey(type)) {
            null
        } else mListProviderMap[type]
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     */
    @Synchronized
    fun retrieveMediaAsync(callback: Callback?) {
        LogHelper.d(TAG, "retrieveMediaAsync called")
        if (mCurrentState == ListProvider.ProviderState.INITIALIZED) {
            callback?.onMusicCatalogReady(true)
            return
        }
        if (mAsyncTask != null) {
            mAsyncTask!!.cancel(true)
        }
        mAsyncTask = RetrieveMediaAsyncTask(callback)
        mAsyncTask!!.execute()
    }

    fun cacheAndNotifyLatestMusicMap() {
        Thread(Runnable {
            mMusicListById = createLatestMusicMap()
            ProviderCache.saveCache(mContext, mMusicListById)
            MusicListObserver.getInstance().notifyMediaListUpdated()
        }).start()
    }

    private fun createLatestMusicMap(): HashMap<String, MediaMetadataCompat> {
        val musicListById = HashMap<String, MediaMetadataCompat>()
        val tracks = mSource.iterator()
        while (tracks.hasNext()) {
            val item = tracks.next()
            val musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            musicListById[musicId] = item
        }
        return musicListById
    }

    private inner class RetrieveMediaAsyncTask internal constructor(internal val callback: Callback?) : AsyncTask<Void, Void, ListProvider.ProviderState>() {

        override fun doInBackground(vararg params: Void): ListProvider.ProviderState {
            retrieveMedia()
            LogHelper.i(TAG, "RetrieveMediaAsyncTask.doInBackground end")
            return mCurrentState
        }

        override fun onPostExecute(current: ListProvider.ProviderState) {
            callback?.onMusicCatalogReady(current == ListProvider.ProviderState.INITIALIZED)
            cacheAndNotifyLatestMusicMap()
        }

        override fun onCancelled(current: ListProvider.ProviderState) {
            callback?.onMusicCatalogReady(current == ListProvider.ProviderState.INITIALIZED)
            mCurrentState = ListProvider.ProviderState.NON_INITIALIZED
        }

        @Synchronized
        private fun retrieveMedia() {
            try {
                if (mCurrentState == ListProvider.ProviderState.NON_INITIALIZED) {
                    mCurrentState = ListProvider.ProviderState.INITIALIZING
                    mMusicListById = ProviderCache.readCache(mContext)
                    LogHelper.i(TAG, "Read " + mMusicListById.size + " songs from cache")
                    if (mMusicListById.size == 0) {
                        mMusicListById = createLatestMusicMap()
                    }
                    mCurrentState = ListProvider.ProviderState.INITIALIZED
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (mCurrentState != ListProvider.ProviderState.INITIALIZED) {
                    // Something bad happened, so we reset ProviderState to NON_INITIALIZED to allow
                    // retries (eg if the network connection is temporary unavailable)
                    mCurrentState = ListProvider.ProviderState.NON_INITIALIZED
                }
            }
        }
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(MusicProvider::class.java)
    }

}
