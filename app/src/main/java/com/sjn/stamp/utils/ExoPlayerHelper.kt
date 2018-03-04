package com.sjn.stamp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC
import com.google.android.exoplayer2.C.USAGE_MEDIA
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.sjn.stamp.MusicService
import com.sjn.stamp.controller.UserSettingController


object ExoPlayerHelper {
    private val TAG = LogHelper.makeLogTag(ExoPlayerHelper::class.java)

    // The volume we set the media player to when we lose audio focus, but are
    // allowed to reduce the volume instead of stopping playback.
    private const val VOLUME_DUCK = 0.2f
    // The volume we set the media player when we have audio focus.
    private const val VOLUME_NORMAL = 1.0f

    class ExoPlayerManager(private val context: Context, initialStreamPosition: Int, var currentMediaId: String?, private val listener: Listener) :
            Player.EventListener, AudioFocusHelper.AudioFocusManager.Listener {

        interface Listener {
            fun onError(error: String)
            fun onCompletion()
            fun onPlaybackStatusChanged(state: Int)
        }

        private val audioManager = AudioFocusHelper.AudioFocusManager(context, this)
        private var mediaPlayer: SimpleExoPlayer? = null

        val state: Int
            get() =
                mediaPlayer?.let {
                    when (mediaPlayer?.playbackState) {
                        Player.STATE_IDLE -> PlaybackStateCompat.STATE_PAUSED
                        Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                        Player.STATE_READY -> if (mediaPlayer?.playWhenReady == true)
                            PlaybackStateCompat.STATE_PLAYING
                        else
                            PlaybackStateCompat.STATE_PAUSED
                        Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
                        else -> PlaybackStateCompat.STATE_NONE
                    }
                } ?: PlaybackStateCompat.STATE_STOPPED
        val isPlaying: Boolean
            get() = audioManager.playOnFocusGain || (mediaPlayer?.playWhenReady ?: false)
        val currentStreamPosition: Long get() = mediaPlayer?.currentPosition ?: 0L

        fun play(item: MediaSessionCompat.QueueItem) {
            if (item.description.mediaUri == null) {
                listener.onError("Media not found.")
                return
            }
            val isSameMedia = TextUtils.equals(item.description.mediaId, currentMediaId)
            if (!isSameMedia || state != PlaybackStateCompat.STATE_PAUSED) {
                currentMediaId = item.description.mediaId
            }
            audioManager.start()
            if (state == PlaybackStateCompat.STATE_PAUSED && isSameMedia && mediaPlayer != null) {
                configMediaPlayerState()
                return
            }
            createMediaPlayerIfNeeded()
            mediaPlayer?.let {
                item.description.mediaUri?.toString()?.let { path ->

                    // Android "O" makes much greater use of AudioAttributes, especially
                    // with regards to AudioFocus. All of UAMP's tracks are music, but
                    // if your content includes spoken word such as audiobooks or podcasts
                    // then the content type should be set to CONTENT_TYPE_SPEECH for those
                    // tracks.
                    it.audioAttributes = AudioAttributes.Builder()
                            .setContentType(CONTENT_TYPE_MUSIC)
                            .setUsage(USAGE_MEDIA)
                            .build()

                    // Produces DataSource instances through which media data is loaded.
                    val dataSourceFactory = DefaultDataSourceFactory(
                            context, Util.getUserAgent(context, "Stamp"), null)
                    // Produces Extractor instances for parsing the media data.
                    val extractorsFactory = DefaultExtractorsFactory()
                    // The MediaSource represents the media to be played.
                    val mediaSource = ExtractorMediaSource(
                            Uri.parse(path), dataSourceFactory, extractorsFactory, null, null)

                    // Prepares media to play (happens on background thread) and triggers
                    // {@code onPlayerStateChanged} callback when the stream is ready to play.
                    it.playWhenReady = true
                    it.prepare(mediaSource)
                }
            }
        }

        fun pause() {
            mediaPlayer?.playWhenReady = false
            audioManager.releaseReceiver()
            audioManager.giveUpAudioFocus()
        }

        fun stop() {
            audioManager.releaseReceiver()
            audioManager.giveUpAudioFocus()
            // Relax all resources
            relaxResources()
        }

        fun seekTo(position: Long) {
            LogHelper.d(TAG, "seekTo called with ", position)

            mediaPlayer?.let {
                audioManager.setUpReceiver()
                it.seekTo(position)
            }
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
            LogHelper.d(TAG, "configMediaPlayerState.")
            if (audioManager.hasFocus()) {  // we have audio focus:
                audioManager.setUpReceiver()
                mediaPlayer?.changeVolume(audioManager.noFocusCanDuck())
                // If we were playing when we lost focus, we need to resume playing.
                if (audioManager.playOnFocusGain) {
                    mediaPlayer?.playWhenReady = true
                    audioManager.playOnFocusGain = false
                }
            } else if (state == PlaybackStateCompat.STATE_PLAYING && UserSettingController().stopOnAudioLostFocus()) {
                // If we don't have audio focus and can't duck, we have to pause,
                pause()
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
                mediaPlayer = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector()).apply {
                    addListener(this@ExoPlayerManager)
                }
            }
        }

        /**
         * Releases resources used by the service for playback. This includes the
         * "foreground service" status, the wake locks and possibly the MediaPlayer.
         */
        private fun relaxResources() {
            LogHelper.d(TAG, "relaxResources.")
            // stop and release the Media Player
            mediaPlayer?.apply {
                release()
                removeListener(this@ExoPlayerManager)
            }
            mediaPlayer = null
        }

        private fun SimpleExoPlayer.changeVolume(duck: Boolean) {
            volume = if (duck) {
                VOLUME_DUCK // we'll be relatively quiet
            } else {
                VOLUME_NORMAL // we can be loud again
            }
        }

        override fun onPositionDiscontinuity(reason: Int) {
            // Nothing to do.
        }

        override fun onSeekProcessed() {
            // Nothing to do.
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            // Nothing to do.
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            // Nothing to do.
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
            // Nothing to do.
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            // Nothing to do.
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            LogHelper.i(TAG, "ExoPlayer onPlayerStateChanged: playWhenReady=" + playWhenReady, ", playbackState=", playbackState)
            when (playbackState) {
                Player.STATE_IDLE, Player.STATE_BUFFERING, Player.STATE_READY ->
                    listener.onPlaybackStatusChanged(state)
                Player.STATE_ENDED ->
                    // The media player finished playing the current song.
                    listener.onCompletion()
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            val what: String? = when (error.type) {
                ExoPlaybackException.TYPE_SOURCE -> error.sourceException.message
                ExoPlaybackException.TYPE_RENDERER -> error.rendererException.message
                ExoPlaybackException.TYPE_UNEXPECTED -> error.unexpectedException.message
                else -> "Unknown: " + error
            }
            LogHelper.e(TAG, "ExoPlayer error: what=" + what)
            listener.onError("ExoPlayer error " + what)
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            // Nothing to do.
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            // Nothing to do.
        }
    }

}