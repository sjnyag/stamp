/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sjn.stamp.media.provider;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.sjn.stamp.media.provider.single.QueueProvider;
import com.sjn.stamp.media.source.MusicProviderSource;
import com.sjn.stamp.ui.observer.MusicListObserver;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.sjn.stamp.utils.MediaIDHelper.MEDIA_ID_ROOT;

/**
 * Simple data provider for music tracks. The actual metadata source is delegated to a
 * MusicProviderSource defined by a constructor argument of this class.
 */
public class MusicProvider {

    private static final String TAG = LogHelper.makeLogTag(MusicProvider.class);

    private MusicProviderSource mSource;

    // Categorized caches for music track data:
    private HashMap<String, MediaMetadataCompat> mMusicListById;
    private final Map<ProviderType, ListProvider> mListProviderMap = new HashMap<>();
    private final Set<String> mFavoriteTracks;
    private Context mContext;
    private RetrieveMediaAsyncTask mAsyncTask;

    private volatile ListProvider.ProviderState mCurrentState = ListProvider.ProviderState.NON_INITIALIZED;

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    public MusicProvider(Context context, MusicProviderSource source) {
        mContext = context;
        mSource = source;
        mMusicListById = new HashMap<>();
        mFavoriteTracks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        for (ProviderType providerType : ProviderType.values()) {
            mListProviderMap.put(providerType, providerType.newProvider(context));
        }
    }

    public void setQueueListener(QueueProvider.QueueListener queueListener) {
        ListProvider provider = getProvider(ProviderType.QUEUE);
        if (provider != null && provider instanceof QueueProvider) {
            ((QueueProvider) provider).setQueueListener(queueListener);
        }
    }

    public List<MediaMetadataCompat> getMusicsHierarchy(String categoryType, String categoryValue) {
        LogHelper.d(TAG, "getMusicsHierarchy categoryType: ", categoryType, ", categoryValue: ", categoryValue);
        ListProvider listProvider = mListProviderMap.get(ProviderType.of(categoryType));
        if (listProvider == null) {
            return null;
        }
        return listProvider.getListByKey(categoryValue, mCurrentState, mMusicListById);
    }

    /**
     * Get an iterator over a shuffled collection of all songs
     */
    public Iterable<MediaMetadataCompat> getShuffledMusic() {
        if (mCurrentState != ListProvider.ProviderState.INITIALIZED) {
            return Collections.emptyList();
        }
        List<MediaMetadataCompat> shuffled = new ArrayList<>(mMusicListById.size());
        for (MediaMetadataCompat metadata : mMusicListById.values()) {
            shuffled.add(metadata);
        }
        Collections.shuffle(shuffled);
        return shuffled;
    }

    /**
     * Very basic implementation of a search that filter music tracks with title containing
     * the given query.
     */
    public Iterable<MediaMetadataCompat> searchMusicBySongTitle(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_TITLE, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with album containing
     * the given query.
     */
    public Iterable<MediaMetadataCompat> searchMusicByAlbum(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ALBUM, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with artist containing
     * the given query.
     */
    public Iterable<MediaMetadataCompat> searchMusicByArtist(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ARTIST, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with artist containing
     * the given query.
     */
    public Iterable<MediaMetadataCompat> searchMusicByGenre(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_GENRE, query);
    }

    Iterable<MediaMetadataCompat> searchMusic(String metadataField, String query) {
        if (mCurrentState != ListProvider.ProviderState.INITIALIZED || query == null || query.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<MediaMetadataCompat> result = new ArrayList<>();
        query = query.toLowerCase(Locale.US);
        for (MediaMetadataCompat metadata : mMusicListById.values()) {
            if (metadata.getString(metadataField).toLowerCase(Locale.US)
                    .contains(query)) {
                result.add(metadata);
            }
        }
        return result;
    }


    /**
     * Return the MediaMetadataCompat for the given musicID.
     *
     * @param musicId The unique, non-hierarchical music ID.
     */
    public MediaMetadataCompat getMusicByMusicId(String musicId) {
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId) : null;
    }

    public MediaMetadataCompat getMusicByQueueItem(MediaSessionCompat.QueueItem item) {
        if (item == null) {
            return null;
        }
        return getMusicByMediaId(item.getDescription().getMediaId());
    }

    public MediaMetadataCompat getMusicByMediaId(String mediaId) {
        if (mediaId == null) {
            return null;
        }
        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
        if (musicId == null) {
            return null;
        }
        return getMusicByMusicId(musicId);
    }

    public synchronized void updateMusicArt(String musicId, Bitmap albumArt, Bitmap icon) {
        MediaMetadataCompat metadata = getMusicByMusicId(musicId);
        metadata = new MediaMetadataCompat.Builder(metadata)

                // set high resolution bitmap in METADATA_KEY_ALBUM_ART. This is used, for
                // example, on the lockscreen background when the media session is active.
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)

                // set small version of the album art in the DISPLAY_ICON. This is used on
                // the MediaDescription and thus it should be small to be serialized if
                // necessary
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon)

                .build();
        mMusicListById.put(musicId, metadata);
    }

    public void setFavorite(String musicId, boolean favorite) {
        if (favorite) {
            mFavoriteTracks.add(musicId);
        } else {
            mFavoriteTracks.remove(musicId);
        }
    }

    public boolean isInitialized() {
        return mCurrentState == ListProvider.ProviderState.INITIALIZED;
    }

    public boolean isFavorite(String musicId) {
        return mFavoriteTracks.contains(musicId);
    }

    public List<MediaBrowserCompat.MediaItem> getChildren(String mediaId, Resources resources) {
        LogHelper.i(TAG, "getChildren mediaId: " + mediaId);
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        if (!MediaIDHelper.isBrowseable(mediaId)) {
            return mediaItems;
        }

        if (MEDIA_ID_ROOT.equals(mediaId)) {
            for (ListProvider listProvider : mListProviderMap.values()) {
                mediaItems.add(listProvider.getRootMenu(resources));
            }
        } else {
            ProviderType type = ProviderType.of(mediaId);
            if (type != null) {
                return getProvider(type).getListItems(mediaId, resources, mCurrentState, mMusicListById);
            }
        }
        return mediaItems;
    }

    private ListProvider getProvider(ProviderType type) {
        if (type == null || !mListProviderMap.containsKey(type)) {
            return null;
        }
        return mListProviderMap.get(type);
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     */
    synchronized public void retrieveMediaAsync(final Callback callback) {
        LogHelper.d(TAG, "retrieveMediaAsync called");
        if (mCurrentState == ListProvider.ProviderState.INITIALIZED) {
            if (callback != null) {
                // Nothing to do, execute callback immediately
                callback.onMusicCatalogReady(true);
            }
            return;
        }
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        mAsyncTask = new RetrieveMediaAsyncTask(callback);
        mAsyncTask.execute();
    }

    public void cacheAndNotifyLatestMusicMap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMusicListById = createLatestMusicMap();
                ProviderCache.saveCache(mContext, mMusicListById);
                MusicListObserver.getInstance().notifyMediaListUpdated();
            }
        }).start();
    }

    private HashMap<String, MediaMetadataCompat> createLatestMusicMap() {
        HashMap<String, MediaMetadataCompat> musicListById = new HashMap<>();
        Iterator<MediaMetadataCompat> tracks = mSource.iterator();
        while (tracks.hasNext()) {
            MediaMetadataCompat item = tracks.next();
            String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            musicListById.put(musicId, item);
        }
        return musicListById;
    }

    private class RetrieveMediaAsyncTask extends AsyncTask<Void, Void, ListProvider.ProviderState> {
        final Callback callback;

        RetrieveMediaAsyncTask(final Callback callback) {
            this.callback = callback;
        }

        @Override
        protected ListProvider.ProviderState doInBackground(Void... params) {
            retrieveMedia();
            LogHelper.i(TAG, "RetrieveMediaAsyncTask.doInBackground end");
            return mCurrentState;
        }

        @Override
        protected void onPostExecute(ListProvider.ProviderState current) {
            if (callback != null) {
                callback.onMusicCatalogReady(current == ListProvider.ProviderState.INITIALIZED);
            }
            cacheAndNotifyLatestMusicMap();
        }

        @Override
        protected void onCancelled(ListProvider.ProviderState current) {
            if (callback != null) {
                callback.onMusicCatalogReady(current == ListProvider.ProviderState.INITIALIZED);
            }
            mCurrentState = ListProvider.ProviderState.NON_INITIALIZED;
        }

        private synchronized void retrieveMedia() {
            try {
                if (mCurrentState == ListProvider.ProviderState.NON_INITIALIZED) {
                    mCurrentState = ListProvider.ProviderState.INITIALIZING;
                    mMusicListById = ProviderCache.readCache(mContext);
                    LogHelper.i(TAG, "Read " + mMusicListById.size() + " songs from cache");
                    if (mMusicListById.size() == 0) {
                        mMusicListById = createLatestMusicMap();
                    }
                    mCurrentState = ListProvider.ProviderState.INITIALIZED;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCurrentState != ListProvider.ProviderState.INITIALIZED) {
                    // Something bad happened, so we reset ProviderState to NON_INITIALIZED to allow
                    // retries (eg if the network connection is temporary unavailable)
                    mCurrentState = ListProvider.ProviderState.NON_INITIALIZED;
                }
            }
        }
    }

}
