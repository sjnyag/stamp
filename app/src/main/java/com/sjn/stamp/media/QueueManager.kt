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

package com.sjn.stamp.media

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.sjn.stamp.R
import com.sjn.stamp.controller.CustomController
import com.sjn.stamp.controller.UserSettingController
import com.sjn.stamp.media.provider.MusicProvider
import com.sjn.stamp.media.provider.single.QueueProvider
import com.sjn.stamp.ui.observer.MediaControllerObserver
import com.sjn.stamp.utils.*
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.util.*

/**
 * Simple data provider for queues. Keeps track of a current queue and a current index in the
 * queue. Also provides methods to set the current queue based on common queries, relying on a
 * given MusicProvider to provide the actual media metadata.
 */
class QueueManager(private val service: MediaBrowserServiceCompat,
                   private val musicProvider: MusicProvider,
                   private val listener: MetadataUpdateListener) : QueueProvider.QueueListener, MediaControllerObserver.Listener {

    override val playingQueueMetadata: Iterable<MediaMetadataCompat>
        get() = playingQueue.map { MediaItemHelper.convertToMetadata(it, currentMusic?.description?.mediaId) }

    // "Now playing" queue:
    private var orderedQueue: List<MediaSessionCompat.QueueItem> = Collections.synchronizedList(ArrayList())
    private var shuffledQueue: MutableList<MediaSessionCompat.QueueItem> = Collections.synchronizedList(ArrayList())
    private var currentIndex: Int = 0
    //to avoid GC
    private var mTarget: Target? = null

    private val playingQueue: List<MediaSessionCompat.QueueItem>
        get() = if (CustomController.getShuffleMode(service) == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            shuffledQueue
        } else orderedQueue

    val currentMusic: MediaSessionCompat.QueueItem?
        get() = if (!QueueHelper.isIndexPlayable(currentIndex, playingQueue)) {
            null
        } else playingQueue[currentIndex]

    override fun onShuffleModeChanged(@PlaybackStateCompat.ShuffleMode shuffleMode: Int) {
        if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            updateShuffleQueue()
        } else {
            //TODO Bug will occur when Callback called but ShuffleState won't change
            if (shuffledQueue.isNotEmpty()) {
                setCurrentQueueIndex(Math.max(QueueHelper.getMusicIndexOnQueueByMediaId(playingQueue, shuffledQueue[currentIndex].description.mediaId), 0))
            }
        }
    }

    private fun updateShuffleQueue() {
        shuffledQueue = ArrayList(orderedQueue)
        shuffleQueue(shuffledQueue as ArrayList<MediaSessionCompat.QueueItem>, currentMusic)
        setCurrentQueueIndex(0)
    }

    init {
        musicProvider.setQueueListener(this)
        MediaControllerObserver.addListener(this)
    }

    fun restorePreviousState(lastMusicId: String?, queueIdentifyMediaId: String) {
        setQueueFromMusic(queueIdentifyMediaId, lastMusicId)
    }

    private fun isSameBrowsingCategory(mediaId: String): Boolean {
        val current = currentMusic ?: return false
        return Arrays.equals(MediaIDHelper.getHierarchy(mediaId), MediaIDHelper.getHierarchy(current.description.mediaId!!))
    }

    private fun setCurrentQueueIndex(index: Int): Boolean {
        if (index >= 0 && index < playingQueue.size) {
            currentIndex = index
            MediaIDHelper.extractMusicIDFromMediaID(currentMusic!!.description.mediaId!!)?.let {
                UserSettingController().lastMusicId = it
            }
            return true
        }
        return false
    }

    fun setCurrentQueueItem(queueId: Long): Boolean {
        // set the current index on queue from the queue Id:
        val index = QueueHelper.getMusicIndexOnQueue(playingQueue, queueId)
        if (setCurrentQueueIndex(index)) {
            listener.onCurrentQueueIndexUpdated(currentIndex)
        }
        return index >= 0
    }

    private fun setCurrentQueueItem(mediaId: String): Boolean {
        // set the current index on queue from the music Id:
        val index = QueueHelper.getMusicIndexOnQueueByMediaId(playingQueue, mediaId)
        if (setCurrentQueueIndex(index)) {
            listener.onCurrentQueueIndexUpdated(currentIndex)
        }
        return index >= 0
    }

    fun skipQueuePosition(amount: Int): Boolean {
        var index = currentIndex + amount
        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = 0
        } else if (CustomController.getRepeatMode(service) == PlaybackStateCompat.REPEAT_MODE_ALL && playingQueue.isNotEmpty()) {
            // skip forwards when in last song will cycle back to start of the queue
            index %= playingQueue.size
        }
        if (!QueueHelper.isIndexPlayable(index, playingQueue)) {
            LogHelper.i(TAG, "Cannot increment queue index by ", amount, ". Current=", currentIndex, " queue length=", playingQueue.size)
            return false
        }
        setCurrentQueueIndex(index)
        return true
    }

    fun skipTo0() {
        setCurrentQueueIndex(0)
    }

    fun setQueueFromSearch(query: String, extras: Bundle): Boolean {
        val queue = QueueHelper.getPlayingQueueFromSearch(query, extras, musicProvider)
        setCurrentQueue(service.getString(R.string.search_queue_title), queue)
        updateMetadata()
        return !queue.isEmpty()
    }

    fun setRandomQueue() {
        setCurrentQueue(service.getString(R.string.random_queue_title), QueueHelper.getRandomQueue(musicProvider))
        updateMetadata()
    }

    fun setQueueFromMusic(mediaId: String, startMusicId: String? = null) {
        LogHelper.d(TAG, "setQueueFromMusic ", mediaId)

        // The mediaId used here is not the unique musicId. This one comes from the
        // MediaBrowser, and is actually a "hierarchy-aware mediaID": a concatenation of
        // the hierarchy in MediaBrowser and the actual unique musicID. This is necessary
        // so we can build the correct playing queue, based on where the track was
        // selected from.
        var canReuseQueue = false
        if (isSameBrowsingCategory(mediaId)) {
            canReuseQueue = setCurrentQueueItem(mediaId)
        }
        if (!canReuseQueue) {
            val queueTitle = service.getString(R.string.browse_musics_by_genre_subtitle,
                    MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaId))
            setCurrentQueue(queueTitle, QueueHelper.getPlayingQueue(mediaId, musicProvider), mediaId, startMusicId)
        }
        updateMetadata()
    }

    fun setCurrentQueue(title: String, newQueue: List<MediaSessionCompat.QueueItem>,
                        initialMediaId: String? = null, startMusicId: String? = null) {
        orderedQueue = newQueue
        if (CustomController.getShuffleMode(service) == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            updateShuffleQueue()
        }
        var index = -1
        if (startMusicId != null) {
            index = QueueHelper.getMusicIndexOnQueueByMusicId(playingQueue, startMusicId)
        }
        if (index == -1 && initialMediaId != null) {
            index = QueueHelper.getMusicIndexOnQueueByMediaId(playingQueue, initialMediaId)
        }
        if (initialMediaId != null
                && !initialMediaId.startsWith(MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE)
                && !initialMediaId.startsWith(MediaIDHelper.MEDIA_ID_MUSICS_BY_DIRECT)
                && !initialMediaId.startsWith(MediaIDHelper.MEDIA_ID_MUSICS_BY_RANKING)) {
            UserSettingController().queueIdentifyMediaId = initialMediaId
        }
        setCurrentQueueIndex(Math.max(index, 0))
        listener.onQueueUpdated(title, newQueue)
    }

    //TODO:
    fun updateMetadata() {
        LogHelper.d(TAG, "updateMetadata")
        if (currentMusic == null) {
            return
        }
        val musicId = MediaIDHelper.extractMusicIDFromMediaID(currentMusic!!.description.mediaId!!)
                ?: return
        val metadata = musicProvider.getMusicByMusicId(musicId) ?: return

        listener.onMetadataChanged(metadata)

        // Set the proper album artwork on the media session, so it can be shown in the
        // locked screen and in other places.
        if (metadata.description.iconBitmap == null && metadata.description.iconUri != null) {
            val albumUri = metadata.description.iconUri!!.toString()
            mTarget = object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    setMetadataMusicArt(bitmap)
                }

                override fun onBitmapFailed(errorDrawable: Drawable?) {
                    val bitmap = AlbumArtHelper.createTextBitmap(metadata.description.title)
                    setMetadataMusicArt(bitmap)
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                private fun setMetadataMusicArt(bitmap: Bitmap) {
                    val icon = AlbumArtHelper.createIcon(bitmap)
                    musicProvider.updateMusicArt(musicId, bitmap, icon)

                    // If we are still playing the same music, notify the listeners:
                    currentMusic?.description?.mediaId?.let {
                        MediaIDHelper.extractMusicIDFromMediaID(it)?.let { currentPlayingId ->
                            if (musicId == currentPlayingId) {
                                musicProvider.getMusicByMusicId(currentPlayingId)?.let {
                                    listener.onMetadataChanged(it)
                                }
                            }
                        }
                    }
                }
            }
            ViewHelper.readBitmapAsync(service, albumUri, mTarget!!)
        }
    }

    interface MetadataUpdateListener {
        fun onMetadataChanged(metadata: MediaMetadataCompat)

        fun onMetadataRetrieveError()

        fun onCurrentQueueIndexUpdated(queueIndex: Int)

        fun onQueueUpdated(title: String, newQueue: List<MediaSessionCompat.QueueItem>)
    }

    companion object {
        private val TAG = LogHelper.makeLogTag(QueueManager::class.java)

        private fun shuffleQueue(queueItemList: MutableList<MediaSessionCompat.QueueItem>, initialQueue: MediaSessionCompat.QueueItem?) {
            if (initialQueue != null) {
                queueItemList.remove(initialQueue)
                queueItemList.shuffle()
                queueItemList.add(0, initialQueue)
            } else {
                queueItemList.shuffle()
            }
        }
    }
}
