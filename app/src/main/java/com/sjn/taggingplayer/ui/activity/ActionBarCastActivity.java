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
package com.sjn.taggingplayer.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.MediaRouteButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.utils.LogHelper;

/**
 * Abstract activity with toolbar, navigation drawer and cast support. Needs to be extended by
 * any activity that wants to be shown as a top level activity.
 * <p>
 * The requirements for a subclass is to call {@link #initializeToolbar()} on onCreate, after
 * setContentView() is called and have three mandatory layout elements:
 * a {@link android.support.v7.widget.Toolbar} with id 'toolbar',
 * a {@link android.support.v4.widget.DrawerLayout} with id 'drawerLayout' and
 * a {@link android.widget.ListView} with id 'drawerList'.
 */
public abstract class ActionBarCastActivity extends DrawerActivity {

    private static final String TAG = LogHelper.makeLogTag(ActionBarCastActivity.class);

    private static final int DELAY_MILLIS = 1000;

    private CastContext mCastContext;
    private MenuItem mMediaRouteMenuItem;

    private CastStateListener mCastStateListener = new CastStateListener() {
        @Override
        public void onCastStateChanged(int newState) {
            if (newState != CastState.NO_DEVICES_AVAILABLE) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mMediaRouteMenuItem != null && mMediaRouteMenuItem.isVisible()) {
                            LogHelper.d(TAG, "Cast Icon is visible");
                            showFtu();
                        }
                    }
                }, DELAY_MILLIS);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "Activity onCreate");
        try {
            mCastContext = CastContext.getSharedInstance(this);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCastContext != null) {
            mCastContext.addCastStateListener(mCastStateListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCastContext != null) {
            mCastContext.removeCastStateListener(mCastStateListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        try {
            mMediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                    menu, R.id.media_route_menu_item);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Shows the Cast First Time User experience to the user (an overlay that explains what is
     * the Cast icon)
     */
    private void showFtu() {
        Menu menu = mToolbar.getMenu();
        View view = menu.findItem(R.id.media_route_menu_item).getActionView();
        if (mMediaRouteMenuItem != null && view != null && view instanceof MediaRouteButton) {
            IntroductoryOverlay overlay = new IntroductoryOverlay.Builder(this, mMediaRouteMenuItem)
                    .setTitleText(R.string.touch_to_cast)
                    .setSingleTime()
                    .build();
            overlay.show();
        }
    }
}
