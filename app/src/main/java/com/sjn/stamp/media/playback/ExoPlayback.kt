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
import com.sjn.stamp.utils.ExoPlayerHelper

/**
 * A class that implements local media playback using [android.media.MediaPlayer]
 */
internal class ExoPlayback(context: Context, private val callback: Playback.Callback, initialStreamPosition: Int, initialMediaId: String?) : Playback, ExoPlayerHelper.ExoPlayerManager.Listener {

    private val playerManager = ExoPlayerHelper.ExoPlayerManager(context, initialStreamPosition, initialMediaId, this)

    override val state: Int
        get() = playerManager.state
    override val isConnected: Boolean = true
    override val isPlaying: Boolean get() = playerManager.isPlaying
    override val currentStreamPosition: Int get() = playerManager.currentStreamPosition.toInt()
    override val currentMediaId: String? get() = playerManager.currentMediaId

    override fun start() {}

    override fun stop(notifyListeners: Boolean) {
        playerManager.stop()
    }

    override fun updateLastKnownStreamPosition() {}

    override fun play(item: QueueItem) {
        playerManager.play(item)
    }

    override fun pause() {
        playerManager.pause()
    }

    override fun seekTo(position: Int) {
        playerManager.seekTo(position.toLong())
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
