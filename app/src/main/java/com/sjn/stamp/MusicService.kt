package com.sjn.stamp

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaControllerCompat
import com.sjn.stamp.media.notification.NotificationManager
import com.sjn.stamp.media.playback.Playback
import com.sjn.stamp.media.playback.PlaybackManager
import com.sjn.stamp.media.provider.MusicProvider
import com.sjn.stamp.media.source.LocalMediaSource
import com.sjn.stamp.ui.observer.MediaControllerObserver
import com.sjn.stamp.utils.*
import java.util.*

class MusicService : MediaBrowserServiceCompat(), PlaybackManager.PlaybackServiceCallback {

    companion object {
        private val TAG = LogHelper.makeLogTag(MusicService::class.java)
        const val NOTIFICATION_CMD_PLAY = "CMD_PLAY"
        const val NOTIFICATION_CMD_PAUSE = "CMD_PAUSE"
        const val NOTIFICATION_CMD_STOP_CASTING = "CMD_STOP_CASTING"
        const val NOTIFICATION_CMD_KILL = "CMD_KILL"
        const val CUSTOM_ACTION_RELOAD_MUSIC_PROVIDER = "RELOAD_MUSIC_PROVIDER"
        const val CUSTOM_ACTION_SET_QUEUE = "SET_QUEUE"
        const val CUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_TITLE = "SET_QUEUE_BUNDLE_KEY_TITLE"
        const val CUSTOM_ACTION_SET_QUEUE_BUNDLE_MEDIA_ID = "SET_QUEUE_BUNDLE_MEDIA_ID"
        const val CUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_QUEUE = "SET_QUEUE_BUNDLE_KEY_QUEUE"
    }

    private var playOnPrepared = false

    private var playbackManager: PlaybackManager? = null
    private var mediaController: MediaControllerCompat? = null

    private val notificationManager = NotificationManager(this)
    private val musicProvider = MusicProvider(this,
            LocalMediaSource(this, object : MediaRetrieveHelper.PermissionRequiredCallback {
                override fun onPermissionRequired() {
                }
            }))

    override fun onCreate() {
        super.onCreate()
        LogHelper.d(TAG, "onCreate")
        musicProvider.retrieveMediaAsync(object : MusicProvider.Callback {
            override fun onMusicCatalogReady(success: Boolean) {
                LogHelper.d(TAG, "MusicProvider.callBack start")
                playbackManager = PlaybackManager(this@MusicService, this@MusicService, musicProvider, Playback.Type.LOCAL).apply {
                    restorePreviousState()
                }
                sessionToken = playbackManager?.sessionToken
                sessionToken?.let {
                    mediaController = MediaControllerCompat(this@MusicService, it).apply {
                        MediaControllerObserver.register(this)
                    }
                }
                PlayModeHelper.restore(this@MusicService)
                try {
                    notificationManager.updateSessionToken()
                } catch (e: RemoteException) {
                    throw IllegalStateException("Could not create a NotificationManager", e)
                }
                if (playOnPrepared) {
                    playOnPrepared = false
                    mediaController?.transportControls?.play()
                }
                LogHelper.d(TAG, "MusicProvider.callBack end")
            }
        })
    }

    override fun onDestroy() {
        LogHelper.d(TAG, "onDestroy")
        mediaController?.let {
            it.transportControls.stop()
            MediaControllerObserver.unregister(it)
        }
        notificationManager.stopNotification()
    }

    override fun onStartCommand(startIntent: Intent?, flags: Int, startId: Int): Int {
        LogHelper.d(TAG, "onStartCommand")
        startIntent?.let {
            if (NotificationHelper.ACTION_CMD == it.action) {
                handleActionCommand(it.getStringExtra(NotificationHelper.CMD_NAME))
            } else {
                playbackManager?.handleIntent(it)
            }
        }
        return Service.START_NOT_STICKY
    }

    /**
     * [MediaBrowserServiceCompat]
     */
    override fun onCustomAction(action: String, extras: Bundle?,
                                result: MediaBrowserServiceCompat.Result<Bundle>) {
        LogHelper.d(TAG, "onCustomAction $action")
        when (action) {
            CUSTOM_ACTION_RELOAD_MUSIC_PROVIDER -> {
                result.detach()
                musicProvider.cacheAndNotifyLatestMusicMap()
                return
            }
            CUSTOM_ACTION_SET_QUEUE -> {
                result.detach()
                extras?.let {
                    playbackManager?.startNewQueue(
                            extras.getString(CUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_TITLE),
                            extras.getString(CUSTOM_ACTION_SET_QUEUE_BUNDLE_MEDIA_ID),
                            extras.getParcelableArrayList(CUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_QUEUE))
                }
                return
            }
        }
        result.sendError(null)
    }

    override fun onSearch(query: String, extras: Bundle?,
                          result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
        LogHelper.d(TAG, "onSearch $query")
        result.detach()
        if (musicProvider.isInitialized) {
            Thread(Runnable { result.sendResult(musicProvider.getChildren(query, resources)) }).start()
        }
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int,
                           rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        LogHelper.d(TAG, "OnGetRoot: clientPackageName=$clientPackageName", "; clientUid=$clientUid ; rootHints=", rootHints)
        return MediaBrowserServiceCompat.BrowserRoot(MediaIDHelper.MEDIA_ID_ROOT, null)
    }

    override fun onLoadChildren(parentMediaId: String,
                                result: MediaBrowserServiceCompat.Result<List<MediaItem>>) {
        LogHelper.d(TAG, "OnLoadChildren: parentMediaId=", parentMediaId)
        if (MediaIDHelper.MEDIA_ID_EMPTY_ROOT == parentMediaId) {
            result.sendResult(ArrayList())
        } else {
            result.detach()
            if (musicProvider.isInitialized) {
                Thread(Runnable { result.sendResult(musicProvider.getChildren(parentMediaId, resources)) }).start()
            }
        }
    }

    /**
     * [PlaybackManager.PlaybackServiceCallback]
     */
    override fun onPlaybackStart() {
        LogHelper.d(TAG, "onPlaybackStart")
        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startForegroundServiceCompatible(Intent(applicationContext, MusicService::class.java))
        notificationManager.startForeground()
    }

    override fun onPlaybackStop() {
        LogHelper.d(TAG, "onPlaybackStop")
        notificationManager.stopForeground(false)
    }

    override fun onNotificationRequired() {
        LogHelper.d(TAG, "onNotificationRequired")
        notificationManager.startNotification()
    }

    private fun handleActionCommand(command: String) {
        LogHelper.d(TAG, "handleActionCommand ", command)
        when (command) {
            NOTIFICATION_CMD_PLAY -> mediaController?.transportControls?.play()
                    ?: run { playOnPrepared = true }
            NOTIFICATION_CMD_PAUSE -> mediaController?.transportControls?.pause()
            NOTIFICATION_CMD_STOP_CASTING -> playbackManager?.stopCasting()
            NOTIFICATION_CMD_KILL -> playbackManager?.let { stopSelf() }
        }
    }

}
