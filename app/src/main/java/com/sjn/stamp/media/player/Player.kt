package com.sjn.stamp.media.player


import android.content.Context
import android.content.Intent
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.sjn.stamp.R
import com.sjn.stamp.controller.UserSettingController
import com.sjn.stamp.media.playback.Playback
import com.sjn.stamp.media.provider.MusicProvider
import com.sjn.stamp.utils.MediaControllerHelper

class Player(private val context: Context, callback: PlaybackManager.PlaybackServiceCallback, musicProvider: MusicProvider) : StampSession.SessionListener {

    val sessionToken: MediaSessionCompat.Token?
        get() = sessionManager.sessionToken
    private val queueManager: QueueManager
    private val playbackManager: PlaybackManager
    private val sessionManager: StampSession

    init {
        queueManager = QueueManager(context, musicProvider, QueueUpdateListener())
        playbackManager = PlaybackManager(context, callback, queueManager, Playback.Type.LOCAL)
        sessionManager = StampSession(context, playbackManager.mediaSessionCallback, this)
    }

    fun restorePreviousState() {
        queueManager.restorePreviousState(UserSettingController().lastMusicId, UserSettingController().queueIdentifyMediaId)
        playbackManager.updatePlaybackState(null)
    }

    fun stop() {
        MediaControllerHelper.getController(context, sessionToken)?.transportControls?.stop()
        sessionManager.release()
    }

    fun pause() {
        MediaControllerHelper.getController(context, sessionToken)?.transportControls?.pause()
    }

    fun handleIntent(startIntent: Intent) {
        sessionManager.handleIntent(startIntent)
    }

    fun setActive(active: Boolean) {
        sessionManager.setActive(active)
    }

    fun setPlaybackState(playbackState: PlaybackStateCompat) {
        sessionManager.setPlaybackState(playbackState)
    }

    fun startNewQueue(title: String, mediaId: String, queueItemList: List<MediaSessionCompat.QueueItem>) {
        playbackManager.startNewQueue(title, mediaId, queueItemList)
    }

    fun stopCasting() {
        sessionManager.stopCasting(context)
    }

    override fun toLocalPlayback() {
        playbackManager.switchToPlayback(Playback.Type.LOCAL, false)
    }

    override fun toCastCallback() {
        playbackManager.switchToPlayback(Playback.Type.CAST, true)
    }

    override fun onSessionEnd() {
        playbackManager.playback.updateLastKnownStreamPosition()
    }

    private inner class QueueUpdateListener : QueueManager.MetadataUpdateListener {
        override fun onMetadataChanged(metadata: MediaMetadataCompat) =
                sessionManager.setMetadata(metadata)

        override fun onMetadataRetrieveError() {
            playbackManager.updatePlaybackState(context.getString(R.string.error_no_metadata))
        }

        override fun onCurrentQueueIndexUpdated(queueIndex: Int) {
            MediaControllerHelper.getController(context, sessionToken)?.transportControls?.play()
        }

        override fun onQueueUpdated(title: String, newQueue: List<MediaSessionCompat.QueueItem>) =
                sessionManager.updateQueue(newQueue, title)
    }
}
