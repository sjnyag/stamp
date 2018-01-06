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
package com.sjn.stamp.media.playback

import android.content.Context
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat.QueueItem
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.MediaItemHelper
import org.json.JSONException
import org.json.JSONObject

/**
 * An implementation of Playback that talks to Cast.
 */
open class CastPlayback(internal val context: Context, private val callback: Playback.Callback, initialStreamPosition: Int, override var currentMediaId: String?) : Playback {

    internal val remoteMediaClient: RemoteMediaClient = CastContext.getSharedInstance(this.context).sessionManager.currentCastSession.remoteMediaClient
    private val remoteMediaClientListener: RemoteMediaClient.Listener = CastMediaClientListener()
    private var currentPosition: Int = initialStreamPosition

    override var state: Int = 0
    override val isConnected: Boolean get() = CastContext.getSharedInstance(context).sessionManager.currentCastSession?.isConnected ?: false
    override val isPlaying: Boolean get() = isConnected && remoteMediaClient.isPlaying
    override val currentStreamPosition: Int get() = if (!isConnected) currentPosition else remoteMediaClient.approximateStreamPosition.toInt()

    override fun start() = remoteMediaClient.addListener(remoteMediaClientListener)

    override fun stop(notifyListeners: Boolean) {
        remoteMediaClient.removeListener(remoteMediaClientListener)
        state = PlaybackStateCompat.STATE_STOPPED
        if (notifyListeners) callback.onPlaybackStatusChanged(state)
    }

    override fun updateLastKnownStreamPosition() {
        currentPosition = currentStreamPosition
    }

    override fun play(item: QueueItem) {
        playItem(item)
        state = PlaybackStateCompat.STATE_BUFFERING
        callback.onPlaybackStatusChanged(state)
    }

    override fun pause() {
        if (remoteMediaClient.hasMediaSession()) {
            remoteMediaClient.pause()
            currentPosition = remoteMediaClient.approximateStreamPosition.toInt()
        }
    }

    override fun seekTo(position: Int) {
        if (currentMediaId == null) {
            callback.onError("seekTo cannot be calling in the absence of mediaId.")
            return
        }
        if (remoteMediaClient.hasMediaSession()) {
            remoteMediaClient.seek(position.toLong())
            currentPosition = position
        }
    }

    open fun playItem(item: QueueItem) {
        send(item, true, item.description.mediaUri.toString(), Uri.Builder().encodedPath(item.description.iconUri.toString()).build())
    }

    @Throws(JSONException::class)
    protected fun send(item: QueueItem, autoPlay: Boolean, mediaUri: String, iconUri: Uri) {
        val mediaId = item.description.mediaId
        if (mediaId == null || mediaId.isEmpty()) {
            throw IllegalArgumentException("Invalid mediaId")
        }
        val musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId)
        if (musicId == null || musicId.isEmpty()) {
            throw IllegalArgumentException("Invalid mediaId")
        }
        if (!TextUtils.equals(mediaId, currentMediaId) || state != PlaybackStateCompat.STATE_PAUSED) {
            currentMediaId = mediaId
            currentPosition = 0
        }
        val customData = JSONObject()
        try {
            customData.put(ITEM_ID, mediaId)
        } catch (e: JSONException) {
            LogHelper.e(TAG, "Exception loading media ", e, null)
            e.message?.let { callback.onError(it) }
        }
        remoteMediaClient.load(MediaItemHelper.convertToMediaInfo(
                item, customData, mediaUri, iconUri), autoPlay, currentPosition.toLong(), customData)
    }

    private fun setMetadataFromRemote() {
        // Sync: We get the customData from the remote media information and update the local
        // metadata if it happens to be different from the one we are currently using.
        // This can happen when the app was either restarted/disconnected + connected, or if the
        // app joins an existing session while the Chromecast was playing a queue.
        try {
            val mediaInfo = remoteMediaClient.mediaInfo ?: return
            val customData = mediaInfo.customData

            if (customData != null && customData.has(ITEM_ID)) {
                val remoteMediaId = customData.getString(ITEM_ID)
                if (!TextUtils.equals(currentMediaId, remoteMediaId)) {
                    currentMediaId = remoteMediaId
                    callback.setCurrentMediaId(remoteMediaId)
                    updateLastKnownStreamPosition()
                }
            }
        } catch (e: JSONException) {
            LogHelper.e(TAG, e, "Exception processing update metadata")
        }

    }

    private fun updatePlaybackState() {
        val status = remoteMediaClient.playerState

        LogHelper.d(TAG, "onRemoteMediaPlayerStatusUpdated ", status)

        // Convert the remote playback states to media playback states.
        when (status) {
            MediaStatus.PLAYER_STATE_IDLE -> if (remoteMediaClient.idleReason == MediaStatus.IDLE_REASON_FINISHED) {
                callback.onCompletion()
            }
            MediaStatus.PLAYER_STATE_BUFFERING -> {
                state = PlaybackStateCompat.STATE_BUFFERING
                callback.onPlaybackStatusChanged(state)
            }
            MediaStatus.PLAYER_STATE_PLAYING -> {
                state = PlaybackStateCompat.STATE_PLAYING
                setMetadataFromRemote()
                callback.onPlaybackStatusChanged(state)
            }
            MediaStatus.PLAYER_STATE_PAUSED -> {
                state = PlaybackStateCompat.STATE_PAUSED
                setMetadataFromRemote()
                callback.onPlaybackStatusChanged(state)
            }
            else // case unknown
            -> LogHelper.d(TAG, "State default : ", status)
        }
    }

    private inner class CastMediaClientListener : RemoteMediaClient.Listener {
        private var lastStatus = -1

        override fun onMetadataUpdated() {
            LogHelper.d(TAG, "RemoteMediaClient.onMetadataUpdated")
            setMetadataFromRemote()
        }

        override fun onStatusUpdated() {
            LogHelper.d(TAG, "RemoteMediaClient.onStatusUpdated")
            if (lastStatus != remoteMediaClient.playerState) {
                updatePlaybackState()
            }
            lastStatus = remoteMediaClient.playerState
        }

        override fun onSendingRemoteMediaRequest() {}

        override fun onAdBreakStatusUpdated() {}

        override fun onQueueStatusUpdated() {}

        override fun onPreloadStatusUpdated() {}
    }

    companion object {
        private val TAG = LogHelper.makeLogTag(CastPlayback::class.java)
        val ITEM_ID = "itemId"
    }
}
