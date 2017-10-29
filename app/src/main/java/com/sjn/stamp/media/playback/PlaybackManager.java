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

package com.sjn.stamp.media.playback;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.sjn.stamp.R;
import com.sjn.stamp.model.constant.RepeatState;
import com.sjn.stamp.controller.SongHistoryController;
import com.sjn.stamp.media.CustomController;
import com.sjn.stamp.media.MediaLogger;
import com.sjn.stamp.media.QueueManager;
import com.sjn.stamp.media.provider.MusicProvider;
import com.sjn.stamp.utils.AnalyticsHelper;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.MediaItemHelper;
import com.sjn.stamp.utils.TimeHelper;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Manage the interactions among the container service, the queue manager and the actual playback.
 */
public class PlaybackManager implements Playback.Callback, MediaLogger.Listener {

    private static final String TAG = LogHelper.makeLogTag(PlaybackManager.class);
    // Action to thumbs up a media item
    private static final String CUSTOM_ACTION_THUMBS_UP = "com.sjn.stamp.THUMBS_UP";

    private Context mContext;
    private MusicProvider mMusicProvider;
    private QueueManager mQueueManager;
    private Resources mResources;
    private Playback mPlayback;
    private PlaybackServiceCallback mServiceCallback;
    private MediaSessionCallback mMediaSessionCallback;
    private MediaLogger mMediaLogger;
    private SongHistoryController mSongHistoryController;

    public PlaybackManager(Context context, PlaybackServiceCallback serviceCallback, Resources resources,
                           MusicProvider musicProvider, QueueManager queueManager,
                           Playback playback, SongHistoryController songHistoryController) {
        mContext = context;
        mMusicProvider = musicProvider;
        mServiceCallback = serviceCallback;
        mResources = resources;
        mQueueManager = queueManager;
        mMediaSessionCallback = new MediaSessionCallback();
        mPlayback = playback;
        mPlayback.setCallback(this);
        mMediaLogger = new MediaLogger(this);
        mSongHistoryController = songHistoryController;
    }

    public Playback getPlayback() {
        return mPlayback;
    }

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    public void startNewQueue(String title, String mediaId, @NotNull List<MediaSessionCompat.QueueItem> queueItemList) {
        mMediaLogger.onSkip(getCurrentMedia(), getCurrentPosition());
        AnalyticsHelper.trackCategory(mContext, mediaId);
        mQueueManager.setCurrentQueue(title, queueItemList, mediaId);
        mQueueManager.updateMetadata();
        handlePlayRequest();
    }

    /**
     * Handle a request to play music
     */
    public void handlePlayRequest() {
        LogHelper.d(TAG, "handlePlayRequest: mState=" + mPlayback.getState());
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            int playBackState = mPlayback.getState();
            if (playBackState == PlaybackStateCompat.STATE_PLAYING || playBackState == PlaybackStateCompat.STATE_STOPPED || playBackState == PlaybackStateCompat.STATE_NONE) {
                mMediaLogger.onStart();
            }
            mServiceCallback.onPlaybackStart();
            mPlayback.play(currentMusic);
        }
    }

    /**
     * Handle a request to pause music
     */
    public void handlePauseRequest() {
        LogHelper.d(TAG, "handlePauseRequest: mState=" + mPlayback.getState());
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
            mServiceCallback.onPlaybackStop();
        }
    }

    /**
     * Handle a request to stop music
     *
     * @param withError Error message in case the stop has an unexpected cause. The error
     *                  message will be set in the PlaybackState and will be visible to
     *                  MediaController clients.
     */
    public void handleStopRequest(String withError) {
        LogHelper.d(TAG, "handleStopRequest: mState=" + mPlayback.getState() + " error=", withError);
        mPlayback.stop(true);
        mServiceCallback.onPlaybackStop();
        updatePlaybackState(withError);
    }


    /**
     * Update the current media player state, optionally showing an error message.
     *
     * @param error if not null, error message to present to the user.
     */
    public void updatePlaybackState(String error) {
        LogHelper.d(TAG, "updatePlaybackState, playback state=" + mPlayback.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }

        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        setCustomAction(stateBuilder);
        int state = mPlayback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        // Set the activeQueueItemId if the current index is valid.
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
        }

        mServiceCallback.onPlaybackStateUpdated(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            mServiceCallback.onNotificationRequired();
        }
    }

    private void setCustomAction(PlaybackStateCompat.Builder stateBuilder) {
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic == null) {
            return;
        }
        // Set appropriate "Favorite" icon on Custom action:
        String mediaId = currentMusic.getDescription().getMediaId();
        if (mediaId == null) {
            return;
        }
        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
        int favoriteIcon = mMusicProvider.isFavorite(musicId) ?
                R.drawable.ic_star_on : R.drawable.ic_star_off;
        LogHelper.d(TAG, "updatePlaybackState, setting Favorite custom action of music ",
                musicId, " current favorite=", mMusicProvider.isFavorite(musicId));
        Bundle customActionExtras = new Bundle();
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                CUSTOM_ACTION_THUMBS_UP, mResources.getString(R.string.favorite), favoriteIcon)
                .setExtras(customActionExtras)
                .build());
    }

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }

    /**
     * Implementation of the Playback.Callback interface
     */
    @Override
    public void onCompletion() {
        mMediaLogger.onComplete(getCurrentMedia());
        // The media player finished playing the current song, so we go ahead
        // and start the next.
        int skipCount = CustomController.getInstance().getRepeatState() == RepeatState.ONE ? 0 : 1;
        if (mQueueManager.skipQueuePosition(skipCount)) {
            handlePlayRequest();
            mQueueManager.updateMetadata();
        } else {
            // If skipping was not possible, we stop and release the resources:
            handleStopRequest(null);
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        mMediaLogger.onSkip(getCurrentMedia(), getCurrentPosition());
        LogHelper.d(TAG, "setCurrentMediaId", mediaId);
        mQueueManager.setQueueFromMusic(mediaId);
    }


    /**
     * Switch to a different Playback instance, maintaining all playback state, if possible.
     *
     * @param playback switch to this playback
     */
    public void switchToPlayback(Playback playback, boolean resumePlaying) {
        if (playback == null) {
            throw new IllegalArgumentException("Playback cannot be null");
        }
        // suspend the current one.
        int oldState = mPlayback.getState();
        int pos = mPlayback.getCurrentStreamPosition();
        String currentMediaId = mPlayback.getCurrentMediaId();
        mPlayback.stop(false);
        playback.setCallback(this);
        playback.setCurrentStreamPosition(pos < 0 ? 0 : pos);
        playback.setCurrentMediaId(currentMediaId);
        playback.start();
        // finally swap the instance
        mPlayback = playback;
        switch (oldState) {
            case PlaybackStateCompat.STATE_BUFFERING:
            case PlaybackStateCompat.STATE_CONNECTING:
            case PlaybackStateCompat.STATE_PAUSED:
                mPlayback.pause();
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
                if (resumePlaying && currentMusic != null) {
                    mPlayback.play(currentMusic);
                } else if (!resumePlaying) {
                    mPlayback.pause();
                } else {
                    mPlayback.stop(true);
                }
                break;
            case PlaybackStateCompat.STATE_NONE:
                break;
            default:
                LogHelper.d(TAG, "Default called. Old state is ", oldState);
        }
    }

    @Override
    public MediaMetadataCompat getCurrentMedia() {
        return mMusicProvider.getMusicByMediaId(mPlayback.getCurrentMediaId());
    }

    @Override
    public int getPlaybackState() {
        return mPlayback.getState();
    }

    @Override
    public int getCurrentPosition() {
        return mPlayback.getCurrentStreamPosition();
    }

    @Override
    public void onSongStart(@NonNull MediaMetadataCompat metadata) {
        mSongHistoryController.onStart(metadata, TimeHelper.getJapanNow().toDate());
    }

    @Override
    public void onSongPlay(@NonNull MediaMetadataCompat metadata) {
        mSongHistoryController.onPlay(metadata, TimeHelper.getJapanNow().toDate());
    }

    @Override
    public void onSongSkip(@NonNull MediaMetadataCompat metadata) {
        mSongHistoryController.onSkip(metadata, TimeHelper.getJapanNow().toDate());
    }

    @Override
    public void onSongComplete(@NonNull MediaMetadataCompat metadata) {
        mSongHistoryController.onComplete(metadata, TimeHelper.getJapanNow().toDate());
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {

            MediaSessionCompat.QueueItem queueItem = MediaItemHelper.createQueueItem(mContext, uri);
            if (queueItem == null) {
                return;
            }
            int playBackState = mPlayback.getState();
            if (playBackState == PlaybackStateCompat.STATE_PLAYING || playBackState == PlaybackStateCompat.STATE_STOPPED || playBackState == PlaybackStateCompat.STATE_NONE) {
                mMediaLogger.onStart();
            }
            mServiceCallback.onPlaybackStart();
            mPlayback.play(queueItem);
        }

        @Override
        public void onPlay() {
            LogHelper.d(TAG, "play");
            if (mQueueManager.getCurrentMusic() == null) {
                mQueueManager.setRandomQueue();
            }
            handlePlayRequest();
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            mMediaLogger.onSkip(getCurrentMedia(), getCurrentPosition());
            LogHelper.d(TAG, "OnSkipToQueueItem:" + queueId);
            mQueueManager.setCurrentQueueItem(queueId);
            mQueueManager.updateMetadata();
        }

        @Override
        public void onSeekTo(long position) {
            LogHelper.d(TAG, "onSeekTo:", position);
            mPlayback.seekTo((int) position);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            mMediaLogger.onSkip(getCurrentMedia(), getCurrentPosition());
            LogHelper.d(TAG, "playFromMediaId mediaId:", mediaId, "  extras=", extras);
            AnalyticsHelper.trackCategory(mContext, mediaId);
            mQueueManager.setQueueFromMusic(mediaId);
            handlePlayRequest();
        }

        @Override
        public void onPause() {
            LogHelper.d(TAG, "pause. current state=" + mPlayback.getState());
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            LogHelper.d(TAG, "stop. current state=" + mPlayback.getState());
            handleStopRequest(null);
        }

        @Override
        public void onSkipToNext() {
            mMediaLogger.onSkip(getCurrentMedia(), getCurrentPosition());
            LogHelper.d(TAG, "skipToNext");
            if (mQueueManager.skipQueuePosition(1)) {
                handlePlayRequest();
            } else {
                mQueueManager.skipTo0();
                handlePlayRequest();
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void onSkipToPrevious() {
            long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
            if (mPlayback != null && mPlayback.isConnected()) {
                position = mPlayback.getCurrentStreamPosition();
            }
            if (position > 2000) {
                onSeekTo(0);
                return;
            }
            if (mQueueManager.skipQueuePosition(-1)) {
                handlePlayRequest();
            } else {
                mQueueManager.skipTo0();
                handlePlayRequest();
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void onCustomAction(@NonNull String action, Bundle extras) {
            if (CUSTOM_ACTION_THUMBS_UP.equals(action)) {
                LogHelper.i(TAG, "onCustomAction: favorite for current track");
                MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
                if (currentMusic != null) {
                    String mediaId = currentMusic.getDescription().getMediaId();
                    if (mediaId != null) {
                        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
                        mMusicProvider.setFavorite(musicId, !mMusicProvider.isFavorite(musicId));
                    }
                }
                // playback state needs to be updated because the "Favorite" icon on the
                // custom action will change to reflect the new favorite state.
                updatePlaybackState(null);
            } else {
                LogHelper.e(TAG, "Unsupported action: ", action);
            }
        }

        /**
         * Handle free and contextual searches.
         * <p/>
         * All voice searches on Android Auto are sent to this method through a connected
         * {@link android.support.v4.media.session.MediaControllerCompat}.
         * <p/>
         * Threads and async handling:
         * Search, as a potentially slow operation, should run in another thread.
         * <p/>
         * Since this method runs on the main thread, most apps with non-trivial metadata
         * should defer the actual search to another thread (for example, by using
         * an {@link AsyncTask} as we do here).
         **/
        @Override
        public void onPlayFromSearch(final String query, final Bundle extras) {
            LogHelper.d(TAG, "playFromSearch  query=", query, " extras=", extras);

            mPlayback.setState(PlaybackStateCompat.STATE_CONNECTING);
            boolean successSearch = mQueueManager.setQueueFromSearch(query, extras);
            if (successSearch) {
                handlePlayRequest();
                mQueueManager.updateMetadata();
            } else {
                updatePlaybackState("Could not find music");
            }
        }
    }

    public interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
}
