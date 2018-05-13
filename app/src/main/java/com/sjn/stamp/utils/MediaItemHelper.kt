package com.sjn.stamp.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.common.images.WebImage
import com.sjn.stamp.model.Song
import com.sjn.stamp.model.TotalSongHistory
import com.sjn.stamp.model.constant.CategoryType
import com.sjn.stamp.model.dao.ArtistDao
import io.realm.RealmList
import org.json.JSONObject

object MediaItemHelper {

    private const val CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__"
    private const val MIME_TYPE_AUDIO_MPEG = "audio/mpeg"
    const val META_DATA_KEY_BASE_MEDIA_ID = "com.sjn.stamp.media.META_DATA_KEY_BASE_MEDIA_ID"

    fun isSameSong(metadata: MediaMetadataCompat, song: Song): Boolean =
            song.album == fetchString(metadata, MediaMetadataCompat.METADATA_KEY_ALBUM) &&
                    song.artist.name == fetchString(metadata, MediaMetadataCompat.METADATA_KEY_ARTIST) &&
                    song.title == fetchString(metadata, MediaMetadataCompat.METADATA_KEY_TITLE)

    private fun convertToMetadataBuilder(song: Song): MediaMetadataCompat.Builder =
            MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.mediaId)
                    .putString(CUSTOM_METADATA_TRACK_SOURCE, song.trackSource)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist.name)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration!!)
                    .putString(MediaMetadataCompat.METADATA_KEY_GENRE, song.genre)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.albumArtUri)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                    .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, song.trackNumber!!)
                    .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, song.numTracks!!)
                    .putString(MediaMetadataCompat.METADATA_KEY_DATE, song.dateAdded)

    fun updateMediaId(metadata: MediaMetadataCompat, mediaId: String): MediaMetadataCompat =
            MediaMetadataCompat.Builder(metadata)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                    .build()


    fun updateMusicArt(metadata: MediaMetadataCompat, albumArt: Bitmap, icon: Bitmap): MediaMetadataCompat =
            MediaMetadataCompat.Builder(metadata)
                    // set high resolution bitmap in METADATA_KEY_ALBUM_ART. This is used, for
                    // example, on the lockscreen background when the media session is active.
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                    // set small version of the album art in the DISPLAY_ICON. This is used on
                    // the MediaDescription and thus it should be small to be serialized if
                    // necessary
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon)
                    .build()

    fun createMetadata(musicId: String, source: String, album: String, artist: String, playlistTitle: String, duration: Long?, albumArtUri: String, title: String, trackNumber: Long, totalTrackCount: Long, dateAdded: String?): MediaMetadataCompat =
            MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, musicId)
                    .putString(CUSTOM_METADATA_TRACK_SOURCE, source)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_GENRE, playlistTitle)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration!!)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArtUri)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                    .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                    .putString(MediaMetadataCompat.METADATA_KEY_DATE, dateAdded)
                    .build()

    fun convertToMetadata(song: Song): MediaMetadataCompat = convertToMetadataBuilder(song).build()

    fun convertToMetadata(queueItem: MediaSessionCompat.QueueItem, mediaId: String?): MediaMetadataCompat =
            MediaMetadataCompat.Builder().apply {
                queueItem.description.mediaId?.let { putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, MediaIDHelper.extractMusicIDFromMediaID(it)) }
                queueItem.description.description?.let { putString(MediaMetadataCompat.METADATA_KEY_ALBUM, it.toString()) }
                queueItem.description.subtitle?.let { putString(MediaMetadataCompat.METADATA_KEY_ARTIST, it.toString()) }
                queueItem.description.iconUri?.let { putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, it.toString()) }
                queueItem.description.title?.let { putString(MediaMetadataCompat.METADATA_KEY_TITLE, it.toString()) }
                queueItem.description.mediaUri?.let { putString(CUSTOM_METADATA_TRACK_SOURCE, it.toString()) }
                mediaId?.let { putString(META_DATA_KEY_BASE_MEDIA_ID, it) }
            }.build()


    fun fetch(mediaMetadata: MediaMetadataCompat, categoryType: CategoryType?): String? =
            when (categoryType) {
                null -> null
                CategoryType.ALBUM -> getAlbum(mediaMetadata)
                CategoryType.ARTIST -> getArtist(mediaMetadata)
                CategoryType.GENRE -> getGenre(mediaMetadata)
            }

    fun getTitle(metadata: MediaMetadataCompat): String? = fetchString(metadata, MediaMetadataCompat.METADATA_KEY_TITLE)

    fun getArtist(metadata: MediaMetadataCompat): String? = fetchString(metadata, MediaMetadataCompat.METADATA_KEY_ARTIST)

    fun getAlbum(metadata: MediaMetadataCompat): String? = fetchString(metadata, MediaMetadataCompat.METADATA_KEY_ALBUM)

    fun getGenre(metadata: MediaMetadataCompat): String? = fetchString(metadata, MediaMetadataCompat.METADATA_KEY_GENRE)

    fun getAlbumArtUri(metadata: MediaMetadataCompat): String? = fetchString(metadata, MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)

    fun createSong(metadata: MediaMetadataCompat): Song =
            Song(0,
                    metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) ?: "",
                    metadata.getString(MediaItemHelper.CUSTOM_METADATA_TRACK_SOURCE) ?: "",
                    getAlbum(metadata) ?: "",
                    fetchLong(metadata, MediaMetadataCompat.METADATA_KEY_DURATION),
                    fetchString(metadata, MediaMetadataCompat.METADATA_KEY_GENRE) ?: "",
                    fetchString(metadata, MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI) ?: "",
                    getTitle(metadata) ?: "",
                    fetchLong(metadata, MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER),
                    fetchLong(metadata, MediaMetadataCompat.METADATA_KEY_NUM_TRACKS),
                    fetchString(metadata, MediaMetadataCompat.METADATA_KEY_DATE) ?: "",
                    RealmList(), null,
                    TotalSongHistory(),
                    ArtistDao.newStandalone(
                            getArtist(metadata) ?: "",
                            getAlbumArtUri(metadata) ?: ""
                    )
            )

    fun convertToDescription(metadata: MediaMetadataCompat): MediaDescriptionCompat =
            MediaDescriptionCompat.Builder()
                    .setMediaId(metadata.description.mediaId)
                    .setTitle(metadata.description.title)
                    .setSubtitle(metadata.description.subtitle)
                    .setDescription(metadata.description.description)
                    .setIconBitmap(metadata.description.iconBitmap)
                    .setIconUri(metadata.description.iconUri)
                    .setExtras(metadata.bundle).build()

    fun convertToMediaInfo(track: MediaSessionCompat.QueueItem, customData: JSONObject, mediaUri: String, iconUri: Uri): MediaInfo {
        val mediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK).apply {
            putString(MediaMetadata.KEY_TITLE, if (track.description.title == null) "" else track.description.title.toString())
            putString(MediaMetadata.KEY_SUBTITLE, if (track.description.subtitle == null) "" else track.description.subtitle.toString())
            track.description.extras?.let {
                putString(MediaMetadata.KEY_ALBUM_ARTIST, it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
                putString(MediaMetadata.KEY_ARTIST, it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
                putString(MediaMetadata.KEY_ALBUM_TITLE, it.getString(MediaMetadataCompat.METADATA_KEY_ALBUM))
            }
            WebImage(iconUri).let {
                // First image is used by the receiver for showing the audio album art.
                addImage(it)
                // Second image is used by Cast Companion Library on the full screen activity that is shown
                // when the cast dialog is clicked.
                addImage(it)
            }
        }
        return MediaInfo.Builder(mediaUri)
                .setContentType(MIME_TYPE_AUDIO_MPEG)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .setCustomData(customData)
                .build()
    }

    fun convertToQueueItem(track: MediaMetadataCompat, mediaId: String, id: Long): MediaSessionCompat.QueueItem {
        val bundle = track.bundle.apply {
            putString(MediaMetadata.KEY_ARTIST, track.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
            putString(MediaMetadata.KEY_ALBUM_TITLE, track.getString(MediaMetadataCompat.METADATA_KEY_ALBUM))
        }
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(track.description.title)
                .setSubtitle(track.description.subtitle)
                .setDescription(track.description.description)
                .setIconBitmap(track.description.iconBitmap)
                .setIconUri(track.description.iconUri)
                .setMediaUri(track.getString(CUSTOM_METADATA_TRACK_SOURCE)?.let { if (it.isNotEmpty()) return@let Uri.parse(it) else return@let null })
                .setExtras(bundle).build()
        // We don't expect queues to change after created, so we use the item index as the
        // queueId. Any other number unique in the queue would work.
        return MediaSessionCompat.QueueItem(description, id)
    }

    fun createQueueItem(context: Context, uri: Uri): MediaSessionCompat.QueueItem? {
        val pathSegments = uri.pathSegments
        val host = uri.host
        val scheme = uri.scheme
        var albumName = ""
        var trackName = ""
        var artistName = ""
        try {
            MediaMetadataRetriever().apply {
                setDataSource(context, uri)
                albumName = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                artistName = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                trackName = extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                if (TextUtils.isEmpty(trackName) && pathSegments != null) {
                    trackName = pathSegments[pathSegments.size - 1]
                }
            }.release()
            return MediaSessionCompat.QueueItem(MediaDescriptionCompat.Builder()
                    .setMediaUri(uri)
                    .setMediaId(uri.toString())
                    .setTitle(trackName)
                    .setSubtitle(artistName)
                    .setDescription("streaming from $scheme")
                    //.setIconUri(Uri.parse(mCoverArtUrl))
                    .build(), 0)
        } catch (e: Exception) {
            return null
        }
    }

    fun createArtistMediaItem(artist: String): MediaBrowserCompat.MediaItem =
            MediaItemHelper.createBrowsableItem(MediaDescriptionCompat.Builder()
                    .setMediaId(MediaIDHelper.createMediaID(null, MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST, artist))
                    .setTitle(MediaIDHelper.unescape(artist))
                    .build())

    fun createPlayableItem(description: MediaDescriptionCompat): MediaBrowserCompat.MediaItem =
            MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)

    fun createPlayableItem(metadata: MediaMetadataCompat): MediaBrowserCompat.MediaItem =
            createPlayableItem(convertToDescription(metadata))

    fun createBrowsableItem(description: MediaDescriptionCompat): MediaBrowserCompat.MediaItem =
            MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)

    fun createBrowsableItem(mediaId: String, title: String): MediaBrowserCompat.MediaItem =
            createBrowsableItem(MediaDescriptionCompat.Builder()
                    .setMediaId(mediaId)
                    .setTitle(title)
                    .build())

    private fun fetchString(metadata: MediaMetadataCompat, key: String): String? = if (metadata.containsKey(key)) metadata.getString(key) else null

    private fun fetchLong(metadata: MediaMetadataCompat, key: String): Long? = if (metadata.containsKey(key)) metadata.getLong(key) else null
}
