/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sjn.stamp.media.notification

import android.os.RemoteException
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.sjn.stamp.MusicService
import com.sjn.stamp.ui.observer.MediaControllerObserver
import com.sjn.stamp.utils.LogHelper

/**
 * Keeps track of a notification and updates it automatically for a given
 * MediaSession. Maintaining a visible notification (usually) guarantees that the music service
 * won't be killed during playback.
 */
class NotificationManager @Throws(RemoteException::class) constructor(private val service: MusicService) : MediaControllerObserver.Listener {

    companion object {
        private val TAG = LogHelper.makeLogTag(NotificationManager::class.java)
    }

    private var sessionToken: MediaSessionCompat.Token? = null
    private var controller: MediaControllerCompat? = null

    private var playbackState: PlaybackStateCompat? = null
    private var currentMetadata: MediaMetadataCompat? = null
    private var notificationContainer: NotificationContainer? = null

    private var started = false

    private var receiver: NotificationReceiver? = null


    init {
        updateSessionToken()
    }

    fun startForeground() {
        LogHelper.i(TAG, "startForeground")
        notificationContainer?.notification?.let {
            service.startForeground(NotificationContainer.NOTIFICATION_ID, it)
        }
    }

    fun stopForeground(removeNotification: Boolean) {
        LogHelper.i(TAG, "stopForeground")
        service.stopForeground(removeNotification)
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before [.stopNotification] is called.
     */
    fun startNotification() {
        LogHelper.i(TAG, "startNotification")
        if (!started) {
            currentMetadata = controller!!.metadata
            playbackState = controller!!.playbackState
            if (playbackState == null) {
                stopForeground(true)
                return
            }
            // The notification must be updated after setting started to true
            notificationContainer?.let {
                MediaControllerObserver.addListener(this)
                receiver?.let {
                    service.registerReceiver(it, NotificationAction.createIntentFilter())
                }
                it.create(currentMetadata, playbackState)
                it.start()
                startForeground()
                started = true
            }
        }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    fun stopNotification() {
        LogHelper.i(TAG, "stopNotification")
        if (started) {
            started = false
            MediaControllerObserver.removeListener(this)
            try {
                notificationContainer?.cancel()
                receiver?.let { service.unregisterReceiver(it) }
            } catch (ex: IllegalArgumentException) {
                // ignore if the receiver is not registered.
            }
            stopForeground(true)
        }
    }

    /**
     * Update the state based on a change on the session token. Called either when
     * we are running for the first time or when the media session owner has destroyed the session
     * (see [android.media.session.MediaController.Callback.onSessionDestroyed])
     */
    @Throws(RemoteException::class)
    private fun updateSessionToken() {
        LogHelper.i(TAG, "updateSessionToken")
        val freshToken = service.sessionToken
        if (sessionToken == null && freshToken != null || sessionToken != null && sessionToken != freshToken) {
            controller?.let {
                MediaControllerObserver.removeListener(this)
            }
            sessionToken = freshToken
            sessionToken?.let {
                controller = MediaControllerCompat(service, it)
                receiver = NotificationReceiver(controller!!)
                notificationContainer = NotificationContainer(service, it, controller!!)
                if (started) {
                    MediaControllerObserver.addListener(this)
                }
            }
        }
    }

    override fun onMediaControllerConnected() = Unit

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        LogHelper.d(TAG, "Received new playback state", state)
        playbackState = state
        if (playbackState?.state == PlaybackStateCompat.STATE_STOPPED || playbackState?.state == PlaybackStateCompat.STATE_NONE) {
            stopNotification()
        } else {
            if (playbackState == null || !started) {
                stopForeground(true)
                return
            } else {
                notificationContainer?.let {
                    it.create(currentMetadata, playbackState)
                    it.start()
                }
            }
        }
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        LogHelper.d(TAG, "Received new metadata ", metadata)
        currentMetadata = metadata
        if (playbackState == null || !started) {
            stopForeground(true)
            return
        } else {
            notificationContainer?.let {
                it.create(currentMetadata, playbackState)
                it.start()
            }
        }
    }

    override fun onSessionDestroyed() {
        LogHelper.d(TAG, "Session was destroyed, resetting to the new session token")
        try {
            updateSessionToken()
        } catch (e: RemoteException) {
            LogHelper.e(TAG, e, "could not connect media controller")
        }

    }
}
