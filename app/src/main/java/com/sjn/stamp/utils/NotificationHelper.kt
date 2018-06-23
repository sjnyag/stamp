package com.sjn.stamp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import com.sjn.stamp.R
import com.sjn.stamp.model.Song
import com.sjn.stamp.ui.activity.IntentDispatchActivity
import java.util.*

object NotificationHelper {

    const val ACTION_CMD = "com.sjn.stamp.ACTION_CMD"
    const val CMD_NAME = "CMD_NAME"
    const val CMD_SHARE = "CMD_SHARE"
    const val SHARE_MESSAGE = "SHARE_MESSAGE"
    const val HASH_TAG_LIST = "HASH_TAG_LIST"
    private const val CHANNEL_ID = "stamp_channel_02"
    private const val CHANNEL_NAME = "Achievement"

    fun createChannel(context: Context, channelId: String, name: String) {
        if (CompatibleHelper.hasOreo()) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW))
        }
    }

    fun isSendPlayedNotification(count: Int): Boolean {
        return count == 10 || count == 50 || count % 100 == 0 && count >= 100
    }

    fun sendPlayedNotification(context: Context, title: String, bitmapUrl: String, playCount: Int, recordedAt: Date) {
        val contentTitle = context.resources.getString(R.string.notification_title, title, playCount.toString())
        val contentText = context.resources.getString(R.string.notification_text, TimeHelper.getDateDiff(context, recordedAt))
        val requestCode = TimeHelper.localUnixTime.toInt()
        val shareIntent = createShareAction(context, requestCode, contentTitle + "\n" + contentText, ArrayList(Arrays.asList(title)))
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            color = ViewHelper.getThemeColor(context, R.attr.colorPrimary, Color.DKGRAY)
            setSmallIcon(R.mipmap.ic_notification)
            setLargeIcon(AlbumArtHelper.readBitmapSync(context, bitmapUrl, title))
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentTitle(contentTitle)
            setContentText(contentText)
            setContentIntent(shareIntent)
            addAction(R.drawable.ic_share, context.resources.getString(R.string.notification_share), shareIntent)
        }
        send(context, requestCode, builder)
    }

    fun sendPlayedNotification(context: Context, song: Song, bitmapUrl: String, playCount: Int, recordedAt: Date) {
        val contentTitle = context.resources.getString(R.string.notification_title, context.resources.getString(R.string.share_song, song.title, song.artist.name), playCount.toString())
        val contentText = context.resources.getString(R.string.notification_text, TimeHelper.getDateDiff(context, recordedAt))
        val requestCode = TimeHelper.localUnixTime.toInt()
        val shareIntent = createShareAction(context, requestCode, contentTitle + "\n" + contentText, ArrayList(Arrays.asList(song.title, song.artist.name)))
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            color = ViewHelper.getThemeColor(context, R.attr.colorPrimary, Color.DKGRAY)
            setSmallIcon(R.mipmap.ic_notification)
            setLargeIcon(AlbumArtHelper.readBitmapSync(context, bitmapUrl, song.title))
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentTitle(contentTitle)
            setContentText(contentText)
            setContentIntent(shareIntent)
            addAction(R.drawable.ic_share, context.resources.getString(R.string.notification_share), shareIntent)
        }
        send(context, requestCode, builder)
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

}
