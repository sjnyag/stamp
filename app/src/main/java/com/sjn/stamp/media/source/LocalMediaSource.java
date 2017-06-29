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

package com.sjn.stamp.media.source;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.utils.MediaRetrieveHelper;
import com.sjn.stamp.utils.LogHelper;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to get a list of MusicTrack's from Content Provider.
 */
public class LocalMediaSource implements MusicProviderSource {

    public interface PermissionRequiredCallback {
        void onPermissionRequired();
    }

    private static final String TAG = LogHelper.makeLogTag(LocalMediaSource.class);

    private final Context mContext;
    private final PermissionRequiredCallback mCallback;

    public LocalMediaSource(Context context, PermissionRequiredCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        if (!MediaRetrieveHelper.hasPermission(mContext)) {
            mCallback.onPermissionRequired();
            return Collections.<MediaMetadataCompat>emptyList().iterator();
        }
        MediaRetrieveHelper.initCache(mContext);
        List<MediaRetrieveHelper.MediaCursorContainer> list = MediaRetrieveHelper.readCache();
        LogHelper.i(TAG, "Read " + list.size() + "songs from cache");
        if (list.size() == 0) {
            list = MediaRetrieveHelper.retrieveAllMedia(mContext);
        }
        MediaRetrieveHelper.retrieveAndUpdateCache(mContext);
        return MediaRetrieveHelper.createIterator(list);
    }


}
