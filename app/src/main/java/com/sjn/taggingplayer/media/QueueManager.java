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

package com.sjn.taggingplayer.media;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.constant.RepeatState;
import com.sjn.taggingplayer.constant.ShuffleState;
import com.sjn.taggingplayer.controller.UserSettingController;
import com.sjn.taggingplayer.media.provider.MusicProvider;
import com.sjn.taggingplayer.media.provider.single.QueueProvider;
import com.sjn.taggingplayer.utils.BitmapHelper;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.MediaIDHelper;
import com.sjn.taggingplayer.utils.QueueHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Simple data provider for queues. Keeps track of a current queue and a current index in the
 * queue. Also provides methods to set the current queue based on common queries, relying on a
 * given MusicProvider to provide the actual media metadata.
 */
public class QueueManager implements QueueProvider.QueueListener, CustomController.ShuffleStateListener {
    private static final String TAG = LogHelper.makeLogTag(QueueManager.class);

    private Context mContext;
    private MusicProvider mMusicProvider;
    private MetadataUpdateListener mListener;
    private Resources mResources;

    // "Now playing" queue:
    private List<MediaSessionCompat.QueueItem> mOrderedQueue;
    private List<MediaSessionCompat.QueueItem> mShuffledQueue;
    private int mCurrentIndex;
    //to avoid GC
    private Target mTarget;

    @Override
    public void onShuffleStateChanged(ShuffleState state) {
        mShuffledQueue = new ArrayList<>(mOrderedQueue);
        shuffleQueue(mShuffledQueue, getCurrentMusic());
        setCurrentQueueIndex(0);
    }

    private List<MediaSessionCompat.QueueItem> getPlayingQueue() {
        if (CustomController.getInstance().getShuffleState() == ShuffleState.SHUFFLE) {
            return mShuffledQueue;
        }
        return mOrderedQueue;
    }

    private static void shuffleQueue(List<MediaSessionCompat.QueueItem> queueItemList, MediaSessionCompat.QueueItem initialQueue) {
        if (initialQueue != null) {
            queueItemList.remove(initialQueue);
            Collections.shuffle(queueItemList);
            queueItemList.add(0, initialQueue);
        } else {
            Collections.shuffle(queueItemList);
        }
    }

    public QueueManager(@NonNull Context context,
                        @NonNull MusicProvider musicProvider,
                        @NonNull Resources resources,
                        @NonNull MetadataUpdateListener listener) {
        this.mContext = context;
        this.mMusicProvider = musicProvider;
        this.mListener = listener;
        this.mResources = resources;
        CustomController.getInstance().addShuffleStateListenerSet(this);

        mOrderedQueue = Collections.synchronizedList(new ArrayList<MediaSessionCompat.QueueItem>());
        mShuffledQueue = Collections.synchronizedList(new ArrayList<MediaSessionCompat.QueueItem>());
        mCurrentIndex = 0;
    }

    public void restorePreviousState(String lastMusicId, String queueIdentifyMediaId) {
        setQueueFromMusic(queueIdentifyMediaId);
        if (lastMusicId != null && !lastMusicId.isEmpty() && mOrderedQueue != null) {
            for (int i = 0; i < mOrderedQueue.size(); i++) {
                if (lastMusicId.equals(MediaIDHelper.extractMusicIDFromMediaID(mOrderedQueue.get(i).getDescription().getMediaId()))) {
                    setCurrentQueueIndex(i);
                    break;
                }
            }
        }
        setCurrentQueueIndex(mCurrentIndex);
        onShuffleStateChanged(CustomController.getInstance().getShuffleState());
    }

    public boolean isSameBrowsingCategory(@NonNull String mediaId) {
        String[] newBrowseHierarchy = MediaIDHelper.getHierarchy(mediaId);
        MediaSessionCompat.QueueItem current = getCurrentMusic();
        if (current == null) {
            return false;
        }
        String[] currentBrowseHierarchy = MediaIDHelper.getHierarchy(
                current.getDescription().getMediaId());

        return Arrays.equals(newBrowseHierarchy, currentBrowseHierarchy);
    }

    private boolean setCurrentQueueIndex(int index) {
        if (index >= 0 && index < getPlayingQueue().size()) {
            mCurrentIndex = index;
            UserSettingController userSettingController = new UserSettingController(mContext);
            userSettingController.setLastMusicId(MediaIDHelper.extractMusicIDFromMediaID(getCurrentMusic().getDescription().getMediaId()));
            return true;
        }
        return false;
    }

    public boolean setCurrentQueueItem(long queueId) {
        // set the current index on queue from the queue Id:
        int index = QueueHelper.getMusicIndexOnQueue(getPlayingQueue(), queueId);
        if (setCurrentQueueIndex(index)) {
            mListener.onCurrentQueueIndexUpdated(mCurrentIndex);
        }
        return index >= 0;
    }

    public boolean setCurrentQueueItem(String mediaId) {
        // set the current index on queue from the music Id:
        int index = QueueHelper.getMusicIndexOnQueue(getPlayingQueue(), mediaId);
        if (setCurrentQueueIndex(index)) {
            mListener.onCurrentQueueIndexUpdated(mCurrentIndex);
        }
        return index >= 0;
    }

    public boolean skipQueuePosition(int amount) {
        int index = mCurrentIndex + amount;
        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = 0;
        } else if (CustomController.getInstance().getRepeatState() == RepeatState.ALL) {
            // skip forwards when in last song will cycle back to start of the queue
            index %= getPlayingQueue().size();
        }
        if (!QueueHelper.isIndexPlayable(index, getPlayingQueue())) {
            LogHelper.e(TAG, "Cannot increment queue index by ", amount,
                    ". Current=", mCurrentIndex, " queue length=", getPlayingQueue().size());
            return false;
        }
        setCurrentQueueIndex(index);
        return true;
    }

    public boolean setQueueFromSearch(String query, Bundle extras) {
        List<MediaSessionCompat.QueueItem> queue =
                QueueHelper.getPlayingQueueFromSearch(query, extras, mMusicProvider);
        setCurrentQueue(mResources.getString(R.string.search_queue_title), queue);
        updateMetadata();
        return queue != null && !queue.isEmpty();
    }

    public void setRandomQueue() {
        setCurrentQueue(mResources.getString(R.string.random_queue_title),
                QueueHelper.getRandomQueue(mMusicProvider));
        updateMetadata();
    }

    public void setQueueFromMusic(String mediaId) {
        LogHelper.d(TAG, "setQueueFromMusic", mediaId);

        // The mediaId used here is not the unique musicId. This one comes from the
        // MediaBrowser, and is actually a "hierarchy-aware mediaID": a concatenation of
        // the hierarchy in MediaBrowser and the actual unique musicID. This is necessary
        // so we can build the correct playing queue, based on where the track was
        // selected from.
        boolean canReuseQueue = false;
        if (isSameBrowsingCategory(mediaId)) {
            canReuseQueue = setCurrentQueueItem(mediaId);
        }
        if (!canReuseQueue) {
            String queueTitle = mResources.getString(R.string.browse_musics_by_genre_subtitle,
                    MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaId));
            setCurrentQueue(queueTitle,
                    QueueHelper.getPlayingQueue(mediaId, mMusicProvider), mediaId);
        }
        updateMetadata();
    }

    public MediaSessionCompat.QueueItem getCurrentMusic() {
        if (!QueueHelper.isIndexPlayable(mCurrentIndex, getPlayingQueue())) {
            return null;
        }
        return getPlayingQueue().get(mCurrentIndex);
    }

    public int getCurrentQueueSize() {
        if (getPlayingQueue() == null) {
            return 0;
        }
        return getPlayingQueue().size();
    }

    protected void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue) {
        setCurrentQueue(title, newQueue, null);
    }

    protected void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue,
                                   String initialMediaId) {
        mOrderedQueue = newQueue;
        int index = 0;
        if (initialMediaId != null) {
            index = QueueHelper.getMusicIndexOnQueue(mOrderedQueue, initialMediaId);
        }
        if (initialMediaId != null && !initialMediaId.startsWith(MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE)) {
            UserSettingController userSettingController = new UserSettingController(mContext);
            userSettingController.setQueueIdentifyMediaId(initialMediaId);
        }
        setCurrentQueueIndex(Math.max(index, 0));
        mListener.onQueueUpdated(title, newQueue);
    }

    public void updateMetadata() {
        MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            mListener.onMetadataRetrieveError();
            return;
        }
        final String musicId = MediaIDHelper.extractMusicIDFromMediaID(
                currentMusic.getDescription().getMediaId());
        MediaMetadataCompat metadata = mMusicProvider.getMusicByMusicId(musicId);
        if (metadata == null) {
            throw new IllegalArgumentException("Invalid musicId " + musicId);
        }

        mListener.onMetadataChanged(metadata);

        // Set the proper album artwork on the media session, so it can be shown in the
        // locked screen and in other places.
        if (metadata.getDescription().getIconBitmap() == null &&
                metadata.getDescription().getIconUri() != null) {
            final String albumUri = metadata.getDescription().getIconUri().toString();
            mTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Bitmap icon = BitmapHelper.createIcon(bitmap);
                    mMusicProvider.updateMusicArt(musicId, bitmap, icon);

                    // If we are still playing the same music, notify the listeners:
                    MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
                    if (currentMusic == null) {
                        return;
                    }
                    String currentPlayingId = MediaIDHelper.extractMusicIDFromMediaID(
                            currentMusic.getDescription().getMediaId());
                    if (musicId.equals(currentPlayingId)) {
                        mListener.onMetadataChanged(mMusicProvider.getMusicByMusicId(currentPlayingId));
                    }
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };
            BitmapHelper.readBitmapAsync(mContext, albumUri, mTarget);
        }
    }

    @Override
    public Iterable<MediaMetadataCompat> getPlayingQueueMetadata() {
        List<MediaMetadataCompat> queueList = new ArrayList<>();
        for (MediaSessionCompat.QueueItem queueItem : getPlayingQueue()) {
            queueList.add(
                    new MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, MediaIDHelper.extractMusicIDFromMediaID(queueItem.getDescription().getMediaId()))
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, queueItem.getDescription().getDescription().toString())
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, queueItem.getDescription().getSubtitle().toString())
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, queueItem.getDescription().getIconUri().toString())
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, queueItem.getDescription().getTitle().toString())
                            .build());
        }
        return queueList;
    }

    @Override
    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    @Override
    public RepeatState getRepeatState() {
        return CustomController.getInstance().getRepeatState();
    }

    public interface MetadataUpdateListener {
        void onMetadataChanged(MediaMetadataCompat metadata);

        void onMetadataRetrieveError();

        void onCurrentQueueIndexUpdated(int queueIndex);

        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue);
    }
}
