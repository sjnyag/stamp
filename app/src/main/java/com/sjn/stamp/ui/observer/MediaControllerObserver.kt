package com.sjn.stamp.ui.observer

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.sjn.stamp.utils.LogHelper
import java.util.*

object MediaControllerObserver : MediaControllerCompat.Callback() {

    private val TAG = LogHelper.makeLogTag(MediaControllerObserver::class.java)
    private val listenerList = Collections.synchronizedList(ArrayList<Listener>())

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
        LogHelper.i(TAG, "notifyConnected ", listenerList?.size)
        for (listener in ArrayList(listenerList)) {
            listener.onMediaControllerConnected()
        }
    }

    fun addListener(listener: Listener) {
        LogHelper.i(TAG, "addListener")
        if (!listenerList.contains(listener)) {
            listenerList.add(listener)
        }
    }

    fun removeListener(listener: Listener) {
        LogHelper.i(TAG, "removeListener")
        if (listenerList.contains(listener)) {
            listenerList.remove(listener)
        }
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
        LogHelper.i(TAG, "onPlaybackStateChanged ", listenerList?.size)
        for (listener in ArrayList(listenerList)) {
            listener.onPlaybackStateChanged(state)
        }
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        LogHelper.i(TAG, "onMetadataChanged ", listenerList?.size)
        for (listener in ArrayList(listenerList)) {
            listener.onMetadataChanged(metadata)
        }
    }


    override fun onSessionDestroyed() {
        super.onSessionDestroyed()
        LogHelper.i(TAG, "onSessionDestroyed ", listenerList?.size)
        for (listener in ArrayList(listenerList)) {
            listener.onSessionDestroyed()
        }
    }

}
