package com.sjn.stamp.utils


import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
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

    private var sColorStatePlaying: ColorStateList? = null
    private var sColorStateNotPlaying: ColorStateList? = null

    fun getDrawableByState(context: Context, state: Int): Drawable? {
        if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
            initializeColorStateLists(context)
        }

        when (state) {
            STATE_PLAYABLE -> {
                val pauseDrawable = ContextCompat.getDrawable(context,
                        R.drawable.ic_play_arrow_black_36dp)
                DrawableCompat.setTintList(pauseDrawable!!, sColorStateNotPlaying)
                return pauseDrawable
            }
            STATE_PLAYING -> {
                val animation = ContextCompat.getDrawable(context, R.drawable.ic_equalizer_white_36dp) as AnimationDrawable?
                DrawableCompat.setTintList(animation!!, sColorStatePlaying)
                animation.start()
                return animation
            }
            STATE_PAUSED -> {
                val playDrawable = ContextCompat.getDrawable(context,
                        R.drawable.ic_equalizer1_white_36dp)
                DrawableCompat.setTintList(playDrawable!!, sColorStatePlaying)
                return playDrawable
            }
            else -> return null
        }
    }

    fun getMediaItemState(activity: Activity, mediaId: String, isPlayable: Boolean): Int =
            if (isPlayable) getMediaItemState(activity, mediaId) else STATE_PLAYABLE

    private fun getMediaItemState(activity: Activity, mediaId: String): Int =
            if (MediaIDHelper.isMediaItemPlaying(activity, mediaId)) getStateFromController(activity) else STATE_PLAYABLE

    private fun initializeColorStateLists(ctx: Context) {
        sColorStateNotPlaying = ColorStateList.valueOf(ctx.resources.getColor(
                R.color.media_item_icon))
        sColorStatePlaying = ColorStateList.valueOf(ctx.resources.getColor(
                R.color.media_item_icon))
    }

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
