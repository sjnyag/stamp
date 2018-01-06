package com.sjn.stamp.utils

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import com.sjn.stamp.MusicService
import com.sjn.stamp.controller.UserSettingController
import java.io.IOException

object MediaPlayerHelper {
    private val TAG = LogHelper.makeLogTag(MediaPlayerHelper::class.java)

    class MediaPlayerManager(private val context: Context, initialStreamPosition: Int, var currentMediaId: String?, private val listener: Listener) :
            OnCompletionListener, OnErrorListener, OnPreparedListener, OnSeekCompleteListener, AudioFocusHelper.AudioFocusManager.Listener {

        interface Listener {
            fun onError(error: String)
            fun onCompletion()
            fun onPlaybackStatusChanged(state: Int)
        }

        private val audioManager = AudioFocusHelper.AudioFocusManager(context, this)
        private var currentPosition: Int = initialStreamPosition
        private var mediaPlayer: MediaPlayer? = null

        var state: Int = 0
        val isPlaying: Boolean get() = audioManager.playOnFocusGain || (mediaPlayer != null && mediaPlayer?.isPlaying ?: false)
        val currentStreamPosition: Int get() = mediaPlayer?.currentPosition ?: currentPosition

        fun play(item: MediaSessionCompat.QueueItem) {
            audioManager.playOnFocusGain = true
            audioManager.tryToGetAudioFocus()
            audioManager.start()
            val mediaId = item.description.mediaId
            val mediaHasChanged = !TextUtils.equals(mediaId, currentMediaId)
            if (mediaHasChanged || state != PlaybackStateCompat.STATE_PAUSED) {
                currentPosition = 0
                currentMediaId = mediaId
            }

            if (state == PlaybackStateCompat.STATE_PAUSED && !mediaHasChanged && mediaPlayer != null) {
                configMediaPlayerState()
            } else if (item.description.mediaUri == null) {
                state = PlaybackStateCompat.STATE_ERROR
                listener.onPlaybackStatusChanged(state)
                listener.onError("Media not found.")
            } else {
                state = PlaybackStateCompat.STATE_STOPPED
                val source = item.description.mediaUri!!.toString()
                try {
                    createMediaPlayerIfNeeded()

                    state = PlaybackStateCompat.STATE_BUFFERING

                    mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    if (!DataSourceHelper.setMediaPlayerDataSource(context, mediaPlayer, source)) {
                        listener.onError("Failed to retrieve media.")
                        return
                    }

                    // Starts preparing the media player in the background. When
                    // it's done, it will call our OnPreparedListener (that is,
                    // the onPrepared() method on this class, since we set the
                    // listener to 'this'). Until the media player is prepared,
                    // we *cannot* call start() on it!
                    mediaPlayer?.prepareAsync()

                    listener.onPlaybackStatusChanged(state)

                } catch (ex: IOException) {
                    LogHelper.e(TAG, ex, "Exception playing song")
                    ex.message?.let { listener.onError(it) }
                }

            }
        }

        fun pause() {
            if (state == PlaybackStateCompat.STATE_PLAYING) {
                // Pause media player and cancel the 'foreground service' state.
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.pause()
                        currentPosition = it.currentPosition
                    }
                }
            }
            state = PlaybackStateCompat.STATE_PAUSED
            listener.onPlaybackStatusChanged(state)
            audioManager.stop()
            audioManager.giveUpAudioFocus()
        }

        fun stop(notifyListeners: Boolean) {
            state = PlaybackStateCompat.STATE_STOPPED
            if (notifyListeners) listener.onPlaybackStatusChanged(state)
            currentPosition = currentStreamPosition
            audioManager.stop()
            audioManager.giveUpAudioFocus()
            // Relax all resources
            relaxResources()
        }

        fun seekTo(position: Int) {
            LogHelper.d(TAG, "seekTo called with ", position)

            if (mediaPlayer == null) {
                // If we do not have a current media player, simply update the current position
                currentPosition = position
            } else {
                if (mediaPlayer!!.isPlaying) {
                    state = PlaybackStateCompat.STATE_BUFFERING
                }
                audioManager.start()
                mediaPlayer!!.seekTo(position)
                listener.onPlaybackStatusChanged(state)
            }
        }

        fun updateLastKnownStreamPosition() {
            mediaPlayer?.let { currentPosition = it.currentPosition }
        }

        /**
         * Called when MediaPlayer has completed a seek
         *
         * @see OnSeekCompleteListener
         */
        override fun onSeekComplete(player: MediaPlayer) {
            LogHelper.d(TAG, "onSeekComplete from MediaPlayer:", player.currentPosition)
            currentPosition = player.currentPosition
            if (state == PlaybackStateCompat.STATE_BUFFERING) {
                audioManager.start()
                mediaPlayer!!.start()
                state = PlaybackStateCompat.STATE_PLAYING
            }
            listener.onPlaybackStatusChanged(state)
        }

        /**
         * Called when media player is done playing current song.
         *
         * @see OnCompletionListener
         */
        override fun onCompletion(player: MediaPlayer) {
            LogHelper.d(TAG, "onCompletion from MediaPlayer")
            // The media player finished playing the current song, so we go ahead
            // and start the next.
            listener.onCompletion()
        }

        /**
         * Called when media player is done preparing.
         *
         * @see OnPreparedListener
         */
        override fun onPrepared(player: MediaPlayer) {
            LogHelper.d(TAG, "onPrepared from MediaPlayer")
            // The media player is done preparing. That means we can start playing if we
            // have audio focus.
            configMediaPlayerState()
        }

        /**
         * Called when there's an error playing media. When this happens, the media
         * player goes to the Error state. We warn the user about the error and
         * reset the media player.
         *
         * @see OnErrorListener
         */
        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            LogHelper.e(TAG, "Media player error: what=$what, extra=$extra")
            listener.onError("MediaPlayer error $what ($extra)")
            return true // true indicates we handled the error
        }

        override fun onHeadphonesDisconnected() {
            if (isPlaying) {
                context.startService(Intent(context, MusicService::class.java).apply {
                    action = NotificationHelper.ACTION_CMD
                    putExtra(NotificationHelper.CMD_NAME, NotificationHelper.CMD_PAUSE)
                })
            }
        }

        override fun onAudioFocusChange() {
            configMediaPlayerState()
        }

        override fun isMediaPlayerPlaying(): Boolean = state == PlaybackStateCompat.STATE_PLAYING

        /**
         * Reconfigures MediaPlayer according to audio focus settings and
         * starts/restarts it. This method starts/restarts the MediaPlayer
         * respecting the current audio focus state. So if we have focus, it will
         * play normally; if we don't have focus, it will either leave the
         * MediaPlayer paused or set it to a low volume, depending on what is
         * allowed by the current focus settings. This method assumes mPlayer !=
         * null, so if you are calling it, you have to do so from a context where
         * you are sure this is the case.
         */
        private fun configMediaPlayerState() {
            LogHelper.d(TAG, "configMediaPlayerState. audioFocus=", audioManager.audioFocus)
            if (audioManager.audioFocus == AudioFocusHelper.AUDIO_NO_FOCUS_NO_DUCK) {
                // If we don't have audio focus and can't duck, we have to pause,
                if (state == PlaybackStateCompat.STATE_PLAYING && UserSettingController().stopOnAudioLostFocus()) {
                    pause()
                }
            } else {  // we have audio focus:
                audioManager.start()
                if (audioManager.audioFocus == AudioFocusHelper.AUDIO_NO_FOCUS_CAN_DUCK) {
                    mediaPlayer!!.setVolume(AudioFocusHelper.VOLUME_DUCK, AudioFocusHelper.VOLUME_DUCK) // we'll be relatively quiet
                } else {
                    if (mediaPlayer != null) {
                        mediaPlayer!!.setVolume(AudioFocusHelper.VOLUME_NORMAL, AudioFocusHelper.VOLUME_NORMAL) // we can be loud again
                    } // else do something for remote client.
                }
                // If we were playing when we lost focus, we need to resume playing.
                if (audioManager.playOnFocusGain) {
                    if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                        LogHelper.d(TAG, "configMediaPlayerState startMediaPlayer. seeking to ",
                                currentPosition)
                        state = if (currentPosition == mediaPlayer!!.currentPosition) {
                            mediaPlayer!!.start()
                            PlaybackStateCompat.STATE_PLAYING
                        } else {
                            mediaPlayer!!.seekTo(currentPosition)
                            PlaybackStateCompat.STATE_BUFFERING
                        }
                    }
                    audioManager.playOnFocusGain = false
                }
            }
            listener.onPlaybackStatusChanged(state)
        }

        /**
         * Makes sure the media player exists and has been reset. This will create
         * the media player if needed, or reset the existing media player if one
         * already exists.
         */
        private fun createMediaPlayerIfNeeded() {
            LogHelper.d(TAG, "createMediaPlayerIfNeeded. needed? ", mediaPlayer == null)
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    // Make sure the media player will acquire a wake-lock while
                    // playing. If we don't do that, the CPU might go to sleep while the
                    // song is playing, causing playback to stop.
                    setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

                    // we want the media player to notify us when it's ready preparing,
                    // and when it's done playing:
                    setOnPreparedListener(this@MediaPlayerManager)
                    setOnCompletionListener(this@MediaPlayerManager)
                    setOnErrorListener(this@MediaPlayerManager)
                    setOnSeekCompleteListener(this@MediaPlayerManager)
                }
            } else {
                mediaPlayer?.reset()
            }
        }

        /**
         * Releases resources used by the service for playback. This includes the
         * "foreground service" status, the wake locks and possibly the MediaPlayer.
         */
        private fun relaxResources() {
            LogHelper.d(TAG, "relaxResources.")
            // stop and release the Media Player
            mediaPlayer?.let {
                it.reset()
                it.release()
            }
            mediaPlayer = null
        }
    }

}