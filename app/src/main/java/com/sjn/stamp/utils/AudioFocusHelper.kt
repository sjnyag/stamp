package com.sjn.stamp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager

object AudioFocusHelper {
    private val TAG = LogHelper.makeLogTag(AudioFocusHelper::class.java)

    // The volume we set the media player to when we lose audio focus, but are
    // allowed to reduce the volume instead of stopping playback.
    val VOLUME_DUCK = 0.2f
    // The volume we set the media player when we have audio focus.
    val VOLUME_NORMAL = 1.0f
    // we don't have audio focus, and can't duck (play at a low volume)
    val AUDIO_NO_FOCUS_NO_DUCK = 0
    // we don't have focus, but can duck (play at a low volume)
    val AUDIO_NO_FOCUS_CAN_DUCK = 1
    // we have full audio focus
    val AUDIO_FOCUSED = 2

    class AudioFocusManager(private val context: Context, private val listener: Listener) : AudioManager.OnAudioFocusChangeListener {
        interface Listener {
            fun onHeadphonesDisconnected()
            fun onAudioFocusChange()
            fun isMediaPlayerPlaying(): Boolean
        }

        var playOnFocusGain: Boolean = false

        // Type of audio focus we have:
        var audioFocus = AUDIO_NO_FOCUS_NO_DUCK
        private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        private var audioNoisyReceiverRegistered: Boolean = false
        private val audioNoisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        private val audioNoisyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                    LogHelper.d(TAG, "Headphones disconnected.")
                    listener.onHeadphonesDisconnected()
                }
            }
        }

        fun start() {
            if (!audioNoisyReceiverRegistered) {
                context.registerReceiver(audioNoisyReceiver, audioNoisyIntentFilter)
                audioNoisyReceiverRegistered = true
            }
        }

        fun stop() {
            // Give up Audio focus
            if (audioNoisyReceiverRegistered) {
                context.unregisterReceiver(audioNoisyReceiver)
                audioNoisyReceiverRegistered = false
            }
        }

        /**
         * Try to get the system audio focus.
         */
        fun tryToGetAudioFocus() {
            LogHelper.d(TAG, "tryToGetAudioFocus")
            val result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN)
            audioFocus = if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                AUDIO_FOCUSED
            } else {
                AUDIO_NO_FOCUS_NO_DUCK
            }
        }

        /**
         * Give up the audio focus.
         */
        fun giveUpAudioFocus() {
            LogHelper.d(TAG, "giveUpAudioFocus")
            if (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocus = AUDIO_NO_FOCUS_NO_DUCK
            }
        }

        /**
         * Called by AudioManager on audio focus changes.
         * Implementation of [android.media.AudioManager.OnAudioFocusChangeListener]
         */
        override fun onAudioFocusChange(focusChange: Int) {
            LogHelper.d(TAG, "onAudioFocusChange. focusChange=", focusChange)
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // We have gained focus:
                audioFocus = AUDIO_FOCUSED

            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // We have lost focus. If we can duck (low playback volume), we can keep playing.
                // Otherwise, we need to pause the playback.
                val canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
                audioFocus = if (canDuck) AUDIO_NO_FOCUS_CAN_DUCK else AUDIO_NO_FOCUS_NO_DUCK

                // If we are playing, we need to reset media player by calling configMediaPlayerState
                // with audioFocus properly set.
                if (listener.isMediaPlayerPlaying() && !canDuck) {
                    // If we don't have audio focus and can't duck, we create the information that
                    // we were playing, so that we can resume playback once we get the focus back.
                    playOnFocusGain = true
                }
            } else {
                LogHelper.e(TAG, "onAudioFocusChange: Ignoring unsupported focusChange: ", focusChange)
            }
            listener.onAudioFocusChange()
        }

    }

}