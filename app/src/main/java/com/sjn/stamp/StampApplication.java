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
package com.sjn.stamp;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.widget.ImageView;

import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.sjn.stamp.utils.RealmHelper;
import com.squareup.picasso.Picasso;

import net.danlew.android.joda.JodaTimeAndroid;

public class StampApplication extends MultiDexApplication {
    private Activity mCurrentActivity = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Firebase.setAndroidContext(this);
        RealmHelper.init(this);
        JodaTimeAndroid.init(this);

        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.mCurrentActivity = currentActivity;
    }
}
