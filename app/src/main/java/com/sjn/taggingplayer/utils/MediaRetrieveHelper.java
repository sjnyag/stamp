package com.sjn.taggingplayer.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirPutCallback;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.sjn.taggingplayer.model.MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE;

/**
 * Created by sjnya on 2016/04/15.
 */
public class MediaRetrieveHelper {
    private static final String TAG = LogHelper.makeLogTag(MediaRetrieveHelper.class);
    private static final String UNKNOWN_TAG = "<unknown>";

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
            MediaStore.Audio.Media.DATE_ADDED};

    private static final String MUSIC_SELECTION = MediaStore.Audio.Media.IS_MUSIC + " != 0";
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
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

    public static void writeCache(final List<MediaCursorContainer> list) {
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

    public static void updateCache(Context context) {
        new CacheUpdateAsyncTask(context).execute();
    }

    public static List<MediaCursorContainer> retrieve(Context context) {
        return uniqueMerge(retrieveMediaByGenre(context), retrieveAllMedia(context));
    }

    private static List<MediaCursorContainer> retrieveAllMedia(Context context) {
        return fetchMediaWithGenre(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, UNKNOWN_TAG);
    }

    private static List<MediaCursorContainer> retrieveMediaByGenre(Context context) {
        List<MediaCursorContainer> mediaList = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, GENRE_PROJECTION, null, null, null);

        if (cursor == null) {
            return null;
        }
        if (cursor.moveToFirst()) {
            do {
                mediaList = merge(mediaList, fetchMediaWithGenre(context, getGenreCursorUri(cursor), getGenre(cursor)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return mediaList;
    }

    private static List<MediaCursorContainer> fetchMediaWithGenre(Context context, Uri uri, String genre) {
        List<MediaCursorContainer> mediaList = new ArrayList<>();

        Cursor mediaCursor = context.getContentResolver().query(
                uri, MEDIA_PROJECTION, MUSIC_SELECTION, null, null);

        if (mediaCursor == null) {
            return null;
        }
        if (mediaCursor.moveToFirst()) {
            do {
                mediaList.add(parseCursor(mediaCursor, genre));
            } while (mediaCursor.moveToNext());
        }
        mediaCursor.close();
        return mediaList;
    }

    private static MediaCursorContainer parseCursor(Cursor cursor, String genre) {
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
        return new MediaCursorContainer(musicId, source, album, artist, duration, genre, getAlbumArtUri(albumId).toString(), title, trackNumber, totalTrackCount, dateAdded);
    }

    private static String getGenre(Cursor genreCursor) {
        String genre = UNKNOWN_TAG;
        try {
            genre = genreCursor.getString(
                    genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (genre != null && genre.trim().equals("")) {
            genre = UNKNOWN_TAG;
        }
        return genre;
    }

    private static Uri getGenreCursorUri(Cursor genreCursor) {
        int index = genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID);
        long genreId = Long.parseLong(genreCursor.getString(index));
        return MediaStore.Audio.Genres.Members.getContentUri("external", genreId);
    }

    public static Uri getAlbumArtUri(long albumId) {
        Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(albumArtUri, albumId);
    }

    private static List<MediaCursorContainer> uniqueMerge(List<MediaCursorContainer> srcList, List<MediaCursorContainer> newList) {
        if (newList == null) {
            return srcList;
        }
        if (srcList == null) {
            return newList;
        }

        if (srcList.size() != 0 && newList.size() != 0) {
            ConcurrentMap<String, MediaCursorContainer> srcMap = toMap(srcList);
            for (MediaCursorContainer media : newList) {
                if (!srcMap.containsKey(media.getMusicId())) {
                    srcList.add(media);
                }
            }
        } else if (srcList.size() == 0 && newList.size() != 0) {
            srcList = newList;
        }
        return srcList;
    }

    private static List<MediaCursorContainer> merge(List<MediaCursorContainer> srcList, List<MediaCursorContainer> newList) {
        if (newList == null) {
            return srcList;
        }
        if (srcList == null) {
            return newList;
        }

        if (srcList.size() != 0 && newList.size() != 0) {
            srcList.addAll(newList);
        } else if (srcList.size() == 0 && newList.size() != 0) {
            srcList = newList;
        }
        return srcList;
    }

    private static ConcurrentMap<String, MediaCursorContainer> toMap(List<MediaCursorContainer> metadataCompatList) {
        ConcurrentMap<String, MediaCursorContainer> map = new ConcurrentHashMap<>();
        if (metadataCompatList == null) {
            return map;
        }
        for (MediaCursorContainer media : metadataCompatList) {
            map.put(media.getMusicId(), media);
        }
        return map;
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

    static class CacheUpdateAsyncTask extends AsyncTask<Void, Void, String> {

        private Context mContext;

        public CacheUpdateAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            writeCache(retrieve(mContext));
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

}

