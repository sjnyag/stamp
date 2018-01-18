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

package com.sjn.stamp.media.player

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.sjn.stamp.R
import com.sjn.stamp.controller.UserSettingController
import com.sjn.stamp.media.CustomController
import com.sjn.stamp.media.provider.MusicProvider
import com.sjn.stamp.media.provider.single.QueueProvider
import com.sjn.stamp.model.constant.RepeatState
import com.sjn.stamp.model.constant.ShuffleState
import com.sjn.stamp.utils.*
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.util.*

/**
 * Simple data provider for queues. Keeps track of a current queue and a current index in the
 * queue. Also provides methods to set the current queue based on common queries, relying on a
 * given MusicProvider to provide the actual media metadata.
 */
class QueueManager(private val mContext: Context,
                   private val mMusicProvider: MusicProvider,
                   private val mResources: Resources,
                   private val mListener: MetadataUpdateListener) : QueueProvider.QueueListener, CustomController.ShuffleStateListener {

    // "Now playing" queue:
    private var mOrderedQueue: List<MediaSessionCompat.QueueItem>? = null
    private var mShuffledQueue: MutableList<MediaSessionCompat.QueueItem>? = null
    private var mCurrentIndex: Int = 0
    //to avoid GC
    private var mTarget: Target? = null

    private val playingQueue: List<MediaSessionCompat.QueueItem>?
        get() = if (CustomController.shuffleState === ShuffleState.SHUFFLE) {
            mShuffledQueue
        } else mOrderedQueue

    val currentMusic: MediaSessionCompat.QueueItem?
        get() = if (!QueueHelper.isIndexPlayable(mCurrentIndex, playingQueue)) {
            null
        } else playingQueue!![mCurrentIndex]

    val currentQueueSize: Int
        get() = if (playingQueue == null) {
            0
        } else playingQueue!!.size

    override fun onShuffleStateChanged(state: ShuffleState) {
        if (state === ShuffleState.SHUFFLE) {
            updateShuffleQueue()
        }
    }

    private fun updateShuffleQueue() {
        mShuffledQueue = ArrayList(mOrderedQueue!!)
        shuffleQueue(mShuffledQueue as ArrayList<MediaSessionCompat.QueueItem>, currentMusic)
        setCurrentQueueIndex(0)
    }

    init {
        CustomController.addShuffleStateListenerSet(this)

        mOrderedQueue = Collections.synchronizedList(ArrayList())
        mShuffledQueue = Collections.synchronizedList(ArrayList())
        mCurrentIndex = 0
        mMusicProvider.setQueueListener(this)
    }

    fun restorePreviousState(lastMusicId: String?, queueIdentifyMediaId: String) {
        setQueueFromMusic(queueIdentifyMediaId, lastMusicId)
        onShuffleStateChanged(CustomController.shuffleState)
    }

    private fun isSameBrowsingCategory(mediaId: String): Boolean {
        val newBrowseHierarchy = MediaIDHelper.getHierarchy(mediaId)
        val current = currentMusic ?: return false
        val currentBrowseHierarchy = MediaIDHelper.getHierarchy(
                current.description.mediaId!!)

        return Arrays.equals(newBrowseHierarchy, currentBrowseHierarchy)
    }

    private fun setCurrentQueueIndex(index: Int): Boolean {
        if (index >= 0 && index < playingQueue!!.size) {
            mCurrentIndex = index
            UserSettingController().lastMusicId = MediaIDHelper.extractMusicIDFromMediaID(currentMusic!!.description.mediaId!!)
            return true
        }
        return false
    }

    fun setCurrentQueueItem(queueId: Long): Boolean {
        // set the current index on queue from the queue Id:
        val index = QueueHelper.getMusicIndexOnQueue(playingQueue, queueId)
        if (setCurrentQueueIndex(index)) {
            mListener.onCurrentQueueIndexUpdated(mCurrentIndex)
        }
        return index >= 0
    }

    private fun setCurrentQueueItem(mediaId: String): Boolean {
        // set the current index on queue from the music Id:
        val index = QueueHelper.getMusicIndexOnQueueByMediaId(playingQueue, mediaId)
        if (setCurrentQueueIndex(index)) {
            mListener.onCurrentQueueIndexUpdated(mCurrentIndex)
        }
        return index >= 0
    }

    fun skipQueuePosition(amount: Int): Boolean {
        var index = mCurrentIndex + amount
        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = 0
        } else if (CustomController.repeatState === RepeatState.ALL) {
            // skip forwards when in last song will cycle back to start of the queue
            index %= playingQueue!!.size
        }
        if (!QueueHelper.isIndexPlayable(index, playingQueue)) {
            LogHelper.e(TAG, "Cannot increment queue index by ", amount,
                    ". Current=", mCurrentIndex, " queue length=", playingQueue!!.size)
            return false
        }
        setCurrentQueueIndex(index)
        return true
    }

    fun skipTo0() {
        setCurrentQueueIndex(0)
    }

    fun setQueueFromSearch(query: String, extras: Bundle): Boolean {
        val queue = QueueHelper.getPlayingQueueFromSearch(query, extras, mMusicProvider)
        setCurrentQueue(mResources.getString(R.string.search_queue_title), queue)
        updateMetadata()
        return queue != null && !queue.isEmpty()
    }

    fun setRandomQueue() {
        setCurrentQueue(mResources.getString(R.string.random_queue_title),
                QueueHelper.getRandomQueue(mMusicProvider))
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
            val queueTitle = mResources.getString(R.string.browse_musics_by_genre_subtitle,
                    MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaId))
            setCurrentQueue(queueTitle,
                    QueueHelper.getPlayingQueue(mediaId, mMusicProvider), mediaId, startMusicId)
        }
        updateMetadata()
    }

    @JvmOverloads
    fun setCurrentQueue(title: String, newQueue: List<MediaSessionCompat.QueueItem>?,
                        initialMediaId: String? = null, startMusicId: String? = null) {
        mOrderedQueue = newQueue
        if (CustomController.shuffleState === ShuffleState.SHUFFLE) {
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
            val userSettingController = UserSettingController()
            userSettingController.queueIdentifyMediaId = initialMediaId
        }
        setCurrentQueueIndex(Math.max(index, 0))
        mListener.onQueueUpdated(title, newQueue!!)
    }

    //TODO:
    fun updateMetadata() {
        LogHelper.d(TAG, "updateMetadata")
        if (currentMusic == null) {
            return
        }
        val musicId = MediaIDHelper.extractMusicIDFromMediaID(
                currentMusic!!.description.mediaId!!)
        val metadata = mMusicProvider.getMusicByMusicId(musicId) ?: return

        mListener.onMetadataChanged(metadata)

        // Set the proper album artwork on the media session, so it can be shown in the
        // locked screen and in other places.
        if (metadata.description.iconBitmap == null && metadata.description.iconUri != null) {
            val albumUri = metadata.description.iconUri!!.toString()
            mTarget = object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    val icon = ViewHelper.createIcon(bitmap)
                    mMusicProvider.updateMusicArt(musicId, bitmap, icon)

                    // If we are still playing the same music, notify the listeners:
                    val currentPlayingId = MediaIDHelper.extractMusicIDFromMediaID(currentMusic?.description?.mediaId!!)
                    if (musicId == currentPlayingId) {
                        mListener.onMetadataChanged(mMusicProvider.getMusicByMusicId(currentPlayingId))
                    }
                }

                override fun onBitmapFailed(errorDrawable: Drawable?) {
                    val bitmap = ViewHelper.toBitmap(ViewHelper.createTextDrawable(metadata.description.title!!.toString()))
                    val icon = ViewHelper.createIcon(bitmap)
                    mMusicProvider.updateMusicArt(musicId, bitmap, icon)

                    // If we are still playing the same music, notify the listeners:
                    val currentPlayingId = MediaIDHelper.extractMusicIDFromMediaID(currentMusic?.description?.mediaId!!)
                    if (musicId == currentPlayingId) {
                        mListener.onMetadataChanged(mMusicProvider.getMusicByMusicId(currentPlayingId))
                    }
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            }
            ViewHelper.readBitmapAsync(mContext, albumUri, mTarget)
        }
    }

    override fun getPlayingQueueMetadata(): Iterable<MediaMetadataCompat> {
        return playingQueue!!.map { MediaItemHelper.convertToMetadata(it, currentMusic!!.description.mediaId) }
    }

    override fun getCurrentIndex(): Int {
        return mCurrentIndex
    }

    override fun getRepeatState(): RepeatState {
        return CustomController.repeatState
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
                Collections.shuffle(queueItemList)
                queueItemList.add(0, initialQueue)
            } else {
                Collections.shuffle(queueItemList)
            }
        }
    }
}
