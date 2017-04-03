package com.sjn.taggingplayer.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.util.SparseArray;

import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirPutCallback;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.sjn.taggingplayer.media.source.MusicProviderSource;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.sjn.taggingplayer.media.source.MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE;

public class MediaRetrieveHelper {
    private static final String TAG = LogHelper.makeLogTag(MediaRetrieveHelper.class);

    private static final String[] GENRE_PROJECTION = {
            MediaStore.Audio.Genres.NAME,
            MediaStore.Audio.Genres._ID};

    private static final String[] MEDIA_PROJECTION = {
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_MODIFIED,
    };

    private static final String ALL_MUSIC_SELECTION = MediaStore.Audio.Media.IS_MUSIC + " != 0";
    private static final int DISK_CACHE_SIZE = 1024 * 1024; // 1MB
    private static final String CACHE_KEY = "media_source";

    private static final Type CACHE_TYPE = new TypeToken<List<MediaCursorContainer>>() {
    }.getType();

    public static Iterator<MediaMetadataCompat> createIterator(List<MediaCursorContainer> list) {
        return Lists.transform(list, new Function<MediaCursorContainer, MediaMetadataCompat>() {
            @Override
            public MediaMetadataCompat apply(MediaCursorContainer mediaCursorContainer) {
                return mediaCursorContainer.buildMediaMetadataCompat();
            }
        }).iterator();
    }

    public static MediaMetadataCompat findByMusicId(Context context, long musicId) {
        MediaMetadataCompat metadata = null;
        //TODO
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId);
        Cursor mediaCursor = context.getContentResolver().query(uri, MEDIA_PROJECTION, ALL_MUSIC_SELECTION, null, null);
        if (mediaCursor != null && mediaCursor.moveToFirst()) {
            metadata = parseCursor(mediaCursor, null).buildMediaMetadataCompat();
            mediaCursor.close();
        }
        return metadata;
    }

    public static String findAlbumArtByArtist(Context context, String artist) {
        //TODO
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor mediaCursor = context.getContentResolver().query(uri, new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums.ARTIST + "=?",
                new String[]{artist}, null);
        if (mediaCursor != null && mediaCursor.moveToFirst()) {
            Long albumId = mediaCursor.getLong(mediaCursor.getColumnIndex(MediaStore.Audio.Albums._ID));
            mediaCursor.close();
            return makeAlbumArtUri(albumId).toString();
        }
        return "";
    }

    public static boolean initCache(Context context) {
        try {
            Reservoir.init(context, MediaRetrieveHelper.DISK_CACHE_SIZE);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static List<MediaCursorContainer> readCache() {
        try {
            if (Reservoir.contains(CACHE_KEY)) {
                return Reservoir.get(CACHE_KEY, CACHE_TYPE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    public static List<MediaCursorContainer> retrieveAllMedia(Context context, boolean hasStoragePermission) {
        List<MediaCursorContainer> mediaList = new ArrayList<>();
        //FIXME
        SparseArray<String> genreMap = createGenreMap(context, false);
        Uri uri = hasStoragePermission ? MediaStore.Audio.Media.EXTERNAL_CONTENT_URI : MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        Cursor mediaCursor = context.getContentResolver().query(
                uri, MEDIA_PROJECTION, ALL_MUSIC_SELECTION, null, null);
        if (mediaCursor != null && mediaCursor.moveToFirst()) {
            do {
                mediaList.add(parseCursor(mediaCursor, genreMap));
            } while (mediaCursor.moveToNext());
            mediaCursor.close();
        }
        return mediaList;
    }

    public static void retrieveAndUpdateCache(Context context, boolean hasStoragePermission, MusicProviderSource.OnListChangeListener listener) {
        new CacheUpdateAsyncTask(context, hasStoragePermission, listener).execute();
    }

    private static SparseArray<String> createGenreMap(Context context, boolean hasStoragePermission) {
        SparseArray<String> genreMap = new SparseArray<>();
        if (!hasStoragePermission) {
            return genreMap;
        }
        try {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, GENRE_PROJECTION, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    genreMap.put(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME))
                    );
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return genreMap;

    }

    private static MediaCursorContainer parseCursor(Cursor cursor, SparseArray<String> genreMap) {
        String title = cursor.getString(0);
        String artist = cursor.getString(1);
        String album = cursor.getString(2);
        long duration = cursor.getLong(3);
        String source = cursor.getString(4);
        int trackNumber = cursor.getInt(5);
        long totalTrackCount = cursor.getLong(6);
        String musicId = cursor.getString(7);
        long albumId = cursor.getLong(8);
        String dateAdded = TimeHelper.toRFC3339(cursor.getLong(10));
        String genre = "";
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
        return new MediaCursorContainer(musicId, source, album, artist, duration, genre, makeAlbumArtUri(albumId).toString(), title, trackNumber, totalTrackCount, dateAdded);
    }

    static Uri makeAlbumArtUri(long albumId) {
        Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(albumArtUri, albumId);
    }

    @Accessors(prefix = "m")
    @Getter
    @Setter
    @AllArgsConstructor(suppressConstructorProperties = true)
    public static class MediaCursorContainer {
        String mMusicId;
        String mSource;
        String mAlbum;
        String mArtist;
        long mDuration;
        String mGenre;
        String mAlbumArtUri;
        String mTitle;
        int mTrackNumber;
        long mTotalTrackCount;
        String mDateAdded;

        private MediaMetadataCompat buildMediaMetadataCompat() {
            //noinspection ResourceType
            return new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mMusicId)
                    .putString(CUSTOM_METADATA_TRACK_SOURCE, mSource)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mAlbum)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mArtist)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mDuration)
                    .putString(MediaMetadataCompat.METADATA_KEY_GENRE, mGenre)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, mAlbumArtUri)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mTitle)
                    .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, mTrackNumber)
                    .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, mTotalTrackCount)
                    .putString(MediaMetadataCompat.METADATA_KEY_DATE, mDateAdded)
                    .build();
        }
    }

    private static class CacheUpdateAsyncTask extends AsyncTask<Void, Void, String> {

        private Context mContext;
        private boolean mHasStoragePermission;
        private MusicProviderSource.OnListChangeListener mListener;

        CacheUpdateAsyncTask(Context context, boolean hasStoragePermission, MusicProviderSource.OnListChangeListener listener) {
            mContext = context;
            mHasStoragePermission = hasStoragePermission;
            mListener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            List<MediaCursorContainer> trackList = retrieveAllMedia(mContext, mHasStoragePermission);
            writeCache(trackList);
            if (mListener != null) {
                mListener.onSourceChange(createIterator(trackList));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        private static void writeCache(final List<MediaCursorContainer> list) {
            Reservoir.putAsync(CACHE_KEY, list, new ReservoirPutCallback() {
                @Override
                public void onSuccess() {
                    LogHelper.i(TAG, "Write " + list.size() + "songs to cache");
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

}

