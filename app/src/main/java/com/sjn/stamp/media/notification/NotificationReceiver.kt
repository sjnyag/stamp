package com.sjn.stamp.media.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaControllerCompat


class NotificationReceiver constructor(private val controller: MediaControllerCompat) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        controller.transportControls?.let { NotificationAction.of(intent.action)?.exec(context, it) }
    }

}
