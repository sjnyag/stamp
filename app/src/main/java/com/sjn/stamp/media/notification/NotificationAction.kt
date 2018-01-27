package com.sjn.stamp.media.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import com.sjn.stamp.MusicService
import com.sjn.stamp.ui.activity.MusicPlayerListActivity
import com.sjn.stamp.utils.NotificationHelper

enum class NotificationAction(val action: String) {
    PLAY("com.sjn.stamp.play") {
        override fun exec(context: Context, control: MediaControllerCompat.TransportControls) = control.play()
    },
    PAUSE("com.sjn.stamp.pause") {
        override fun exec(context: Context, control: MediaControllerCompat.TransportControls) = control.pause()
    },
    PREV("com.sjn.stamp.prev") {
        override fun exec(context: Context, control: MediaControllerCompat.TransportControls) = control.skipToPrevious()
    },
    NEXT("com.sjn.stamp.next") {
        override fun exec(context: Context, control: MediaControllerCompat.TransportControls) = control.skipToNext()
    },
    STOP_CAST("com.sjn.stamp.stop_cast") {
        override fun exec(context: Context, control: MediaControllerCompat.TransportControls) {
            context.startService(Intent(context, MusicService::class.java).apply {
                action = NotificationHelper.ACTION_CMD
                putExtra(NotificationHelper.CMD_NAME, NotificationHelper.CMD_STOP_CASTING)
            })
        }
    }, ;

    open fun createIntent(context: Context): PendingIntent? =
            PendingIntent.getBroadcast(context, REQUEST_CODE,
                    Intent(action).setPackage(context.packageName), PendingIntent.FLAG_CANCEL_CURRENT)

    abstract fun exec(context: Context, control: MediaControllerCompat.TransportControls)

    companion object {
        private const val REQUEST_CODE = 100

        fun of(action: String): NotificationAction? {
            return NotificationAction.values().firstOrNull { it.action == action }
        }

        fun contentIntent(context: Context, description: MediaDescriptionCompat?): PendingIntent =
                PendingIntent.getActivity(context, REQUEST_CODE,
                        Intent(context, MusicPlayerListActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra(MusicPlayerListActivity.EXTRA_START_FULLSCREEN, true)
                            description?.let {
                                putExtra(MusicPlayerListActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, it)
                            }
                        },
                        PendingIntent.FLAG_CANCEL_CURRENT)

        fun killIntent(context: Context): PendingIntent =
                PendingIntent.getService(context, REQUEST_CODE,
                        Intent(context, MusicService::class.java).apply {
                            action = NotificationHelper.ACTION_CMD
                            putExtra(NotificationHelper.CMD_NAME, NotificationHelper.CMD_KILL)
                        },
                        PendingIntent.FLAG_CANCEL_CURRENT)

        fun createIntentFilter(): IntentFilter =
                IntentFilter().apply {
                    NotificationAction.values().forEach { addAction(it.action) }
                }
    }
}
