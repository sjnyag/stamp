package com.sjn.stamp.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import java.util.*

object LocalPlaylistHelper {

    private val PLAYLIST_URI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
    private val COUNT_PROJECTION = arrayOf("count(*)")
    private val MEDIA_PROJECTION = arrayOf(MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TRACK, MediaStore.Audio.Media.SIZE, MediaStore.Audio.Playlists.Members.AUDIO_ID, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DISPLAY_NAME)
    private val PLAYLIST_PROJECTION = arrayOf(MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME)
    private const val PLAYLIST_ORDER = MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER
    private const val MEDIA_ORDER = MediaStore.Audio.Playlists.Members.PLAY_ORDER + " DESC"

    fun isExistAudioId(resolver: ContentResolver, audioId: Int, playlistId: Int): Boolean {
        findAllMediaCursor(resolver, playlistId).use {
            if (it?.moveToFirst() == true) {
                do {
                    if (audioId.toString() == it.getString(7)) {
                        return true
                    }
                } while (it.moveToNext())
            }
        }
        return false
    }

    fun isExistPlayListName(resolver: ContentResolver, name: String?): Boolean {
        return name != null && name.isNotEmpty() && findPlaylistId(resolver, name) >= 0
    }

    fun findPlaylistId(resolver: ContentResolver, name: String): Int {
        var id = -1
        findPlaylistByNameCursor(resolver, name).use {
            if (it?.moveToFirst() == true) {
                if (!it.isAfterLast) {
                    id = it.getInt(0)
                }
            }
        }
        return id
    }

    fun findPlaylistName(resolver: ContentResolver, playlistId: Int): String {
        var name = ""
        findPlaylistByIdCursor(resolver, playlistId).use {
            if (it?.moveToFirst() == true) {
                if (!it.isAfterLast) {
                    name = it.getString(1)
                }
            }
        }
        return name
    }

    fun findAllPlaylistMedia(resolver: ContentResolver, playlistId: Int, playlistTitle: String): MutableList<MediaMetadataCompat> {
        val mediaList = ArrayList<MediaMetadataCompat>()
        findAllMediaCursor(resolver, playlistId).use {
            if (it?.moveToFirst() == true) {
                do {
                    mediaList.add(parseCursor(it, playlistTitle))
                } while (it.moveToNext())
            }

        }
        return mediaList
    }

    fun findAllPlaylist(resolver: ContentResolver, playlistMap: MutableMap<String, MutableList<MediaMetadataCompat>>) {
        findAllPlaylistCursor(resolver).use {
            if (it?.moveToFirst() == true) {
                do {
                    playlistMap[it.getString(1)] = findAllPlaylistMedia(resolver, it.getInt(0), it.getString(1))
                } while (it.moveToNext())
            }
        }
    }

    fun create(resolver: ContentResolver, name: String): Boolean {
        if (!isExistPlayListName(resolver, name)) {
            return false
        }
        return resolver.insert(PLAYLIST_URI, ContentValues(1).apply { put(MediaStore.Audio.Playlists.NAME, name) }) != null
    }

    fun update(resolver: ContentResolver, srcValue: String, dstValue: String): Int {
        if (!isExistPlayListName(resolver, srcValue)) {
            return -1
        }
        return resolver.update(PLAYLIST_URI, ContentValues(1).apply { put(MediaStore.Audio.Playlists.NAME, dstValue) }, wherePlayList(resolver, srcValue), null)
    }

    fun delete(resolver: ContentResolver, name: String): Int {
        return if (!isExistPlayListName(resolver, name)) {
            -1
        } else resolver.delete(PLAYLIST_URI, wherePlayList(resolver, name), null)
    }

    //FIXME: delete duplication check
    //borrowed from http://stackoverflow.com/questions/3182937
    fun add(resolver: ContentResolver, audioId: Int, playlistId: Int): Boolean {
        if (isExistAudioId(resolver, audioId, playlistId)) {
            return false
        }
        val uri = createPlaylistUrl(playlistId)
        return resolver.insert(uri, ContentValues().apply {
            put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, count(resolver, uri) + audioId)
            put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId)
        }) != null
    }

    //FIXME: all of same medias are removed
    fun remove(resolver: ContentResolver, audioId: Int, playlistId: Int): Boolean {
        if (isExistAudioId(resolver, audioId, playlistId)) {
            return false
        }
        return 0 < resolver.delete(createPlaylistUrl(playlistId), MediaStore.Audio.Playlists.Members.AUDIO_ID + " = " + audioId, null)
    }

    private fun count(resolver: ContentResolver, uri: Uri): Int {
        resolver.query(uri, COUNT_PROJECTION, null, null, null).use {
            if (it?.moveToFirst() == true) {
                return it.getInt(0)
            }
        }
        return 0
    }

    private fun findPlaylistByIdCursor(resolver: ContentResolver, playlistId: Int): Cursor? {
        return resolver.query(
                PLAYLIST_URI,
                PLAYLIST_PROJECTION, MediaStore.Audio.Playlists._ID + "= ?", arrayOf(playlistId.toString()), PLAYLIST_ORDER)
    }

    private fun findPlaylistByNameCursor(resolver: ContentResolver, name: String): Cursor? {
        return resolver.query(
                PLAYLIST_URI,
                PLAYLIST_PROJECTION, MediaStore.Audio.Playlists.NAME + "= ?", arrayOf(name), PLAYLIST_ORDER)
    }

    private fun findAllPlaylistCursor(resolver: ContentResolver): Cursor? {
        return resolver.query(
                PLAYLIST_URI,
                PLAYLIST_PROJECTION, null, null, PLAYLIST_ORDER)
    }

    private fun findAllMediaCursor(resolver: ContentResolver, playlistId: Int): Cursor? {
        return resolver.query(
                createPlaylistUrl(playlistId),
                MEDIA_PROJECTION, null, null, MEDIA_ORDER)
    }

    private fun parseCursor(cursor: Cursor, playlistTitle: String): MediaMetadataCompat {
        val title = cursor.getString(0)
        val artist = cursor.getString(1)
        val album = cursor.getString(2)
        val duration = cursor.getLong(3)
        val source = cursor.getString(4)
        val trackNumber = cursor.getInt(5)
        val totalTrackCount = cursor.getLong(6)
        val musicId = cursor.getString(7)
        val albumId = cursor.getLong(8)
        return MediaItemHelper.createMetadata(musicId, source, album, artist, playlistTitle, duration, MediaRetrieveHelper.makeAlbumArtUri(albumId).toString(), title, trackNumber.toLong(), totalTrackCount, null)
    }

    private fun createPlaylistUrl(playlistId: Int): Uri {
        return MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId.toLong())
    }

    private fun wherePlayList(resolver: ContentResolver, playlistName: String): String {
        return MediaStore.Audio.Playlists._ID + " = " + findPlaylistId(resolver, playlistName)
    }

}