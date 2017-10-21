package com.sjn.stamp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.sjn.stamp.constant.CategoryType;
import com.sjn.stamp.db.Song;
import com.sjn.stamp.db.SongHistory;
import com.sjn.stamp.db.SongStamp;
import com.sjn.stamp.db.TotalSongHistory;
import com.sjn.stamp.db.dao.ArtistDao;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.List;

import io.realm.RealmList;

public class MediaItemHelper {

    private static final String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";

    private static final String MIME_TYPE_AUDIO_MPEG = "audio/mpeg";

    public static final String META_DATA_KEY_BASE_MEDIA_ID = "com.sjn.stamp.media.META_DATA_KEY_BASE_MEDIA_ID";

    public static boolean isSameSong(MediaMetadataCompat metadata, Song song) {
        return song.getAlbum().equals(fetchString(metadata, MediaMetadataCompat.METADATA_KEY_ALBUM)) &&
                song.getArtist().getName().equals(fetchString(metadata, MediaMetadataCompat.METADATA_KEY_ARTIST)) &&
                song.getTitle().equals(fetchString(metadata, MediaMetadataCompat.METADATA_KEY_TITLE));
    }

    private static MediaMetadataCompat.Builder convertToMetadataBuilder(Song song) {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.getMediaId())
                .putString(CUSTOM_METADATA_TRACK_SOURCE, song.getTrackSource())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist().getName())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.getDuration())
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, song.getGenre())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.getAlbumArtUri())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, song.getTrackNumber())
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, song.getNumTracks())
                .putString(MediaMetadataCompat.METADATA_KEY_DATE, song.getDateAdded());
    }

    public static MediaMetadataCompat updateMediaId(MediaMetadataCompat metadata, String mediaId) {
        return new MediaMetadataCompat.Builder(metadata)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                .build();
    }

    public static MediaMetadataCompat updateMusicArt(MediaMetadataCompat metadata, Bitmap albumArt, Bitmap icon) {
        return new MediaMetadataCompat.Builder(metadata)
                // set high resolution bitmap in METADATA_KEY_ALBUM_ART. This is used, for
                // example, on the lockscreen background when the media session is active.
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                // set small version of the album art in the DISPLAY_ICON. This is used on
                // the MediaDescription and thus it should be small to be serialized if
                // necessary
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon)
                .build();
    }

    public static MediaMetadataCompat createMetadata(String musicId, String source, String album, String artist, String playlistTitle, Long duration, String albumArtUri, String title, long trackNumber, long totalTrackCount, String dateAdded) {
        //noinspection ResourceType
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, musicId)
                .putString(CUSTOM_METADATA_TRACK_SOURCE, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, playlistTitle)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArtUri)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .putString(MediaMetadataCompat.METADATA_KEY_DATE, dateAdded)
                .build();
    }

    public static MediaMetadataCompat convertToMetadata(Song song) {
        return convertToMetadataBuilder(song).build();
    }

    public static MediaMetadataCompat convertToMetadata(MediaSessionCompat.QueueItem queueItem, String mediaId) {

        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        if (queueItem.getDescription().getMediaId() != null) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, MediaIDHelper.extractMusicIDFromMediaID(queueItem.getDescription().getMediaId()));
        }
        if (queueItem.getDescription().getDescription() != null) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, queueItem.getDescription().getDescription().toString());
        }
        if (queueItem.getDescription().getSubtitle() != null) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, queueItem.getDescription().getSubtitle().toString());
        }
        if (queueItem.getDescription().getIconUri() != null) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, queueItem.getDescription().getIconUri().toString());
        }
        if (queueItem.getDescription().getTitle() != null) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, queueItem.getDescription().getTitle().toString());
        }
        if (queueItem.getDescription().getMediaUri() != null) {
            builder.putString(CUSTOM_METADATA_TRACK_SOURCE, queueItem.getDescription().getMediaUri().toString());
        }
        if (mediaId != null) {
            builder.putString(META_DATA_KEY_BASE_MEDIA_ID, mediaId);
        }
        return builder.build();

    }

    @NotNull
    public static String fetch(MediaMetadataCompat mediaMetadata, CategoryType categoryType) {
        if(categoryType == null){
            return null;
        }
        switch (categoryType){
            case ALBUM:
                return getAlbum(mediaMetadata);
            case ARTIST:
                return getArtist(mediaMetadata);
            case GENRE:
                return getGenre(mediaMetadata);
        }
        return null;
    }

    public static String getTitle(MediaMetadataCompat metadata) {
        return fetchString(metadata, MediaMetadataCompat.METADATA_KEY_TITLE);
    }

    public static String getArtist(MediaMetadataCompat metadata) {
        return fetchString(metadata, MediaMetadataCompat.METADATA_KEY_ARTIST);
    }

    public static String getAlbum(MediaMetadataCompat metadata) {
        return fetchString(metadata, MediaMetadataCompat.METADATA_KEY_ALBUM);
    }

    public static String getGenre(MediaMetadataCompat metadata) {
        return fetchString(metadata, MediaMetadataCompat.METADATA_KEY_GENRE);
    }

    public static String getAlbumArtUri(MediaMetadataCompat metadata) {
        return fetchString(metadata, MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
    }

    public static Song createSong(MediaMetadataCompat metadata) {
        return new Song(
                0,
                metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID),
                metadata.getString(MediaItemHelper.CUSTOM_METADATA_TRACK_SOURCE),
                getAlbum(metadata),
                fetchLong(metadata, MediaMetadataCompat.METADATA_KEY_DURATION),
                fetchString(metadata, MediaMetadataCompat.METADATA_KEY_GENRE),
                fetchString(metadata, MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI),
                getTitle(metadata),
                fetchLong(metadata, MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER),
                fetchLong(metadata, MediaMetadataCompat.METADATA_KEY_NUM_TRACKS),
                fetchString(metadata, MediaMetadataCompat.METADATA_KEY_DATE),
                new RealmList<SongStamp>(),
                null,
                new TotalSongHistory(),
                ArtistDao.INSTANCE.newStandalone(
                        getArtist(metadata),
                        getAlbumArtUri(metadata)
                )
        );
    }

    public static MediaDescriptionCompat convertToDescription(MediaMetadataCompat metadata) {
        return new MediaDescriptionCompat.Builder()
                .setMediaId(metadata.getDescription().getMediaId())
                .setTitle(metadata.getDescription().getTitle())
                .setSubtitle(metadata.getDescription().getSubtitle())
                .setDescription(metadata.getDescription().getDescription())
                .setIconBitmap(metadata.getDescription().getIconBitmap())
                .setIconUri(metadata.getDescription().getIconUri())
                .setExtras(metadata.getBundle()).build();
    }

    public static MediaInfo convertToMediaInfo(MediaSessionCompat.QueueItem track,
                                               JSONObject customData) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE,
                track.getDescription().getTitle() == null ? "" :
                        track.getDescription().getTitle().toString());
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE,
                track.getDescription().getSubtitle() == null ? "" :
                        track.getDescription().getSubtitle().toString());
        if (track.getDescription().getExtras() != null) {
            mediaMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST,
                    track.getDescription().getExtras().getString((MediaMetadataCompat.METADATA_KEY_ARTIST)));
            mediaMetadata.putString(MediaMetadata.KEY_ARTIST,
                    track.getDescription().getExtras().getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            mediaMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE,
                    track.getDescription().getExtras().getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
        }
        WebImage image = new WebImage(
                new Uri.Builder().encodedPath(track.getDescription().getIconUri().toString())
                        .build());
        // First image is used by the receiver for showing the audio album art.
        mediaMetadata.addImage(image);
        // Second image is used by Cast Companion Library on the full screen activity that is shown
        // when the cast dialog is clicked.
        mediaMetadata.addImage(image);

        //noinspection ResourceType
        return new MediaInfo.Builder(track.getDescription().getMediaUri().toString())
                .setContentType(MIME_TYPE_AUDIO_MPEG)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .setCustomData(customData)
                .build();
    }

    public static MediaInfo convertToMediaInfo(MediaSessionCompat.QueueItem track, String url) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE,
                track.getDescription().getTitle() == null ? "" :
                        track.getDescription().getTitle().toString());
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE,
                track.getDescription().getSubtitle() == null ? "" :
                        track.getDescription().getSubtitle().toString());
        if (track.getDescription().getExtras() != null) {
            mediaMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST,
                    track.getDescription().getExtras().getString((MediaMetadataCompat.METADATA_KEY_ARTIST)));
            mediaMetadata.putString(MediaMetadata.KEY_ARTIST,
                    track.getDescription().getExtras().getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            mediaMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE,
                    track.getDescription().getExtras().getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
        }
        WebImage image = new WebImage(
                new Uri.Builder().encodedPath(url + "/image/" + TimeHelper.getJapanNow().toString()).build());
        // First image is used by the receiver for showing the audio album art.
        mediaMetadata.addImage(image);
        // Second image is used by Cast Companion Library on the full screen activity that is shown
        // when the cast dialog is clicked.
        mediaMetadata.addImage(image);

        return new MediaInfo.Builder(url)
                .setContentType("audio/mpeg")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
    }

    public static MediaSessionCompat.QueueItem convertToQueueItem(MediaMetadataCompat track, String mediaId, long id) {
        Bundle bundle = track.getBundle();
        bundle.putString(MediaMetadata.KEY_ARTIST,
                track.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        bundle.putString(MediaMetadata.KEY_ALBUM_TITLE,
                track.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));

        Uri uri = null;
        String uriString = track.getString(CUSTOM_METADATA_TRACK_SOURCE);
        if (uriString != null && !uriString.isEmpty()) {
            uri = Uri.parse(uriString);
        }

        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(track.getDescription().getTitle())
                .setSubtitle(track.getDescription().getSubtitle())
                .setDescription(track.getDescription().getDescription())
                .setIconBitmap(track.getDescription().getIconBitmap())
                .setIconUri(track.getDescription().getIconUri())
                .setMediaUri(uri)
                .setExtras(bundle).build();

        // We don't expect queues to change after created, so we use the item index as the
        // queueId. Any other number unique in the queue would work.
        return new MediaSessionCompat.QueueItem(description, id);
    }

    public static MediaSessionCompat.QueueItem createQueueItem(Context context, Uri uri) {
        List<String> pathSegments = uri.getPathSegments();
        String host = uri.getHost();
        String scheme = uri.getScheme();
        String albumName;
        String trackName;
        String artistName;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, uri);
            albumName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            artistName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            trackName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            retriever.release();
        } catch (Exception e) {
            return null;
        }
        if (TextUtils.isEmpty(trackName) && pathSegments != null) {
            trackName = pathSegments.get(pathSegments.size() - 1);
        }
        return new MediaSessionCompat.QueueItem(new MediaDescriptionCompat.Builder()
                .setMediaUri(uri)
                .setMediaId(uri.toString())
                .setTitle(trackName)
                .setSubtitle(artistName)
                .setDescription("streaming from " + scheme)
                //.setIconUri(Uri.parse(mCoverArtUrl))
                .build(), 0);
    }

    public static MediaBrowserCompat.MediaItem createArtistMediaItem(String artist) {
        return MediaItemHelper.createBrowsableItem(new MediaDescriptionCompat.Builder()
                .setMediaId(MediaIDHelper.createMediaID(null, MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST, artist))
                .setTitle(MediaIDHelper.unescape(artist))
                .build());
    }

    public static MediaBrowserCompat.MediaItem createPlayableItem(MediaDescriptionCompat description) {
        return new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    public static MediaBrowserCompat.MediaItem createPlayableItem(MediaMetadataCompat metadata) {
        return createPlayableItem(convertToDescription(metadata));
    }

    public static MediaBrowserCompat.MediaItem createBrowsableItem(MediaDescriptionCompat description) {
        return new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    public static MediaBrowserCompat.MediaItem createBrowsableItem(String mediaId, String title) {
        return createBrowsableItem(new MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(title)
                .build());
    }

    private static String fetchString(MediaMetadataCompat metadata, String key) {
        if (metadata.containsKey(key)) {
            return metadata.getString(key);
        }
        return null;
    }

    private static Long fetchLong(MediaMetadataCompat metadata, String key) {
        if (metadata.containsKey(key)) {
            return metadata.getLong(key);
        }
        return null;
    }
}
