package com.sjn.stamp.controller

import android.content.ContentResolver
import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.utils.LocalPlaylistHelper

@Suppress("unused")
class PlaylistController(context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver

    fun loadAllPlaylist(map: MutableMap<String, MutableList<MediaMetadataCompat>>) {
        try {
            LocalPlaylistHelper.findAllPlaylist(contentResolver, map)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun isExistAudioId(audioId: Int, playlistId: Int): Boolean = LocalPlaylistHelper.isExistAudioId(contentResolver, audioId, playlistId)

    fun isExistPlayListName(name: String): Boolean = LocalPlaylistHelper.isExistPlayListName(contentResolver, name)

    fun addToPlaylist(audioId: Int, playlistId: Int): Boolean = LocalPlaylistHelper.add(contentResolver, audioId, playlistId)

    fun removeFromPlaylist(audioId: Int, playlistId: Int): Boolean = LocalPlaylistHelper.remove(contentResolver, audioId, playlistId)

    fun getPlaylistName(playlistId: Int): String = LocalPlaylistHelper.findPlaylistName(contentResolver, playlistId)

    fun getPlaylistId(name: String): Int = LocalPlaylistHelper.findPlaylistId(contentResolver, name)

    fun createPlaylist(name: String): Boolean = LocalPlaylistHelper.create(contentResolver, name)

    fun updatePlaylist(srcValue: String, dstValue: String): Int = LocalPlaylistHelper.update(contentResolver, srcValue, dstValue)

    fun deletePlaylist(name: String): Int = LocalPlaylistHelper.delete(contentResolver, name)

    fun getMediaList(playlistId: Int): List<MediaMetadataCompat> = getMediaList(playlistId, LocalPlaylistHelper.findPlaylistName(contentResolver, playlistId))

    private fun getMediaList(playlistId: Int, playlistTitle: String): List<MediaMetadataCompat> = LocalPlaylistHelper.findAllPlaylistMedia(contentResolver, playlistId, playlistTitle)

}