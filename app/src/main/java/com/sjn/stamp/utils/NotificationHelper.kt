package com.sjn.stamp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import com.sjn.stamp.R
import com.sjn.stamp.model.Song
import com.sjn.stamp.ui.activity.IntentDispatchActivity
import java.io.FileNotFoundException
import java.lang.ref.WeakReference
import java.util.*

object NotificationHelper {

    val ACTION_CMD = "com.sjn.stamp.ACTION_CMD"
    val CMD_NAME = "CMD_NAME"
    val CMD_PLAY = "CMD_PLAY"
    val CMD_PAUSE = "CMD_PAUSE"
    val CMD_STOP_CASTING = "CMD_STOP_CASTING"
    val CMD_KILL = "CMD_KILL"
    val CMD_SHARE = "CMD_SHARE"
    val SHARE_MESSAGE = "SHARE_MESSAGE"
    val HASH_TAG_LIST = "HASH_TAG_LIST"
    private val CHANNEL_ID = "stamp_channel_02"
    private val CHANNEL_NAME = "Achievement"
    private val BUNDLE_KEY_PLAYED_TEXT = "BUNDLE_KEY_PLAYED_TEXT"
    private val BUNDLE_KEY_HASH_TAG_LIST = "BUNDLE_KEY_HASH_TAG_LIST"

    fun isSendPlayedNotification(count: Int): Boolean {
        return count == 10 || count == 50 || count % 100 == 0 && count >= 100
    }

    fun sendPlayedNotification(context: Context, title: String, bitmapUrl: String, playCount: Int, recordedAt: Date) {
        val contentTitle = context.resources.getString(R.string.notification_title, title, playCount.toString())
        val contentText = context.resources.getString(R.string.notification_text, TimeHelper.getDateDiff(context, recordedAt))
        val bundle = Bundle().apply {
            putString(BUNDLE_KEY_PLAYED_TEXT, contentTitle + "\n" + contentText)
            putStringArrayList(BUNDLE_KEY_HASH_TAG_LIST, ArrayList(Arrays.asList(title)))
        }
        fetchBitmapFromURLAsync(context, title, bitmapUrl, NotificationCompat.Builder(context, CHANNEL_ID)
                .setColor(ViewHelper.getThemeColor(context, R.attr.colorPrimary, Color.DKGRAY))
                .setSmallIcon(R.mipmap.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setExtras(bundle)
        )
    }

    fun sendPlayedNotification(context: Context, song: Song, bitmapUrl: String, playCount: Int, recordedAt: Date) {
        val contentTitle = context.resources.getString(R.string.notification_title, context.resources.getString(R.string.share_song, song.title, song.artist.name), playCount.toString())
        val contentText = context.resources.getString(R.string.notification_text, TimeHelper.getDateDiff(context, recordedAt))
        val bundle = Bundle().apply {
            putString(BUNDLE_KEY_PLAYED_TEXT, contentTitle + "\n" + contentText)
            putStringArrayList(BUNDLE_KEY_HASH_TAG_LIST, ArrayList(Arrays.asList(song.title, song.artist.name)))
        }
        fetchBitmapFromURLAsync(context, song.title, bitmapUrl, NotificationCompat.Builder(context, CHANNEL_ID)
                .setColor(ViewHelper.getThemeColor(context, R.attr.colorPrimary, Color.DKGRAY))
                .setSmallIcon(R.mipmap.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setExtras(bundle)
        )
    }

    private fun fetchBitmapFromURLAsync(context: Context, title: String, bitmapUrl: String,
                                        builder: NotificationCompat.Builder) {
        SetNotificationBitmapAsyncTask(context, object : SetNotificationBitmapAsyncTask.Callback {
            override fun onLoad(builder: NotificationCompat.Builder) {

                val requestCode = builder.hashCode()
                val shareIntent = createShareAction(context, requestCode, builder.extras.getString(BUNDLE_KEY_PLAYED_TEXT), builder.extras.getStringArrayList(BUNDLE_KEY_HASH_TAG_LIST))
                send(context, requestCode, builder.apply {
                    setContentIntent(shareIntent)
                    addAction(R.drawable.ic_share, context.resources.getString(R.string.notification_share), shareIntent)
                })
            }
        }, title, Uri.parse(bitmapUrl), builder).execute()
        /*
        ViewHelper.readBitmapAsync(context, bitmapUrl, object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                SetNotificationBitmapAsyncTask(context, builder, bitmap).execute()
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                val requestCode = builder.hashCode()
                val shareIntent = createShareAction(context, requestCode, builder.extras.getString(BUNDLE_KEY_PLAYED_TEXT), builder.extras.getStringArrayList(BUNDLE_KEY_HASH_TAG_LIST))
                send(context, requestCode, builder.apply {
                    setContentIntent(shareIntent)
                    addAction(R.drawable.ic_share, context.resources.getString(R.string.notification_share), shareIntent)
                })
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        })
        */
    }

    private fun createShareAction(context: Context, requestCode: Int, text: String?, hashTagList: ArrayList<String>?): PendingIntent =
            PendingIntent.getActivity(context, requestCode, Intent(context, IntentDispatchActivity::class.java).apply {
                action = ACTION_CMD
                putExtra(CMD_NAME, CMD_SHARE)
                putExtra(SHARE_MESSAGE, text)
                putStringArrayListExtra(HASH_TAG_LIST, hashTagList)
            }, PendingIntent.FLAG_UPDATE_CURRENT)

    private fun send(context: Context, id: Int, builder: NotificationCompat.Builder) {
        createChannel(context, CHANNEL_ID, CHANNEL_NAME)
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(id, builder.build())
    }

    fun createChannel(context: Context, channelId: String, name: String) {
        if (CompatibleHelper.hasOreo()) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW))
        }
    }

    class SetNotificationBitmapAsyncTask(
            context: Context,
            callback: Callback,
            internal val title: String,
            internal val albumArtUri: Uri,
            internal val builder: NotificationCompat.Builder
    ) : AsyncTask<Void, Void, Void>() {
        val context = WeakReference(context)
        val callback = WeakReference(callback)

        interface Callback {
            fun onLoad(builder: NotificationCompat.Builder)
        }

        var bitmap: Bitmap? = null

        override fun onPreExecute() {
            bitmap = null
        }

        override fun doInBackground(vararg params: Void): Void? {
            bitmap = try {
                context.get()?.let { MediaStore.Images.Media.getBitmap(it.contentResolver, albumArtUri) }
            } catch (e: FileNotFoundException) {
                ViewHelper.toBitmap(ViewHelper.createTextDrawable(title))
            }
            bitmap?.let { bitmap -> callback.get()?.let { callback -> setAndNotify(builder, callback, bitmap) } }
            return null
        }

        fun loadPreparedBitmap(builder: NotificationCompat.Builder, albumArtUri: Uri): Boolean {
            if (albumArtUri == this.albumArtUri) {
                bitmap?.let { bitmap ->
                    callback.get()?.let { callback ->
                        setAndNotify(builder, callback, bitmap)
                        return true
                    }
                }
            }
            return false
        }

        private fun setAndNotify(builder: NotificationCompat.Builder, callback: Callback, bitmap: Bitmap) {
            builder.setLargeIcon(bitmap)
            callback.onLoad(builder)
        }
    }
}
