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
package com.sjn.stamp.ui.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sjn.stamp.constant.ShuffleState;
import com.sjn.stamp.media.CustomController;
import com.sjn.stamp.media.player.CastPlayer;
import com.sjn.stamp.ui.activity.MusicPlayerListActivity;
import com.sjn.stamp.ui.observer.MediaControllerObserver;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.ViewHelper;
import com.sjn.stamp.R;
import com.sjn.stamp.constant.RepeatState;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * A full screen player that shows the current playing music with a background image
 * depicting the album art. The activity also has controls to seek/pause/play the audio.
 */
public class FullScreenPlayerFragment extends Fragment implements CustomController.RepeatStateListener, CustomController.ShuffleStateListener, MediaControllerObserver.Listener {
    private static final String TAG = LogHelper.makeLogTag(FullScreenPlayerFragment.class);
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

    private ImageView mSkipPrev;
    private ImageView mSkipNext;
    private ImageView mPlayPause;
    private ImageView mShuffle;
    private ImageView mRepeat;
    private TextView mStart;
    private TextView mEnd;
    private SeekBar mSeekbar;
    private TextView mLine1;
    private TextView mLine2;
    private TextView mLine3;
    private ProgressBar mLoading;
    private View mControllers;
    private Drawable mPauseDrawable;
    private Drawable mPlayDrawable;
    private Drawable mNoRepeatDrawable;
    private Drawable mRepeatOneDrawable;
    private Drawable mRepeatAllDrawable;
    private Drawable mShuffleDrawable;
    private Drawable mNoShuffleDrawable;
    private ImageView mBackgroundImage;

    private String mCurrentArtUrl;
    private final Handler mHandler = new Handler();

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> mScheduleFuture;
    private PlaybackStateCompat mLastPlaybackState;

    @Override
    public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
        LogHelper.i(TAG, "onPlaybackstate changed", state);
        updatePlaybackState(state);
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        if (metadata != null) {
            updateMediaDescription(metadata.getDescription());
            updateDuration(metadata);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogHelper.i(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_full_player, container, false);

        mBackgroundImage = (ImageView) rootView.findViewById(R.id.background_image);
        mPauseDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.uamp_ic_pause_white_48dp);
        mPlayDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.uamp_ic_play_arrow_white_48dp);
        mRepeatOneDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_repeat_one_white_48dp);
        mRepeatAllDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_repeat_white_48dp);
        mNoRepeatDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_repeat_white_48dp);
        mShuffleDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_shuffle_white_48dp);
        mNoShuffleDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_shuffle_white_48dp);
        mNoRepeatDrawable.setAlpha(50);
        mNoShuffleDrawable.setAlpha(50);
        mPlayPause = (ImageView) rootView.findViewById(R.id.play_pause);
        mSkipNext = (ImageView) rootView.findViewById(R.id.next);
        mSkipPrev = (ImageView) rootView.findViewById(R.id.prev);
        mRepeat = (ImageView) rootView.findViewById(R.id.repeat);
        mShuffle = (ImageView) rootView.findViewById(R.id.shuffle);
        mStart = (TextView) rootView.findViewById(R.id.startText);
        mEnd = (TextView) rootView.findViewById(R.id.endText);
        mSeekbar = (SeekBar) rootView.findViewById(R.id.seekBar1);
        mLine1 = (TextView) rootView.findViewById(R.id.line1);
        mLine2 = (TextView) rootView.findViewById(R.id.line2);
        mLine3 = (TextView) rootView.findViewById(R.id.line3);
        mLoading = (ProgressBar) rootView.findViewById(R.id.progressBar1);
        mControllers = rootView.findViewById(R.id.controllers);

        mSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat controller = getController();
                if (controller == null) {
                    return;
                }
                controller.getTransportControls().skipToNext();
            }
        });

        mSkipPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat controller = getController();
                if (controller == null) {
                    return;
                }
                controller.getTransportControls().skipToPrevious();
            }
        });

        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat controller = getController();
                if (controller == null) {
                    return;
                }
                PlaybackStateCompat state = controller.getPlaybackState();
                if (state != null) {
                    MediaControllerCompat.TransportControls controls = controller.getTransportControls();
                    switch (state.getState()) {
                        case PlaybackStateCompat.STATE_PLAYING: // fall through
                        case PlaybackStateCompat.STATE_BUFFERING:
                            controls.pause();
                            stopSeekbarUpdate();
                            break;
                        case PlaybackStateCompat.STATE_PAUSED:
                        case PlaybackStateCompat.STATE_STOPPED:
                            controls.play();
                            scheduleSeekbarUpdate();
                            break;
                        default:
                            LogHelper.d(TAG, "onClick with state ", state.getState());
                    }
                }
            }
        });

        mRepeat.setImageDrawable(mNoRepeatDrawable);
        mRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomController.getInstance().toggleRepeatState(getContext());
            }
        });
        mShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomController.getInstance().toggleShuffleState(getContext());
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mStart.setText(DateUtils.formatElapsedTime(progress / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopSeekbarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaControllerCompat controller = getController();
                if (controller == null) {
                    return;
                }
                controller.getTransportControls().seekTo(seekBar.getProgress());
                scheduleSeekbarUpdate();
            }
        });

        // Only update from the intent if we are not recreating from a config change:
        if (savedInstanceState == null) {
            updateFromParams(getActivity().getIntent());
        }
        return rootView;
    }

    public void onConnected() {
        MediaControllerCompat controller = getController();
        if (controller == null) {
            return;
        }
        PlaybackStateCompat state = controller.getPlaybackState();
        updatePlaybackState(state);
        MediaMetadataCompat metadata = controller.getMetadata();
        if (metadata != null) {
            updateMediaDescription(metadata.getDescription());
            updateDuration(metadata);
        }
        updateProgress();
        if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
                state.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
            scheduleSeekbarUpdate();
        }
    }

    private void updateFromParams(Intent intent) {
        if (intent != null) {
            MediaDescriptionCompat description = intent.getParcelableExtra(
                    MusicPlayerListActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION);
            if (description != null) {
                updateMediaDescription(description);
            }
        }
    }

    private void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    @Override
    public void onStart() {
        LogHelper.i(TAG, "onStart");
        super.onStart();
        MediaControllerObserver.getInstance().addListener(this);
        onConnected();
        CustomController.getInstance().addRepeatStateListenerSet(this);
        CustomController.getInstance().addShuffleStateListenerSet(this);
        onRepeatStateChanged(CustomController.getInstance().getRepeatState());
        onShuffleStateChanged(CustomController.getInstance().getShuffleState());
    }

    @Override
    public void onStop() {
        LogHelper.i(TAG, "onStop");
        super.onStop();
        MediaControllerObserver.getInstance().removeListener(this);
        CustomController.getInstance().removeRepeatStateListenerSet(this);
        CustomController.getInstance().removeShuffleStateListenerSet(this);
    }

    @Override
    public void onDestroy() {
        LogHelper.i(TAG, "onDestroy");
        super.onDestroy();
        stopSeekbarUpdate();
        mExecutorService.shutdown();
    }

    @Override
    public void onRepeatStateChanged(RepeatState state) {
        if (state == null) {
            return;
        }
        switch (state) {
            case ONE:
                mRepeat.setImageDrawable(mRepeatOneDrawable);
                break;
            case ALL:
                mRepeatAllDrawable.setAlpha(255);
                mRepeat.setImageDrawable(mRepeatAllDrawable);
                break;
            case NONE:
                mRepeatAllDrawable.setAlpha(50);
                mRepeat.setImageDrawable(mRepeatAllDrawable);
                break;
            default:
                LogHelper.d(TAG, "Unhandled state ", state);
        }
    }

    @Override
    public void onShuffleStateChanged(ShuffleState state) {
        if (state == null) {
            return;
        }
        switch (state) {
            case SHUFFLE:
                mShuffleDrawable.setAlpha(255);
                break;
            case NONE:
                mShuffleDrawable.setAlpha(50);
                break;
            default:
                LogHelper.d(TAG, "Unhandled state ", state);
        }
    }


    private void fetchImageAsync(@NonNull MediaDescriptionCompat description) {
        if (description.getIconUri() == null) {
            return;
        }
        mCurrentArtUrl = description.getIconUri().toString();
        ViewHelper.updateAlbumArt(getActivity(), mBackgroundImage, mCurrentArtUrl, description.getTitle().toString(), 800, 800);
    }

    private void updateMediaDescription(MediaDescriptionCompat description) {
        if (description == null) {
            return;
        }
        LogHelper.d(TAG, "updateMediaDescription called ");
        mLine1.setText(description.getTitle());
        mLine2.setText(description.getSubtitle());
        fetchImageAsync(description);
    }

    private void updateDuration(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        LogHelper.d(TAG, "updateDuration called ");
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        mSeekbar.setMax(duration);
        mEnd.setText(DateUtils.formatElapsedTime(duration / 1000));
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state == null) {
            return;
        }
        mLastPlaybackState = state;
        MediaControllerCompat controller = getController();
        if (controller != null && controller.getExtras() != null) {
            String castName = controller.getExtras().getString(CastPlayer.EXTRA_CONNECTED_CAST);
            String line3Text = castName == null ? "" : getResources()
                    .getString(R.string.casting_to_device, castName);
            mLine3.setText(line3Text);
        }

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                mLoading.setVisibility(INVISIBLE);
                mPlayPause.setVisibility(VISIBLE);
                mPlayPause.setImageDrawable(mPauseDrawable);
                mControllers.setVisibility(VISIBLE);
                scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                mControllers.setVisibility(VISIBLE);
                mLoading.setVisibility(INVISIBLE);
                mPlayPause.setVisibility(VISIBLE);
                mPlayPause.setImageDrawable(mPlayDrawable);
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                mLoading.setVisibility(INVISIBLE);
                mPlayPause.setVisibility(VISIBLE);
                mPlayPause.setImageDrawable(mPlayDrawable);
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                mPlayPause.setVisibility(INVISIBLE);
                mLoading.setVisibility(VISIBLE);
                mLine3.setText(R.string.loading);
                stopSeekbarUpdate();
                break;
            default:
                LogHelper.d(TAG, "Unhandled state ", state.getState());
        }

        mSkipNext.setVisibility((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) == 0
                ? INVISIBLE : VISIBLE);
        mSkipPrev.setVisibility((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) == 0
                ? INVISIBLE : VISIBLE);
    }

    private void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() != PlaybackStateCompat.STATE_PAUSED) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }
        mSeekbar.setProgress((int) currentPosition);
    }

    protected MediaControllerCompat getController() {
        if (getActivity() == null) {
            return null;
        }
        return getActivity().getSupportMediaController();
    }
}
