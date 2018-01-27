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
import android.support.v4.media.session.MediaSessionCompat.QueueItem
import com.sjn.stamp.utils.MediaPlayerHelper

/**
 * A class that implements local media playback using [android.media.MediaPlayer]
 */
internal class LocalPlayback(context: Context, private val callback: Playback.Callback, initialStreamPosition: Int, initialMediaId: String?) : Playback, MediaPlayerHelper.MediaPlayerManager.Listener {

    private val mediaPlayerManager = MediaPlayerHelper.MediaPlayerManager(context, initialStreamPosition, initialMediaId, this)

    override var state: Int
        get() = mediaPlayerManager.state
        set(value) {
            mediaPlayerManager.state = value
        }
    override val isConnected: Boolean = true
    override val isPlaying: Boolean get() = mediaPlayerManager.isPlaying
    override val currentStreamPosition: Int get() = mediaPlayerManager.currentStreamPosition
    override val currentMediaId: String? get() = mediaPlayerManager.currentMediaId

    override fun start() {}

    override fun stop(notifyListeners: Boolean) {
        mediaPlayerManager.stop(notifyListeners)
    }

    override fun updateLastKnownStreamPosition() {
        mediaPlayerManager.updateLastKnownStreamPosition()
    }

    override fun play(item: QueueItem) {
        mediaPlayerManager.play(item)
    }

    override fun pause() {
        mediaPlayerManager.pause()
    }

    override fun seekTo(position: Int) {
        mediaPlayerManager.seekTo(position)
    }

    override fun onError(error: String) {
        callback.onError(error)
    }

    override fun onCompletion() {
        callback.onCompletion()
    }

    override fun onPlaybackStatusChanged(state: Int) {
        callback.onPlaybackStatusChanged(state)
    }
}
