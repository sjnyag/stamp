package com.sjn.stamp.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.media.source.MusicProviderSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class LocalPlaylistHelper {

    private static final String TAG = LogHelper.makeLogTag(LocalPlaylistHelper.class);
    private static final Uri PLAYLIST_URI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
    private static final String[] COUNT_PROJECTION = {
            "count(*)"
    };
    private static final String[] MEDIA_PROJECTION = {
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Playlists.Members.AUDIO_ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DISPLAY_NAME
    };
    private static final String[] PLAYLIST_PROJECTION = {
            MediaStore.Audio.Playlists._ID,
            MediaStore.Audio.Playlists.NAME
    };
    private static final String PLAYLIST_ORDER = MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER;
    private static final String MEDIA_ORDER = MediaStore.Audio.Playlists.Members.PLAY_ORDER + " DESC";

    public static boolean isExistAudioId(ContentResolver resolver, int audioId, int playlistId) {
        Cursor mediaCursor = findAllMediaCursor(resolver, playlistId);
        if (mediaCursor.moveToFirst()) {
            do {
                if (String.valueOf(audioId).equals(mediaCursor.getString(7))) {
                    return true;
                }
            } while (mediaCursor.moveToNext());
        }
        mediaCursor.close();
        return false;
    }

    public static boolean isExistPlayListName(ContentResolver resolver, String name) {
        return name != null && name.length() > 0 && findPlaylistId(resolver, name) >= 0;
    }

    public static int findPlaylistId(ContentResolver resolver, String name) {
        int id = -1;
        Cursor cursor = findPlaylistByNameCursor(resolver, name);
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                id = cursor.getInt(0);
            }
            cursor.close();
        }
        return id;
    }

    public static String findPlaylistName(ContentResolver resolver, int playlistId) {
        String name = "";
        Cursor cursor = findPlaylistByIdCursor(resolver, playlistId);
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                name = cursor.getString(1);
            }
            cursor.close();
        }
        return name;
    }

    public static List<MediaMetadataCompat> findAllPlaylistMedia(ContentResolver resolver, int playlistId, String playlistTitle) {
        List<MediaMetadataCompat> mediaList = new ArrayList<>();
        Cursor mediaCursor = findAllMediaCursor(resolver, playlistId);
        if (mediaCursor.moveToFirst()) {
            do {
                mediaList.add(parseCursor(mediaCursor, playlistTitle));
            } while (mediaCursor.moveToNext());
        }
        mediaCursor.close();
        return mediaList;
    }

    public static ConcurrentMap<String, List<MediaMetadataCompat>> findAllPlaylist(ContentResolver resolver) {
        ConcurrentMap<String, List<MediaMetadataCompat>> playlistMap = new ConcurrentHashMap<>();
        Cursor playlistCursor = findAllPlaylistCursor(resolver);
        if (playlistCursor.moveToFirst()) {
            do {
                int id = playlistCursor.getInt(0);
                String title = playlistCursor.getString(1);
                playlistMap.put(title, findAllPlaylistMedia(resolver, id, title));
            } while (playlistCursor.moveToNext());
        }
        playlistCursor.close();
        return playlistMap;
    }

    public static boolean create(ContentResolver resolver, String name) {
        if (!isExistPlayListName(resolver, name)) {
            return false;
        }
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Audio.Playlists.NAME, name);
        Uri uri = resolver.insert(PLAYLIST_URI, values);
        return uri != null;
    }

    public static int update(ContentResolver resolver, String srcValue, String dstValue) {
        if (!isExistPlayListName(resolver, srcValue)) {
            return -1;
        }
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Audio.Playlists.NAME, dstValue);
        return resolver.update(PLAYLIST_URI, values, wherePlayList(resolver, srcValue), null);
    }

    public static int delete(ContentResolver resolver, String name) {
        if (!isExistPlayListName(resolver, name)) {
            return -1;
        }
        return resolver.delete(PLAYLIST_URI, wherePlayList(resolver, name), null);
    }

    //FIXME: remove duplication check
    //borrowed from http://stackoverflow.com/questions/3182937
    public static boolean add(ContentResolver resolver, int audioId, int playlistId) {
        if (isExistAudioId(resolver, audioId, playlistId)) {
            return false;
        }

        Uri uri = createPlaylistUrl(playlistId);
        int order = count(resolver, uri) + audioId;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, order);
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
        return resolver.insert(uri, values) != null;
    }

    //FIXME: all of same medias are removed
    public static boolean remove(ContentResolver resolver, int audioId, int playlistId) {
        if (isExistAudioId(resolver, audioId, playlistId)) {
            return false;
        }
        Uri uri = createPlaylistUrl(playlistId);
        return 0 < resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + " = " + audioId, null);
    }

    private static int count(ContentResolver resolver, Uri uri) {
        Cursor cur = resolver.query(uri, COUNT_PROJECTION, null, null, null);
        cur.moveToFirst();
        int base = cur.getInt(0);
        cur.close();
        return base;
    }

    private static Cursor findPlaylistByIdCursor(ContentResolver resolver, int playlistId) {
        return resolver.query(
                PLAYLIST_URI,
                PLAYLIST_PROJECTION, MediaStore.Audio.Playlists._ID + "= ?", new String[]{String.valueOf(playlistId)}, PLAYLIST_ORDER);
    }

    private static Cursor findPlaylistByNameCursor(ContentResolver resolver, String name) {
        return resolver.query(
                PLAYLIST_URI,
                PLAYLIST_PROJECTION, MediaStore.Audio.Playlists.NAME + "= ?", new String[]{name}, PLAYLIST_ORDER);
    }

    private static Cursor findAllPlaylistCursor(ContentResolver resolver) {
        return resolver.query(
                PLAYLIST_URI,
                PLAYLIST_PROJECTION, null, null, PLAYLIST_ORDER);
    }

    private static Cursor findAllMediaCursor(ContentResolver resolver, int playlistId) {
        return resolver.query(
                createPlaylistUrl(playlistId),
                MEDIA_PROJECTION, null, null, MEDIA_ORDER);
    }

    private static MediaMetadataCompat parseCursor(Cursor cursor, String playlistTitle) {
        String title = cursor.getString(0);
        String artist = cursor.getString(1);
        String album = cursor.getString(2);
        long duration = cursor.getLong(3);
        String source = cursor.getString(4);
        int trackNumber = cursor.getInt(5);
        long totalTrackCount = cursor.getLong(6);
        String musicId = cursor.getString(7);
        long albumId = cursor.getLong(8);

        //noinspection ResourceType
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, musicId)
                .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, playlistTitle)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, MediaRetrieveHelper.makeAlbumArtUri(albumId).toString())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();
    }

    private static Uri createPlaylistUrl(int playlistId) {
        return MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
    }

    private static String wherePlayList(ContentResolver resolver, String playlistName) {
        return MediaStore.Audio.Playlists._ID + " = " + findPlaylistId(resolver, playlistName);
    }

}