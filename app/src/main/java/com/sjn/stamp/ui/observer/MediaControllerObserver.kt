package com.sjn.stamp.ui.observer

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.sjn.stamp.utils.LogHelper
import java.util.*

object MediaControllerObserver : MediaControllerCompat.Callback() {

    private val TAG = LogHelper.makeLogTag(MediaControllerObserver::class.java)
    private val mListenerList = Collections.synchronizedList(ArrayList<Listener>())

    interface Listener {
        fun onPlaybackStateChanged(state: PlaybackStateCompat?)

        fun onMetadataChanged(metadata: MediaMetadataCompat?)

        fun onMediaControllerConnected()

        fun onSessionDestroyed()
    }

    fun register(mediaController: MediaControllerCompat) {
        mediaController.registerCallback(this)
    }

    fun unregister(mediaController: MediaControllerCompat) {
        mediaController.unregisterCallback(this)
    }

    fun notifyConnected() {
        LogHelper.i(TAG, "notifyConnected ", mListenerList?.size)
        for (listener in ArrayList(mListenerList)) {
            listener.onMediaControllerConnected()
        }
    }

    fun addListener(listener: Listener) {
        LogHelper.i(TAG, "addListener")
        if (!mListenerList.contains(listener)) {
            mListenerList.add(listener)
        }
    }

    fun removeListener(listener: Listener) {
        LogHelper.i(TAG, "removeListener")
        if (mListenerList.contains(listener)) {
            mListenerList.remove(listener)
        }
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
        LogHelper.i(TAG, "onPlaybackStateChanged ", mListenerList?.size)
        for (listener in ArrayList(mListenerList)) {
            listener.onPlaybackStateChanged(state)
        }
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        LogHelper.i(TAG, "onMetadataChanged ", mListenerList?.size)
        for (listener in ArrayList(mListenerList)) {
            listener.onMetadataChanged(metadata)
        }
    }


    override fun onSessionDestroyed() {
        super.onSessionDestroyed()
        LogHelper.i(TAG, "onSessionDestroyed ", mListenerList?.size)
        for (listener in ArrayList(mListenerList)) {
            listener.onSessionDestroyed()
        }
    }

}
