package com.sjn.stamp.controller

import android.app.Activity
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.sjn.stamp.utils.MediaControllerHelper
import com.sjn.stamp.utils.PreferenceHelper


object CustomController {

    fun getShuffleMode(service: MediaBrowserServiceCompat?): Int = getShuffleMode(MediaControllerHelper.getController(service))

    fun getShuffleMode(activity: Activity?): Int = getShuffleMode(MediaControllerHelper.getController(activity))

    fun getRepeatMode(service: MediaBrowserServiceCompat?): Int = getRepeatMode(MediaControllerHelper.getController(service))

    fun getRepeatMode(activity: Activity?): Int = getRepeatMode(MediaControllerHelper.getController(activity))

    fun toggleRepeatMode(activity: Activity?) {
        when (getRepeatMode(activity)) {
            PlaybackStateCompat.REPEAT_MODE_NONE -> {
                PlaybackStateCompat.REPEAT_MODE_ALL
            }
            PlaybackStateCompat.REPEAT_MODE_ALL -> {
                PlaybackStateCompat.REPEAT_MODE_ONE
            }
            PlaybackStateCompat.REPEAT_MODE_ONE -> {
                PlaybackStateCompat.REPEAT_MODE_NONE
            }
            else -> {
                null
            }
        }?.let {
            setRepeatMode(activity, it)
        }
    }

    fun toggleShuffleMode(activity: Activity?) {
        when (getShuffleMode(activity)) {
            PlaybackStateCompat.SHUFFLE_MODE_NONE -> {
                PlaybackStateCompat.SHUFFLE_MODE_ALL
            }
            PlaybackStateCompat.SHUFFLE_MODE_ALL -> {
                PlaybackStateCompat.SHUFFLE_MODE_NONE
            }
            else -> {
                null
            }
        }?.let {
            setShuffleMode(activity, it)
        }
    }

    private fun getShuffleMode(controller: MediaControllerCompat?): Int {
        return controller?.shuffleMode
                ?: PlaybackStateCompat.SHUFFLE_MODE_INVALID.also { shuffleMode ->
                    if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_INVALID || shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_GROUP) {
                        return PlaybackStateCompat.SHUFFLE_MODE_NONE
                    }
                }
    }

    fun setShuffleMode(service: MediaBrowserServiceCompat, @PlaybackStateCompat.ShuffleMode shuffleMode: Int) {
        MediaControllerHelper.getController(service)?.transportControls?.setShuffleMode(shuffleMode).also {
            PreferenceHelper.saveShuffle(service, shuffleMode)
        }
    }

    fun setRepeatMode(service: MediaBrowserServiceCompat, @PlaybackStateCompat.RepeatMode repeatMode: Int) {
        MediaControllerHelper.getController(service)?.transportControls?.setRepeatMode(repeatMode).also {
            PreferenceHelper.saveRepeat(service, repeatMode)
        }
    }

    private fun setShuffleMode(activity: Activity?, @PlaybackStateCompat.ShuffleMode shuffleMode: Int) {
        MediaControllerHelper.getController(activity)?.transportControls?.setShuffleMode(shuffleMode)
        activity?.let {
            PreferenceHelper.saveShuffle(it, shuffleMode)
        }
    }

    private fun getRepeatMode(controller: MediaControllerCompat?): Int {
        return controller?.repeatMode
                ?: PlaybackStateCompat.REPEAT_MODE_INVALID.also { shuffleMode ->
                    if (shuffleMode == PlaybackStateCompat.REPEAT_MODE_INVALID || shuffleMode == PlaybackStateCompat.REPEAT_MODE_GROUP) {
                        return PlaybackStateCompat.REPEAT_MODE_NONE
                    }
                }
    }

    private fun setRepeatMode(activity: Activity?, @PlaybackStateCompat.RepeatMode repeatMode: Int) {
        MediaControllerHelper.getController(activity)?.transportControls?.setRepeatMode(repeatMode)
        activity?.let {
            PreferenceHelper.saveRepeat(it, repeatMode)
        }
    }

}