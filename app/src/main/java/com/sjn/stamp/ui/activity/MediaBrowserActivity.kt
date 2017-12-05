package com.sjn.stamp.ui.activity

import android.content.ComponentName
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.sjn.stamp.MusicService
import com.sjn.stamp.ui.MediaBrowsable
import com.sjn.stamp.ui.observer.MediaControllerObserver
import com.sjn.stamp.utils.LogHelper

abstract class MediaBrowserActivity : DrawerActivity(), MediaBrowsable, MediaControllerObserver.Listener {

    private var mMediaBrowser: MediaBrowserCompat? = null
    private val mediaController: MediaControllerCompat?
        get() = MediaControllerCompat.getMediaController(this)

    private val mConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            try {
                LogHelper.i(TAG, "onMediaControllerConnected")
                MediaControllerCompat.setMediaController(this@MediaBrowserActivity, MediaControllerCompat(this@MediaBrowserActivity, mMediaBrowser!!.sessionToken))
                MediaControllerObserver.register(mediaController)
                MediaControllerObserver.getInstance().notifyConnected()
            } catch (e: RemoteException) {
                LogHelper.e(TAG, e, "could not connect media controller")
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogHelper.i(TAG, "Activity onCreate")
        MediaControllerObserver.getInstance().addListener(this)
        mMediaBrowser = MediaBrowserCompat(this, ComponentName(this, MusicService::class.java), mConnectionCallback, null)
        mMediaBrowser?.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogHelper.i(TAG, "Activity onDestroy")
        MediaControllerObserver.getInstance().removeListener(this)
        mediaController?.let {
            MediaControllerObserver.unregister(it)
        }
        mMediaBrowser?.disconnect()
    }

    override fun sendCustomAction(action: String, extras: Bundle, callback: MediaBrowserCompat.CustomActionCallback) {
        if (mMediaBrowser != null && mMediaBrowser!!.isConnected) {
            mMediaBrowser!!.sendCustomAction(action, extras, callback)
        }
    }

    override fun getMediaBrowser(): MediaBrowserCompat? = mMediaBrowser

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {}

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        LogHelper.d(TAG, "onMetadataChanged")
        mDrawer?.updateHeader(metadata)
    }

    override fun onMediaControllerConnected() {
        LogHelper.d(TAG, "onMediaControllerConnected, mediaController==null? ", mediaController == null)
        mediaController?.let {
            onMetadataChanged(it.metadata)
            onPlaybackStateChanged(it.playbackState)
        }
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(MediaBrowserActivity::class.java)
    }

}
