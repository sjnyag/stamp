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
package com.sjn.stamp.ui.activity;

import android.app.UiModeManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sjn.stamp.ui.tv.TvPlaybackActivity;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.TweetHelper;

import static com.sjn.stamp.utils.NotificationHelper.ACTION_CMD;
import static com.sjn.stamp.utils.NotificationHelper.CMD_NAME;
import static com.sjn.stamp.utils.NotificationHelper.CMD_SHARE;
import static com.sjn.stamp.utils.NotificationHelper.SHARE_MESSAGE;

/**
 * The activity for the Now Playing Card PendingIntent.
 * https://developer.android.com/training/tv/playback/now-playing.html
 * <p>
 * This activity determines which activity to launch based on the current UI mode.
 */
public class NowPlayingActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.makeLogTag(NowPlayingActivity.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "onCreate");
        Intent newIntent;
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            LogHelper.d(TAG, "Running on a TV Device");
            newIntent = new Intent(this, TvPlaybackActivity.class);
        } else {
            LogHelper.d(TAG, "Running on a non-TV Device");
            newIntent = new Intent(this, MusicPlayerListActivity.class);
        }
        if (getIntent() != null) {
            newIntent.setAction(getIntent().getAction());
            newIntent.setData(getIntent().getData());
            newIntent.putExtras(getIntent().getExtras());
        }
        String action = newIntent.getAction();
        String command = newIntent.getStringExtra(CMD_NAME);
        if (ACTION_CMD.equals(action) && CMD_SHARE.equals(command)) {
            TweetHelper.tweet(this, newIntent.getExtras().getString(SHARE_MESSAGE));
        } else {
            startActivity(newIntent);
            finish();
        }
    }
}
