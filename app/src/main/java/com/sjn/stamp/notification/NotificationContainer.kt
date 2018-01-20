package com.sjn.stamp.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.sjn.stamp.R
import com.sjn.stamp.media.player.StampSession.Companion.EXTRA_CONNECTED_CAST
import com.sjn.stamp.utils.CompatibleHelper
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.ViewHelper
import java.io.FileNotFoundException

class NotificationContainer(
        private val context: Context,
        private val sessionToken: MediaSessionCompat.Token,
        private val controller: MediaControllerCompat
) {
    companion object {
        private val TAG = LogHelper.makeLogTag(Notification::class.java)
        const val NOTIFICATION_ID = 412
        const val CHANNEL_ID = "stamp_channel_01"

        private class SetNotificationBitmapAsyncTask(
                internal val contentResolver: ContentResolver,
                internal val title: String,
                internal val albumArtUri: Uri,
                internal val builder: NotificationCompat.Builder,
                internal val container: NotificationContainer
        ) : AsyncTask<Void, Void, Void>() {
            var bitmap: Bitmap? = null

            override fun onPreExecute() {
                bitmap = null
            }

            override fun doInBackground(vararg params: Void): Void? {
                bitmap = try {
                    MediaStore.Images.Media.getBitmap(contentResolver, albumArtUri)
                } catch (e: FileNotFoundException) {
                    ViewHelper.toBitmap(ViewHelper.createTextDrawable(title))
                }
                bitmap?.let { setAndNotify(builder, container, it) }
                return null
            }

            fun loadPreparedBitmap(builder: NotificationCompat.Builder, container: NotificationContainer, albumArtUri: Uri): Boolean {
                if (albumArtUri == this.albumArtUri) {
                    bitmap?.let {
                        setAndNotify(builder, container, it)
                        return true
                    }
                }
                return false
            }

            private fun setAndNotify(builder: NotificationCompat.Builder, container: NotificationContainer, bitmap: Bitmap) {
                builder.setLargeIcon(bitmap)
                container.notification = builder.build()
                container.start()
            }
        }
    }

    var notification: Notification? = null

    private var bitmapLoadTask: SetNotificationBitmapAsyncTask? = null
    private val notificationColor: Int = ViewHelper.getThemeColor(context, R.attr.colorPrimary, Color.DKGRAY)
    private val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        notificationManager.cancelAll()
    }

    fun create(metadata: MediaMetadataCompat?, playbackState: PlaybackStateCompat?) {
        LogHelper.d(TAG, "updateNotificationMetadata. metadata=" + metadata)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        val actions = arrayListOf<Int>()

        notificationBuilder
                .apply {
                    // If skip to previous action is enabled
                    //if (playbackState.actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L) {
                        addPreviousAction()

                        // If there is a "skip to previous" button, the play/pause button will
                        // be the second one. We need to keep track of it, because the MediaStyle notification
                        // requires to specify the index of the buttons (actions) that should be visible
                        // when in compact view.
                        actions.add(actions.size)
                    //}
                }
                .apply {
                    playbackState?.let{
                        addPlayPauseAction(it)
                    }
                    actions.add(actions.size)
                }
                .apply {
                    // If skip to next action is enabled
                    //if (playbackState.actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L) {
                        addNextAction()
                        actions.add(actions.size)
                    //}
                }
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(*actions.toIntArray())  // show only play/pause in compact view
                        .setMediaSession(sessionToken))
                .setColor(notificationColor)
                .setSmallIcon(R.mipmap.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setUsesChronometer(true)
                .setDeleteIntent(NotificationAction.killIntent(context))
                .setContentIntent(NotificationAction.contentIntent(context, metadata?.description))
                .setContentTitle(metadata?.description?.title)
                .setContentText(metadata?.description?.subtitle)
                .apply {
                    addCastAction()
                }
                .apply {
                    playbackState?.let{
                        setNotificationPlaybackState(it)
                    }
                }
        metadata?.let{
            fetchBitmapFromURLAsync(notificationBuilder, it)
        }
        notification = notificationBuilder.build()
    }

    fun start() = notification?.let { notificationManager.notify(NOTIFICATION_ID, it) }

    fun cancel() = notificationManager.cancel(NOTIFICATION_ID)

    private fun createNotificationChannel() {
        if (CompatibleHelper.hasOreo()) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Controller", NotificationManager.IMPORTANCE_LOW))
        }
    }

    private fun fetchBitmapFromURLAsync(builder: NotificationCompat.Builder, metadata: MediaMetadataCompat) {
        metadata.description.iconUri?.let {
            if (bitmapLoadTask != null && bitmapLoadTask!!.loadPreparedBitmap(builder, this@NotificationContainer, it)) {
                return
            }
            builder.setLargeIcon(metadata.getTextDrawableBitmap())
            bitmapLoadTask?.cancel(true)
            bitmapLoadTask = SetNotificationBitmapAsyncTask(context.contentResolver, metadata.description.title.toString(), it, builder, this@NotificationContainer)
            bitmapLoadTask?.execute()
        }
    }

    private fun NotificationCompat.Builder.addPreviousAction() =
            addAction(R.drawable.ic_skip_previous_white_24dp,
                    context.getString(R.string.label_previous), NotificationAction.PREV.createIntent(context))

    private fun NotificationCompat.Builder.addNextAction() =
            addAction(R.drawable.ic_skip_next_white_24dp,
                    context.getString(R.string.label_next), NotificationAction.NEXT.createIntent(context))

    private fun NotificationCompat.Builder.addCastAction() =
            controller.extras?.let {
                it.getString(EXTRA_CONNECTED_CAST)?.let {
                    setSubText(context.resources.getString(R.string.casting_to_device, it))
                    addAction(R.drawable.ic_close_black_24dp, context.getString(R.string.stop_casting), NotificationAction.STOP_CAST.createIntent(context))
                }
            }

    private fun NotificationCompat.Builder.addPlayPauseAction(playbackState: PlaybackStateCompat) {
        val label: String
        val icon: Int
        val intent: PendingIntent?
        if (playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
            label = context.getString(R.string.label_pause)
            icon = R.drawable.stamp_ic_pause_white_24dp
            intent = NotificationAction.PAUSE.createIntent(context)
        } else {
            label = context.getString(R.string.label_play)
            icon = R.drawable.stamp_ic_play_arrow_white_24dp
            intent = NotificationAction.PLAY.createIntent(context)
        }
        addAction(NotificationCompat.Action(icon, label, intent))
    }

    private fun NotificationCompat.Builder.setNotificationPlaybackState(playbackState: PlaybackStateCompat) {
        LogHelper.d(TAG, "updateNotificationPlaybackState. playbackState=" + playbackState)
        if (playbackState.state == PlaybackStateCompat.STATE_PLAYING && playbackState.position >= 0) {
            LogHelper.d(TAG, "updateNotificationPlaybackState. updating playback position to ",
                    (System.currentTimeMillis() - playbackState.position) / 1000, " seconds")
            setWhen(System.currentTimeMillis() - playbackState.position)
                    .setShowWhen(true)
                    .setUsesChronometer(true)
        } else {
            LogHelper.d(TAG, "updateNotificationPlaybackState. hiding playback position")
            setWhen(0)
                    .setShowWhen(false)
                    .setUsesChronometer(false)
        }
        // Make sure that the notification can be dismissed by the user when we are not playing:
        setOngoing(playbackState.state == PlaybackStateCompat.STATE_PLAYING)
    }

    private fun MediaMetadataCompat.getTextDrawableBitmap() =
            ViewHelper.toBitmap(ViewHelper.createTextDrawable(description.title.toString()))
}