package com.sjn.stamp.utils

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.util.SparseArray
import com.google.common.collect.Lists
import java.util.*

object MediaRetrieveHelper {

    private const val ALL_MUSIC_SELECTION = MediaStore.Audio.Media.IS_MUSIC + " != 0"
    private const val SORT_ORDER = MediaStore.Audio.Media.TITLE

    private val GENRE_PROJECTION = arrayOf(MediaStore.Audio.Genres.NAME, MediaStore.Audio.Genres._ID)
    private val MEDIA_PROJECTION = arrayOf(MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TRACK, MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATE_MODIFIED)

    val PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    interface PermissionRequiredCallback {
        fun onPermissionRequired()
    }

    fun allMediaMetadataCompat(context: Context, callback: PermissionRequiredCallback?): List<MediaMetadataCompat> =
            Lists.transform(retrieveAllMedia(context, callback)) { mediaCursorContainer -> mediaCursorContainer!!.buildMediaMetadataCompat() }

    fun createIterator(list: List<MediaCursorContainer>): Iterator<MediaMetadataCompat> =
            Lists.transform(list) { mediaCursorContainer -> mediaCursorContainer!!.buildMediaMetadataCompat() }.iterator()

    fun hasPermission(context: Context): Boolean =
            PermissionHelper.hasPermission(context, MediaRetrieveHelper.PERMISSIONS)

    fun findByMusicId(context: Context, musicId: Long?, callback: PermissionRequiredCallback?): MediaMetadataCompat? {
        musicId ?: return null
        if (!MediaRetrieveHelper.hasPermission(context) && callback != null) {
            callback.onPermissionRequired()
            return null
        }
        context.contentResolver.query(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId), MEDIA_PROJECTION, ALL_MUSIC_SELECTION, null, null).use {
            try {
                if (it?.moveToFirst() == true) {
                    return parseCursor(it, null).buildMediaMetadataCompat()
                }
            } catch (e: java.lang.SecurityException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun findAlbumArtByArtist(context: Context, artist: String, callback: PermissionRequiredCallback?): String? {
        if (!MediaRetrieveHelper.hasPermission(context) && callback != null) {
            callback.onPermissionRequired()
            return null
        }
        context.contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                MediaStore.Audio.Albums.ARTIST + "=?",
                arrayOf(artist), null).use {
            try {
                if (it?.moveToFirst() == true) {
                    return makeAlbumArtUri(it.getLong(it.getColumnIndex(MediaStore.Audio.Albums._ID))).toString()
                }
            } catch (e: java.lang.SecurityException) {
                e.printStackTrace()
            }

        }
        return ""
    }

    fun retrieveAllMedia(context: Context, callback: PermissionRequiredCallback?): List<MediaCursorContainer> {
        val mediaList = ArrayList<MediaCursorContainer>()
        if (!MediaRetrieveHelper.hasPermission(context) && callback != null) {
            callback.onPermissionRequired()
            return mediaList
        }
        val genreMap = createGenreMap(context, callback)
        context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MEDIA_PROJECTION, ALL_MUSIC_SELECTION, null, SORT_ORDER).use {
            try {
                if (it?.moveToFirst() == true) {
                    do {
                        mediaList.add(parseCursor(it, genreMap))
                    } while (it.moveToNext())
                }
            } catch (e: java.lang.SecurityException) {
                e.printStackTrace()
            }
        }
        return mediaList
    }

    private fun createGenreMap(context: Context, callback: PermissionRequiredCallback?): SparseArray<String>? {
        if (!MediaRetrieveHelper.hasPermission(context) && callback != null) {
            callback.onPermissionRequired()
            return null
        }
        val genreMap = SparseArray<String>()
        try {
            context.contentResolver.query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, GENRE_PROJECTION, null, null, null).use {
                if (it?.moveToFirst() == true) {
                    do {
                        genreMap.put(it.getInt(it.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)),
                                it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME))
                        )
                    } while (it.moveToNext())
                }
            }
        } catch (e: java.lang.SecurityException) {
            e.printStackTrace()
        }
        return genreMap

    }

    private fun parseCursor(cursor: Cursor, genreMap: SparseArray<String>?): MediaCursorContainer {
        val title = cursor.getString(0)
        val artist = cursor.getString(1)
        val album = cursor.getString(2)
        val duration = cursor.getLong(3)
        //String source = cursor.getString(4);
        val trackNumber = cursor.getInt(5)
        val totalTrackCount = cursor.getLong(6)
        val musicId = cursor.getString(7)
        val albumId = cursor.getLong(8)
        val dateAdded = TimeHelper.toRFC3339(cursor.getLong(10))
        val source = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, java.lang.Long.valueOf(musicId)!!).toString()
        val genre = ""
        /*
        if (genreMap == null || genreMap.size() == 0) {
            genre = "";
        } else {
            try {
                genre = genreMap.get(cursor.getInt(11));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        */
        return MediaCursorContainer(musicId, source, album, artist, duration, genre, makeAlbumArtUri(albumId).toString(), title, trackNumber, totalTrackCount, dateAdded)
    }

    internal fun makeAlbumArtUri(albumId: Long): Uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)

    class MediaCursorContainer(private var musicId: String, private var source: String, private var album: String, private var artist: String, private var duration: Long, private var genre: String, internal var albumArtUri: String, internal var title: String, private var trackNumber: Int, private var totalTrackCount: Long, private var dateAdded: String) {

        fun buildMediaMetadataCompat(): MediaMetadataCompat =
                MediaItemHelper.createMetadata(musicId, source, album, artist, genre, duration, albumArtUri, title, trackNumber.toLong(), totalTrackCount, dateAdded)
    }

}

