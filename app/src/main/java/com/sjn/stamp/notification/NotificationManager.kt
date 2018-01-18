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

package com.sjn.stamp.notification

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
class NotificationManager @Throws(RemoteException::class) constructor(private val mService: MusicService) : MediaControllerObserver.Listener {

    companion object {
        private val TAG = LogHelper.makeLogTag(NotificationManager::class.java)
    }

    private var mSessionToken: MediaSessionCompat.Token? = null
    private var mController: MediaControllerCompat? = null

    private var mPlaybackState: PlaybackStateCompat? = null
    private var mMetadata: MediaMetadataCompat? = null
    private var mNotificationContainer: NotificationContainer? = null

    private var mStarted = false

    private var mReceiver: NotificationReceiver? = null


    init {
        updateSessionToken()
    }

    fun startForeground() {
        LogHelper.i(TAG, "startForeground")
        mNotificationContainer?.notification?.let {
            mService.startForeground(NotificationContainer.NOTIFICATION_ID, it)
        }
    }

    fun stopForeground(removeNotification: Boolean) {
        LogHelper.i(TAG, "stopForeground")
        mService.stopForeground(removeNotification)
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before [.stopNotification] is called.
     */
    fun startNotification() {
        LogHelper.i(TAG, "startNotification")
        if (!mStarted) {
            mMetadata = mController!!.metadata
            mPlaybackState = mController!!.playbackState
            if (mPlaybackState == null) {
                stopForeground(true)
                return
            }
            // The notification must be updated after setting started to true
            mNotificationContainer?.let {
                MediaControllerObserver.getInstance().addListener(this)
                mReceiver?.let {
                    mService.registerReceiver(it, NotificationAction.createIntentFilter())
                }
                it.create(mMetadata, mPlaybackState)
                it.start()
                startForeground()
                mStarted = true
            }
        }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    fun stopNotification() {
        LogHelper.i(TAG, "stopNotification")
        if (mStarted) {
            mStarted = false
            MediaControllerObserver.getInstance().removeListener(this)
            try {
                mNotificationContainer?.cancel()
                mReceiver?.let { mService.unregisterReceiver(it) }
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
        val freshToken = mService.sessionToken
        if (mSessionToken == null && freshToken != null || mSessionToken != null && mSessionToken != freshToken) {
            mController?.let {
                MediaControllerObserver.getInstance().removeListener(this)
            }
            mSessionToken = freshToken
            mSessionToken?.let {
                mController = MediaControllerCompat(mService, it)
                mReceiver = NotificationReceiver(mController!!)
                mNotificationContainer = NotificationContainer(mService, it, mController!!)
                if (mStarted) {
                    MediaControllerObserver.getInstance().addListener(this)
                }
            }
        }
    }

    override fun onMediaControllerConnected() = Unit

    override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
        LogHelper.d(TAG, "Received new playback state", state)
        mPlaybackState = state
        if (state.state == PlaybackStateCompat.STATE_STOPPED || state.state == PlaybackStateCompat.STATE_NONE) {
            stopNotification()
        } else {
            if (mPlaybackState == null || !mStarted) {
                stopForeground(true)
                return
            } else {
                mNotificationContainer?.let {
                    it.create(mMetadata, mPlaybackState)
                    it.start()
                }
            }
        }
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        LogHelper.d(TAG, "Received new metadata ", metadata)
        mMetadata = metadata
        if (mPlaybackState == null || !mStarted) {
            stopForeground(true)
            return
        } else {
            mNotificationContainer?.let {
                it.create(mMetadata, mPlaybackState)
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
