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
package com.sjn.stamp.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.sjn.stamp.R
import com.sjn.stamp.media.StampSession
import com.sjn.stamp.ui.observer.MediaControllerObserver
import com.sjn.stamp.utils.AlbumArtHelper
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaControllerHelper

/**
 * A class that shows the Media Queue to the user.
 */
class PlaybackControlsFragment : Fragment(), MediaControllerObserver.Listener {

    private var playPause: ImageButton? = null
    private var title: TextView? = null
    private var subtitle: TextView? = null
    private var extraInfo: TextView? = null
    private var albumArt: ImageView? = null
    private var currentArtUrl: String = ""

    private val buttonListener = View.OnClickListener { v ->
        activity?.let {
            val controller = MediaControllerHelper.getController(it)
                    ?: return@OnClickListener
            val state = controller.playbackState?.state ?: PlaybackStateCompat.STATE_NONE
            LogHelper.d(TAG, "Button pressed, in state $state")
            when (v.id) {
                R.id.play_pause -> {
                    LogHelper.d(TAG, "Play button pressed, in state $state")
                    if (state == PlaybackStateCompat.STATE_PAUSED ||
                            state == PlaybackStateCompat.STATE_STOPPED ||
                            state == PlaybackStateCompat.STATE_NONE) {
                        playMedia()
                    } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                            state == PlaybackStateCompat.STATE_BUFFERING ||
                            state == PlaybackStateCompat.STATE_CONNECTING) {
                        pauseMedia()
                    }
                }
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playback_controls, container, false).apply {
            playPause = findViewById(R.id.play_pause)
            playPause?.apply {
                isEnabled = true
                setOnClickListener(buttonListener)
            }
            title = findViewById(R.id.title)
            subtitle = findViewById(R.id.artist)
            extraInfo = findViewById(R.id.extra_info)
            albumArt = findViewById(R.id.album_art)
        }
    }

    override fun onStart() {
        super.onStart()
        LogHelper.d(TAG, "fragment.onStart")
        MediaControllerObserver.addListener(this)
        onMediaControllerConnected()
    }

    override fun onStop() {
        super.onStop()
        LogHelper.d(TAG, "fragment.onStop")
        MediaControllerObserver.removeListener(this)
    }

    override fun onMediaControllerConnected() {
        LogHelper.d(TAG, "onMediaControllerConnected")
        MediaControllerHelper.getController(activity)?.let {
            onMetadataChanged(it.metadata)
            onPlaybackStateChanged(it.playbackState)
        } ?: run {
            LogHelper.d(TAG, "media controller not found.")
        }
    }

    override fun onSessionDestroyed() {}

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        LogHelper.d(TAG, "onMetadataChanged ", metadata)
        if (activity == null) {
            LogHelper.w(TAG, "onMetadataChanged called when getActivity null," + "this should not happen if the callback was properly unregistered. Ignoring.")
            return
        }
        metadata ?: return
        title?.text = metadata.description.title
        subtitle?.text = metadata.description.subtitle
        fetchImageAsync(metadata.description)
    }

    private fun fetchImageAsync(description: MediaDescriptionCompat) {
        description.iconUri ?: return
        currentArtUrl = description.iconUri.toString()
        AlbumArtHelper.updateAlbumArt(activity, albumArt, currentArtUrl, description.title)
    }

    private fun updateExtraInfo(text: String?) {
        extraInfo?.let {
            if (text == null) {
                it.visibility = View.GONE
            } else {
                it.text = text
                it.visibility = View.VISIBLE
            }
        }
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        LogHelper.d(TAG, "onPlaybackStateChanged ", state)
        if (activity == null) {
            LogHelper.w(TAG, "onPlaybackStateChanged called when getActivity null," + "this should not happen if the callback was properly unregistered. Ignoring.")
            return
        }
        state ?: return
        var enablePlay = false
        when (state.state) {
            PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> {
                enablePlay = true
                playPause?.visibility = View.VISIBLE
            }
            PlaybackStateCompat.STATE_ERROR -> {
                LogHelper.e(TAG, "error playbackState: ", state.errorMessage)
                Toast.makeText(activity, state.errorMessage, Toast.LENGTH_LONG).show()
                playPause?.visibility = View.INVISIBLE
            }
            else -> playPause?.visibility = View.VISIBLE
        }
        activity?.let {
            playPause?.setImageDrawable(
                    ContextCompat.getDrawable(it, if (enablePlay) R.drawable.ic_play_arrow_black_36dp else R.drawable.ic_pause_black_36dp))
        }
        MediaControllerHelper.getController(activity)?.let { controller ->
            var text: String? = null
            controller.extras?.let {
                controller.extras.getString(StampSession.EXTRA_CONNECTED_CAST)?.let { castName ->
                    text = resources.getString(R.string.casting_to_device, castName)
                }
            }
            updateExtraInfo(text)
        }
    }

    private fun playMedia() {
        MediaControllerHelper.getController(activity)?.transportControls?.play()
    }

    private fun pauseMedia() {
        MediaControllerHelper.getController(activity)?.transportControls?.pause()
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(PlaybackControlsFragment::class.java)
    }
}
