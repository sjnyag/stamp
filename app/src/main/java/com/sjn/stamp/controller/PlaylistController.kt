package com.sjn.stamp.controller

import android.content.ContentResolver
import android.content.Context
import android.support.v4.media.MediaMetadataCompat

import com.sjn.stamp.utils.LocalPlaylistHelper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Suppress("unused")
class PlaylistController(context: Context) {

    private val mContentResolver: ContentResolver = context.contentResolver

    val allPlaylist: ConcurrentMap<String, List<MediaMetadataCompat>>
        get() {
            return try {
                LocalPlaylistHelper.findAllPlaylist(mContentResolver)
            } catch (e: SecurityException) {
                e.printStackTrace()
                ConcurrentHashMap()
            }

        }

    fun isExistAudioId(audioId: Int, playlistId: Int): Boolean = LocalPlaylistHelper.isExistAudioId(mContentResolver, audioId, playlistId)

    fun isExistPlayListName(name: String): Boolean = LocalPlaylistHelper.isExistPlayListName(mContentResolver, name)

    fun addToPlaylist(audioId: Int, playlistId: Int): Boolean = LocalPlaylistHelper.add(mContentResolver, audioId, playlistId)

    fun removeFromPlaylist(audioId: Int, playlistId: Int): Boolean = LocalPlaylistHelper.remove(mContentResolver, audioId, playlistId)

    fun getPlaylistName(playlistId: Int): String = LocalPlaylistHelper.findPlaylistName(mContentResolver, playlistId)

    fun getPlaylistId(name: String): Int = LocalPlaylistHelper.findPlaylistId(mContentResolver, name)

    fun createPlaylist(name: String): Boolean = LocalPlaylistHelper.create(mContentResolver, name)

    fun updatePlaylist(srcValue: String, dstValue: String): Int = LocalPlaylistHelper.update(mContentResolver, srcValue, dstValue)

    fun deletePlaylist(name: String): Int = LocalPlaylistHelper.delete(mContentResolver, name)

    fun getMediaList(playlistId: Int): List<MediaMetadataCompat> = getMediaList(playlistId, LocalPlaylistHelper.findPlaylistName(mContentResolver, playlistId))

    private fun getMediaList(playlistId: Int, playlistTitle: String): List<MediaMetadataCompat> = LocalPlaylistHelper.findAllPlaylistMedia(mContentResolver, playlistId, playlistTitle)

}