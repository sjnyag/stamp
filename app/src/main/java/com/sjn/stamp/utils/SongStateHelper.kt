package com.sjn.stamp.utils


import android.app.Activity
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat

import com.sjn.stamp.R

@Suppress("unused")
object SongStateHelper {

    private const val STATE_INVALID = -1
    private const val STATE_NONE = 0
    private const val STATE_PLAYABLE = 1
    private const val STATE_PAUSED = 2
    private const val STATE_PLAYING = 3

    fun getDrawableByState(context: Context, state: Int): Drawable? {

        when (state) {
            STATE_PLAYABLE -> {
                return ContextCompat.getDrawable(context,
                        R.drawable.ic_play_arrow_black_36dp)
            }
            STATE_PLAYING -> {
                val animation = ContextCompat.getDrawable(context, R.drawable.ic_equalizer_white_36dp) as AnimationDrawable?
                animation?.start()
                return animation
            }
            STATE_PAUSED -> {
                return ContextCompat.getDrawable(context,
                        R.drawable.ic_equalizer1_white_36dp)
            }
            else -> return null
        }
    }

    fun getMediaItemState(activity: Activity, mediaId: String, isPlayable: Boolean): Int =
            if (isPlayable) getMediaItemState(activity, mediaId) else STATE_PLAYABLE

    private fun getMediaItemState(activity: Activity, mediaId: String): Int =
            if (MediaIDHelper.isMediaItemPlaying(activity, mediaId)) getStateFromController(activity) else STATE_PLAYABLE

    private fun getStateFromController(activity: Activity): Int {
        val pbState = MediaControllerCompat.getMediaController(activity).playbackState
        return if (pbState == null || pbState.state == PlaybackStateCompat.STATE_ERROR) {
            STATE_NONE
        } else if (pbState.state == PlaybackStateCompat.STATE_PLAYING) {
            STATE_PLAYING
        } else {
            STATE_PAUSED
        }
    }
}
