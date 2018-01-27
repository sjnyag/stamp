package com.sjn.stamp.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream

object DataSourceHelper {

    fun setMediaPlayerDataSource(context: Context, mediaPlayer: MediaPlayer, fileInfo: String): Boolean {
        try {
            when {
                fileInfo.startsWith("content://") -> mediaPlayer.setDataSource(context, Uri.parse(fileInfo))
                CompatibleHelper.hasHoneycomb() -> try {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(fileInfo)
                } catch (e: Exception) {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(context, Uri.parse(Uri.encode(fileInfo)))
                }
                else -> {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(context, Uri.parse(Uri.encode(fileInfo)))
                }
            }
        } catch (e: Exception) {
            try {
                FileInputStream(File(fileInfo)).use {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(it.fd)
                }
            } catch (ee: Exception) {
                try {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(getRingtoneUriFromPath(context, fileInfo))
                } catch (eee: Exception) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRingtoneUriFromPath(context: Context, path: String): String {
        val ringtoneUri = MediaStore.Audio.Media.getContentUriForPath(path)
        val id = context.contentResolver.query(ringtoneUri, null, MediaStore.Audio.Media.DATA + "='" + path + "'", null, null).use {
            it.moveToFirst()
            it.getLong(it.getColumnIndex(MediaStore.Audio.Media._ID))
        }
        return if (ringtoneUri.toString().endsWith(id.toString())) ringtoneUri.toString() else ringtoneUri.toString() + "/" + id
    }

}
