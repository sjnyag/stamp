package com.sjn.stamp.media

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v7.media.MediaRouter
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener
import com.sjn.stamp.ui.activity.IntentDispatchActivity
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.TvHelper

class StampSession internal constructor(context: Context, callback: MediaSessionCompat.Callback, private val sessionListener: SessionListener?) : MediaSessionCompat(context, "MusicService") {

    private val mediaRouter: MediaRouter = MediaRouter.getInstance(context.applicationContext)
    private val sessionExtras: Bundle = Bundle()
    private var castSessionManager: SessionManager? = null

    internal interface SessionListener {
        fun toLocalPlayback()

        fun toCastCallback()

        fun onSessionEnd()
    }

    init {
        // Start a new MediaSession
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
        initCastSession(context)
    }

    private val castSessionCallback: SessionManagerListener<CastSession> = object : SessionManagerListener<CastSession> {
        /**
         * Session Manager Listener responsible for switching the Playback instances
         * depending on whether it is connected to a remote player.
         */
        override fun onSessionEnded(castSession: CastSession, error: Int) {
            LogHelper.i(TAG, "onSessionEnded error: ", error)
            sessionListener?.let {
                sessionExtras.remove(EXTRA_CONNECTED_CAST)
                setExtras(sessionExtras)
                mediaRouter.setMediaSessionCompat(null)
                it.toLocalPlayback()
            }
        }

        override fun onSessionResumed(castSession: CastSession, wasSuspended: Boolean) {}

        // In case we are casting, send the device name as an extra on MediaSession metadata.
        override fun onSessionStarted(castSession: CastSession, sessionId: String) {
            LogHelper.i(TAG, "onSessionStarted sessionId: ", sessionId)
            sessionListener?.let {
                sessionExtras.putString(EXTRA_CONNECTED_CAST, castSession.castDevice.friendlyName)
                setExtras(sessionExtras)
                mediaRouter.setMediaSessionCompat(this@StampSession)
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
            sessionListener?.onSessionEnd()
        }

        override fun onSessionResuming(castSession: CastSession, sessionId: String) {}

        override fun onSessionResumeFailed(castSession: CastSession, error: Int) {}

        override fun onSessionSuspended(castSession: CastSession, reason: Int) {}
    }


    fun updateQueue(newQueue: List<MediaSessionCompat.QueueItem>, title: String) {
        setQueue(newQueue)
        setQueueTitle(title)
    }

    override fun release() {
        super.release()
        castSessionManager?.removeSessionManagerListener(castSessionCallback, CastSession::class.java)
    }

    fun handleIntent(startIntent: Intent) {
        MediaButtonReceiver.handleIntent(this, startIntent)
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
