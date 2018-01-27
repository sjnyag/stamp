package com.sjn.stamp.media.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.media.MediaRouter
import com.google.android.gms.cast.framework.CastContext

import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener
import com.sjn.stamp.ui.activity.IntentDispatchActivity
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.TvHelper

class StampSession internal constructor(context: Context, callback: MediaSessionCompat.Callback, private val mSessionListener: SessionListener?) {

    private val mediaRouter: MediaRouter = MediaRouter.getInstance(context.applicationContext)
    private val sessionExtras: Bundle = Bundle()
    private val session: MediaSessionCompat
    private var castSessionManager: SessionManager? = null

    internal interface SessionListener {
        fun toLocalPlayback()

        fun toCastCallback()

        fun onSessionEnd()
    }

    init {
        // Start a new MediaSession
        session = MediaSessionCompat(context, "MusicService").apply {
            setCallback(callback)
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setSessionActivity(
                    PendingIntent.getActivity(
                            context.applicationContext,
                            99 /*request code*/,
                            Intent(context.applicationContext, IntentDispatchActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT)
            )
            setExtras(sessionExtras)
        }
        initCastSession(context)
    }

    private val castSessionCallback = object : SessionManagerListener<CastSession> {
        /**
         * Session Manager Listener responsible for switching the Playback instances
         * depending on whether it is connected to a remote player.
         */
        override fun onSessionEnded(castSession: CastSession, error: Int) {
            LogHelper.i(TAG, "onSessionEnded error: ", error)
            mSessionListener?.let {
                sessionExtras.remove(EXTRA_CONNECTED_CAST)
                session.setExtras(sessionExtras)
                mediaRouter.setMediaSessionCompat(null)
                it.toLocalPlayback()
            }
        }

        override fun onSessionResumed(castSession: CastSession, wasSuspended: Boolean) {}

        // In case we are casting, send the device name as an extra on MediaSession metadata.
        override fun onSessionStarted(castSession: CastSession, sessionId: String) {
            LogHelper.i(TAG, "onSessionStarted sessionId: ", sessionId)
            mSessionListener?.let {
                sessionExtras.putString(EXTRA_CONNECTED_CAST, castSession.castDevice.friendlyName)
                session.setExtras(sessionExtras)
                mediaRouter.setMediaSessionCompat(session)
                it.toCastCallback()
            }
        }

        override fun onSessionStarting(castSession: CastSession) {}

        override fun onSessionStartFailed(castSession: CastSession, error: Int) {}

        // This is our final chance to update the underlying stream position
        // In onSessionEnded(), the underlying CastPlayback#remoteMediaClient
        // is disconnected and hence we update our local value of stream position
        // to the latest position.
        override fun onSessionEnding(castSession: CastSession) {
            mSessionListener?.onSessionEnd()
        }

        override fun onSessionResuming(castSession: CastSession, sessionId: String) {}

        override fun onSessionResumeFailed(castSession: CastSession, error: Int) {}

        override fun onSessionSuspended(castSession: CastSession, reason: Int) {}
    }

    val sessionToken: MediaSessionCompat.Token?
        get() = session.sessionToken

    fun setMetadata(metadata: MediaMetadataCompat) {
        session.setMetadata(metadata)
    }

    fun updateQueue(newQueue: List<MediaSessionCompat.QueueItem>, title: String) {
        session.setQueue(newQueue)
        session.setQueueTitle(title)
    }

    fun handleIntent(startIntent: Intent) {
        MediaButtonReceiver.handleIntent(session, startIntent)
    }

    fun release() {
        session.release()
        castSessionManager?.removeSessionManagerListener(castSessionCallback, CastSession::class.java)
    }

    fun setActive(active: Boolean) {
        session.isActive = active
    }

    fun setPlaybackState(playbackState: PlaybackStateCompat) {
        session.setPlaybackState(playbackState)
    }

    fun stopCasting(context: Context) {
        CastContext.getSharedInstance(context).sessionManager.endCurrentSession(true)
    }

    private fun initCastSession(context: Context) {
        if (!TvHelper.isTvUiMode(context)) {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                try {
                    castSessionManager = CastContext.getSharedInstance(context).sessionManager
                    castSessionManager?.addSessionManagerListener(castSessionCallback, CastSession::class.java)
                } catch (e: RuntimeException) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {

        // Extra on MediaSession that contains the Cast device name currently connected to
        const val EXTRA_CONNECTED_CAST = "com.sjn.stamp.CAST_NAME"
        private val TAG = LogHelper.makeLogTag(StampSession::class.java)
    }
}
