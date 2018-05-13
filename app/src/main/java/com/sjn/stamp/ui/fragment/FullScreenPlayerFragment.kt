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

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import com.sjn.stamp.R
import com.sjn.stamp.controller.CustomController
import com.sjn.stamp.media.StampSession
import com.sjn.stamp.ui.activity.MusicPlayerListActivity
import com.sjn.stamp.ui.observer.MediaControllerObserver
import com.sjn.stamp.utils.AlbumArtHelper
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaControllerHelper
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * A full screen player that shows the current playing music with a background image
 * depicting the album art. The activity also has controls to seek/pause/play the audio.
 */
class FullScreenPlayerFragment : Fragment(), MediaControllerObserver.Listener {

    private var skipPrev: ImageView? = null
    private var skipNext: ImageView? = null
    private var playPause: ImageView? = null
    private var shuffle: ImageView? = null
    private var repeat: ImageView? = null
    private var start: TextView? = null
    private var end: TextView? = null
    private var seekBar: SeekBar? = null
    private var line1: TextView? = null
    private var line2: TextView? = null
    private var line3: TextView? = null
    private var loading: ProgressBar? = null
    private var pauseDrawable: Drawable? = null
    private var playDrawable: Drawable? = null
    private var noRepeatDrawable: Drawable? = null
    private var repeatOneDrawable: Drawable? = null
    private var repeatAllDrawable: Drawable? = null
    private var shuffleDrawable: Drawable? = null
    private var noShuffleDrawable: Drawable? = null
    private var backgroundImage: ImageView? = null

    private var currentArtUrl: String? = null
    private val handler = Handler()

    private val updateProgressTask = Runnable { updateProgress() }

    private val executorService = Executors.newSingleThreadScheduledExecutor()

    private var scheduleFuture: ScheduledFuture<*>? = null
    private var lastPlaybackState: PlaybackStateCompat? = null

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        LogHelper.i(TAG, "onPlaybackstate changed", state)
        updatePlaybackState(state)
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        metadata?.let {
            updateMediaDescription(it.description)
            updateDuration(it)
        }
    }

    override fun onSessionDestroyed() {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        LogHelper.i(TAG, "onCreateView")
        val rootView = inflater.inflate(R.layout.fragment_full_player, container, false)

        backgroundImage = rootView.findViewById(R.id.background_image)
        pauseDrawable = ContextCompat.getDrawable(activity!!, R.drawable.stamp_ic_pause_white_48dp)
        playDrawable = ContextCompat.getDrawable(activity!!, R.drawable.stamp_ic_play_arrow_white_48dp)
        repeatOneDrawable = ContextCompat.getDrawable(activity!!, R.drawable.ic_repeat_one_white_48dp)
        repeatAllDrawable = ContextCompat.getDrawable(activity!!, R.drawable.ic_repeat_white_48dp)
        noRepeatDrawable = ContextCompat.getDrawable(activity!!, R.drawable.ic_repeat_white_48dp)
        shuffleDrawable = ContextCompat.getDrawable(activity!!, R.drawable.ic_shuffle_white_48dp)
        noShuffleDrawable = ContextCompat.getDrawable(activity!!, R.drawable.ic_shuffle_white_48dp)
        noRepeatDrawable?.alpha = 50
        noShuffleDrawable?.alpha = 50
        playPause = rootView.findViewById(R.id.play_pause)
        skipNext = rootView.findViewById(R.id.next)
        skipPrev = rootView.findViewById(R.id.prev)
        repeat = rootView.findViewById(R.id.repeat)
        shuffle = rootView.findViewById(R.id.shuffle)
        start = rootView.findViewById(R.id.startText)
        end = rootView.findViewById(R.id.endText)
        seekBar = rootView.findViewById(R.id.seekBar1)
        line1 = rootView.findViewById(R.id.line1)
        line2 = rootView.findViewById(R.id.line2)
        line3 = rootView.findViewById(R.id.line3)
        loading = rootView.findViewById(R.id.progressBar1)

        skipNext?.setOnClickListener({
            MediaControllerHelper.getController(activity)?.transportControls?.skipToNext()
        })

        skipPrev?.setOnClickListener({
            MediaControllerHelper.getController(activity)?.transportControls?.skipToPrevious()
        })

        playPause?.setOnClickListener({
            MediaControllerHelper.getController(activity)?.let { controller ->
                controller.playbackState?.let { state ->
                    controller.transportControls?.let { controls ->
                        when (state.state) {
                            PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING -> {
                                controls.pause()
                                stopSeekBarUpdate()
                            }
                            PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> {
                                controls.play()
                                scheduleSeekBarUpdate()
                            }
                            else -> LogHelper.d(TAG, "onClick with state ", state.state)
                        }
                    }
                }
            }
        })

        repeat?.setImageDrawable(noRepeatDrawable)
        repeat?.setOnClickListener { CustomController.toggleRepeatMode(activity) }
        shuffle?.setOnClickListener { CustomController.toggleShuffleMode(activity) }

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                start?.text = DateUtils.formatElapsedTime((progress / 1000).toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                stopSeekBarUpdate()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                MediaControllerHelper.getController(activity)?.transportControls?.seekTo(seekBar.progress.toLong())
                scheduleSeekBarUpdate()
            }
        })

        // Only update from the intent if we are not recreating from a config change:
        if (savedInstanceState == null) {
            updateFromParams(activity?.intent)
        }
        return rootView
    }

    override fun onMediaControllerConnected() {
        MediaControllerHelper.getController(activity)?.let { controller ->
            controller.playbackState?.let { state ->
                updatePlaybackState(state)
                controller.metadata?.let { metadata ->
                    updateMediaDescription(metadata.description)
                    updateDuration(metadata)
                }
                updateProgress()
                if (state.state == PlaybackStateCompat.STATE_PLAYING || state.state == PlaybackStateCompat.STATE_BUFFERING) {
                    scheduleSeekBarUpdate()
                }
            }
        }
    }

    private fun updateFromParams(intent: Intent?) {
        intent?.getParcelableExtra<MediaDescriptionCompat>(MusicPlayerListActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION)?.let {
            updateMediaDescription(it)
        }
    }

    private fun scheduleSeekBarUpdate() {
        stopSeekBarUpdate()
        if (!executorService.isShutdown) {
            scheduleFuture = executorService.scheduleAtFixedRate(
                    { handler.post(updateProgressTask) }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS)
        }
    }

    private fun stopSeekBarUpdate() {
        scheduleFuture?.cancel(false)
    }

    override fun onStart() {
        LogHelper.i(TAG, "onStart")
        super.onStart()
        MediaControllerObserver.addListener(this)
        onMediaControllerConnected()
        onRepeatModeChanged(CustomController.getRepeatMode(activity))
        onShuffleModeChanged(CustomController.getShuffleMode(activity))
    }

    override fun onStop() {
        LogHelper.i(TAG, "onStop")
        super.onStop()
        MediaControllerObserver.removeListener(this)
    }

    override fun onDestroy() {
        LogHelper.i(TAG, "onDestroy")
        super.onDestroy()
        stopSeekBarUpdate()
        executorService.shutdown()
    }

    override fun onRepeatModeChanged(@PlaybackStateCompat.RepeatMode repeatMode: Int) {
        when (repeatMode) {
            PlaybackStateCompat.REPEAT_MODE_ONE -> repeat?.setImageDrawable(repeatOneDrawable)
            PlaybackStateCompat.REPEAT_MODE_ALL -> {
                repeatAllDrawable?.alpha = 255
                repeat?.setImageDrawable(repeatAllDrawable)
            }
            PlaybackStateCompat.REPEAT_MODE_NONE -> {
                repeatAllDrawable?.alpha = 50
                repeat?.setImageDrawable(repeatAllDrawable)
            }
        }
    }

    override fun onShuffleModeChanged(@PlaybackStateCompat.ShuffleMode shuffleMode: Int) {
        when (shuffleMode) {
            PlaybackStateCompat.SHUFFLE_MODE_ALL -> shuffleDrawable?.alpha = 255
            PlaybackStateCompat.SHUFFLE_MODE_NONE -> shuffleDrawable?.alpha = 50
        }
        shuffle?.setImageDrawable(shuffleDrawable)
    }


    private fun fetchImageAsync(description: MediaDescriptionCompat) {
        description.iconUri ?: return
        currentArtUrl = description.iconUri.toString()
        AlbumArtHelper.updateAlbumArt(activity, backgroundImage, currentArtUrl, description.title, 800, 800)
    }

    private fun updateMediaDescription(description: MediaDescriptionCompat?) {
        description ?: return
        LogHelper.d(TAG, "updateMediaDescription called ")
        line1?.text = description.title
        line2?.text = description.subtitle
        fetchImageAsync(description)
    }

    private fun updateDuration(metadata: MediaMetadataCompat?) {
        metadata ?: return
        LogHelper.d(TAG, "updateDuration called ")
        metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toInt().let { duration ->
            seekBar?.max = duration
            end?.text = DateUtils.formatElapsedTime((duration / 1000).toLong())
        }
    }

    private fun updatePlaybackState(state: PlaybackStateCompat?) {
        state ?: return
        lastPlaybackState = state
        MediaControllerHelper.getController(activity)?.let { controller ->
            line3?.text = controller.extras?.getString(StampSession.EXTRA_CONNECTED_CAST)?.let {
                resources.getString(R.string.casting_to_device, it)
            } ?: ""
        }

        when (state.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                loading?.visibility = INVISIBLE
                playPause?.visibility = VISIBLE
                playPause?.setImageDrawable(pauseDrawable)
                scheduleSeekBarUpdate()
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                loading?.visibility = INVISIBLE
                playPause?.visibility = VISIBLE
                playPause?.setImageDrawable(playDrawable)
                stopSeekBarUpdate()
            }
            PlaybackStateCompat.STATE_NONE, PlaybackStateCompat.STATE_STOPPED -> {
                loading?.visibility = INVISIBLE
                playPause?.visibility = VISIBLE
                playPause?.setImageDrawable(playDrawable)
                stopSeekBarUpdate()
            }
            PlaybackStateCompat.STATE_BUFFERING -> {
                playPause?.visibility = INVISIBLE
                loading?.visibility = VISIBLE
                line3?.setText(R.string.loading)
                stopSeekBarUpdate()
            }
            else -> LogHelper.d(TAG, "Unhandled state ", state.state)
        }

        skipNext?.visibility = if (state.actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT == 0L) INVISIBLE else VISIBLE
        skipPrev?.visibility = if (state.actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS == 0L) INVISIBLE else VISIBLE
    }

    private fun updateProgress() {
        lastPlaybackState?.let {
            var currentPosition = it.position
            if (it.state == PlaybackStateCompat.STATE_PLAYING) {
                // Calculate the elapsed time between the last position update and now and unless
                // paused, we can assume (delta * speed) + current position is approximately the
                // latest position. This ensure that we do not repeatedly call the getPlaybackState()
                // on MediaControllerCompat.
                currentPosition += (SystemClock.elapsedRealtime() - it.lastPositionUpdateTime.toInt() * it.playbackSpeed).toLong()
            }
            seekBar?.progress = currentPosition.toInt()
        }
    }

    companion object {
        private val TAG = LogHelper.makeLogTag(FullScreenPlayerFragment::class.java)
        private const val PROGRESS_UPDATE_INTERNAL: Long = 1000
        private const val PROGRESS_UPDATE_INITIAL_INTERVAL: Long = 100
    }
}
