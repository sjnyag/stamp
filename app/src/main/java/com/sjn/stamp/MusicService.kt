package com.sjn.stamp

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.sjn.stamp.media.playback.PlaybackManager
import com.sjn.stamp.media.player.CastPlayer
import com.sjn.stamp.media.player.Player
import com.sjn.stamp.media.provider.MusicProvider
import com.sjn.stamp.media.source.LocalMediaSource
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.NotificationHelper.*
import java.util.*

class MusicService : MediaBrowserServiceCompat(), PlaybackManager.PlaybackServiceCallback {

    companion object {
        private val TAG = LogHelper.makeLogTag(MusicService::class.java)
        const val CUSTOM_ACTION_RELOAD_MUSIC_PROVIDER = "RELOAD_MUSIC_PROVIDER"
        const val CUSTOM_ACTION_SET_QUEUE = "SET_QUEUE"
        const val CUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_TITLE = "SET_QUEUE_BUNDLE_KEY_TITLE"
        const val CUSTOM_ACTION_SET_QUEUE_BUNDLE_MEDIA_ID = "SET_QUEUE_BUNDLE_MEDIA_ID"
        const val CUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_QUEUE = "SET_QUEUE_BUNDLE_KEY_QUEUE"
    }

    private lateinit var mMusicProvider: MusicProvider
    private var mMediaNotificationManager: MediaNotificationManager? = null
    private var mPlayer: Player? = null
    private var mCastPlayer: CastPlayer? = null

    override fun onCreate() {
        super.onCreate()
        LogHelper.d(TAG, "onCreate")
        mMusicProvider = MusicProvider(this, LocalMediaSource(this, {}))
        mMusicProvider.retrieveMediaAsync {
            LogHelper.d(TAG, "MusicProvider.callBack start")
            mPlayer = Player(this@MusicService)
            sessionToken = mPlayer!!.initialize(this@MusicService, mMusicProvider)
            Thread(Runnable {
                mPlayer?.restorePreviousState(mMusicProvider)
                mCastPlayer = CastPlayer(this@MusicService, mPlayer?.sessionManageListener)
                try {
                    mMediaNotificationManager = MediaNotificationManager(this@MusicService)
                } catch (e: RemoteException) {
                    throw IllegalStateException("Could not create a MediaNotificationManager", e)
                }
                LogHelper.d(TAG, "MusicProvider.callBack end")
            }).start()
        }
    }

    override fun onStartCommand(startIntent: Intent?, flags: Int, startId: Int): Int {
        LogHelper.d(TAG, "onStartCommand")
        if (startIntent != null) {
            val action = startIntent.action
            val command = startIntent.getStringExtra(CMD_NAME)
            if (ACTION_CMD == action) {
                handleActionCommand(command)
            } else {
                mPlayer?.handleIntent(startIntent)
            }
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        LogHelper.d(TAG, "onDestroy")
        mPlayer?.stop()
        mMediaNotificationManager?.stopNotification()
        mCastPlayer?.finish()
    }

    /**
     * [MediaBrowserServiceCompat]
     */
    override fun onCustomAction(action: String, extras: Bundle?,
                                result: MediaBrowserServiceCompat.Result<Bundle>) {
        LogHelper.d(TAG, "onCustomAction " + action)
        when (action) {
            CUSTOM_ACTION_RELOAD_MUSIC_PROVIDER -> {
                result.detach()
                mMusicProvider.cacheAndNotifyLatestMusicMap()
                return
            }
            CUSTOM_ACTION_SET_QUEUE -> {
                result.detach()
                extras?.let {
                    mPlayer?.startNewQueue(extras.getString(CUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_TITLE), extras.getString(CUSTOM_ACTION_SET_QUEUE_BUNDLE_MEDIA_ID), extras.getParcelableArrayList(CUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_QUEUE))
                }
                return
            }
        }
        result.sendError(null)
    }

    override fun onSearch(query: String, extras: Bundle?,
                          result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
        LogHelper.d(TAG, "onSearch " + query)
        result.detach()
        if (mMusicProvider.isInitialized) {
            Thread(Runnable { result.sendResult(mMusicProvider.getChildren(query, resources)) }).start()
        }
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int,
                           rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        LogHelper.d(TAG, "OnGetRoot: clientPackageName=" + clientPackageName, "; clientUid=$clientUid ; rootHints=", rootHints)
        return MediaBrowserServiceCompat.BrowserRoot(MediaIDHelper.MEDIA_ID_ROOT, null)
    }

    override fun onLoadChildren(parentMediaId: String,
                                result: MediaBrowserServiceCompat.Result<List<MediaItem>>) {
        LogHelper.d(TAG, "OnLoadChildren: parentMediaId=", parentMediaId)
        if (MediaIDHelper.MEDIA_ID_EMPTY_ROOT == parentMediaId) {
            result.sendResult(ArrayList<MediaItem>())
        } else {
            result.detach()
            if (mMusicProvider.isInitialized) {
                Thread(Runnable { result.sendResult(mMusicProvider.getChildren(parentMediaId, resources)) }).start()
            }
        }
    }

    /**
     * [PlaybackManager.PlaybackServiceCallback]
     */
    override fun onPlaybackStart() {
        mPlayer?.setActive(true)
        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(Intent(applicationContext, MusicService::class.java))
        mMediaNotificationManager?.startForeground()
    }

    override fun onPlaybackStop() {
        mPlayer?.setActive(false)
        mMediaNotificationManager?.stopForeground(true)
    }

    override fun onPlaybackStateUpdated(newState: PlaybackStateCompat) {
        mPlayer?.setPlaybackState(newState)
    }

    override fun onNotificationRequired() {
        mMediaNotificationManager?.startNotification()
    }

    private fun handleActionCommand(command: String) {
        when (command) {
            CMD_PAUSE -> mPlayer?.pause()
            CMD_STOP_CASTING -> mCastPlayer?.stop()
            CMD_KILL -> if (mPlayer != null) stopSelf()
        }
    }

}
