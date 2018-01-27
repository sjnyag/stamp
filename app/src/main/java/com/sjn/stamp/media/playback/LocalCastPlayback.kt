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
package com.sjn.stamp.media.playback

import android.content.Context
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import com.sjn.stamp.utils.LocalCastHelper
import com.sjn.stamp.utils.TimeHelper

/**
 * An implementation of Playback that talks to Cast.
 */
class LocalCastPlayback(context: Context, callback: Playback.Callback, initialStreamPosition: Int, currentMediaId: String?) : CastPlayback(context, callback, initialStreamPosition, currentMediaId) {

    private var httpServer: LocalCastHelper.HttpServer? = null

    override fun playItem(item: MediaSessionCompat.QueueItem) {
        if (httpServer?.isAlive != true) {
            httpServer = LocalCastHelper.startSever(context)

        }
        httpServer?.let {
            it.media = item
            send(item, true, it.url, Uri.Builder().encodedPath("${it.url}/image/${TimeHelper.japanNow}").build())
        }
    }

    override fun stop(notifyListeners: Boolean) {
        super.stop(notifyListeners)
        httpServer?.let { if (it.isAlive) it.stop() }
    }

}
