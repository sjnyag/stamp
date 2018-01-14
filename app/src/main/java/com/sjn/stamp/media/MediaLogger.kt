package com.sjn.stamp.media

import android.os.Handler
import android.support.v4.media.session.PlaybackStateCompat

class MediaLogger(private val listener: Listener?) {

    interface Listener {

        val currentMediaId: String?

        val playbackState: Int

        val currentPosition: Int

        fun onSongStart(mediaId: String)

        fun onSongPlay(mediaId: String)

        fun onSongSkip(mediaId: String)

        fun onSongComplete(mediaId: String)
    }

    fun onStart() {
        if (listener == null) {
            return
        }
        resetTimerHandler()
        timerHandler?.postDelayed(MediaStartTimer(), START_WAIT_TIME)
    }

    fun onComplete(mediaId: String?) {
        if (listener == null || mediaId == null) {
            return
        }
        listener.onSongComplete(mediaId)
    }

    fun onSkip(mediaId: String?, position: Int) {
        if (listener == null || mediaId == null) {
            return
        }
        if (position in (START_WAIT_TIME + 1)..(PLAY_WAIT_TIME - 1)) {
            listener.onSongSkip(mediaId)
        }
    }

    private fun resetTimerHandler() {
        if (timerHandler == null) {
            timerHandler = Handler()
        } else {
            timerHandler?.removeCallbacksAndMessages(null)
        }
    }

    private inner class MediaStartTimer : Runnable {

        override fun run() {
            if (listener?.currentMediaId != null && timerHandler != null && isStart(listener.playbackState, listener.currentPosition)) {
                listener.onSongStart(listener.currentMediaId!!)
                resetTimerHandler()
                timerHandler!!.postDelayed(MediaPlayTimer(listener.currentMediaId), PLAY_WAIT_TIME - START_WAIT_TIME)
            }
        }
    }

    private inner class MediaPlayTimer internal constructor(internal val mStartMediaId: String?) : Runnable {

        override fun run() {
            if (listener?.currentMediaId != null && isSongPlaying(listener.playbackState)
                    && mStartMediaId != null && mStartMediaId == listener.currentMediaId) {
                listener.onSongPlay(listener.currentMediaId!!)
            }
        }
    }

    companion object {

        private val START_WAIT_TIME = 2000L
        private val START_LIMIT_TIME = 5000L
        private val PLAY_WAIT_TIME = 20000L
        private var timerHandler: Handler? = null

        private fun isStart(state: Int, position: Int): Boolean {
            return isPlaying(state) && position < START_LIMIT_TIME
        }

        private fun isSongPlaying(state: Int): Boolean {
            return isPlaying(state)
        }

        private fun isPlaying(state: Int): Boolean {
            return state == PlaybackStateCompat.STATE_PLAYING
        }
    }

}
