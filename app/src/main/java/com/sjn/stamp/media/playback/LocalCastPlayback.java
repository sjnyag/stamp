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
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.MediaItemHelper;
import com.sjn.stamp.utils.TimeHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import fi.iki.elonen.NanoHTTPD;

import static android.content.Context.WIFI_SERVICE;
import static fi.iki.elonen.NanoHTTPD.Response.Status.NOT_FOUND;
import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

/**
 * An implementation of Playback that talks to Cast.
 */
public class LocalCastPlayback extends CastPlayback {

    private static final String TAG = LogHelper.makeLogTag(LocalCastPlayback.class);

    private HttpServer mHttpServer;

    private class HttpServer extends NanoHTTPD {
        final int mPort;
        MediaSessionCompat.QueueItem mMedia;

        HttpServer(int port) throws IOException {
            super(port);
            mPort = port;
        }

        void setMedia(MediaSessionCompat.QueueItem media) {
            mMedia = media;
        }

        @Override
        public Response serve(IHTTPSession session) {
            if (mMedia == null) {
                return new Response(NOT_FOUND, MIME_PLAINTEXT, "No music");
            }
            if (session.getUri().contains("image")) {
                return serveImage();
            } else if (session.getUri().contains("debug")) {
                return new Response(NOT_FOUND, MIME_PLAINTEXT, mMedia.getDescription().getMediaUri().toString());
            } else {
                return serveMusic();
            }
        }

        private Response serveMusic() {
            InputStream stream = null;
            try {
                stream = new FileInputStream(
                        mMedia.getDescription().getMediaUri().toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return new Response(OK, "audio/mp3", stream);
        }

        private Response serveImage() {
            InputStream stream = null;
            try {
                stream = mAppContext.getContentResolver().openInputStream(Uri.parse(mMedia.getDescription().getIconUri().toString()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return new Response(OK, "image/jpeg", stream);
        }
    }

    private void startSever() {
        try {
            String ip = getWifiAddress();
            int port = findOpenPort(ip, 8080);
            mHttpServer = new HttpServer(port);
            mHttpServer.start();
            LogHelper.e(TAG, "http://" + getWifiAddress() + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getWifiAddress() {
        WifiManager wifiManager = (WifiManager) mAppContext.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return ((ipAddress) & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                ((ipAddress >> 24) & 0xFF);
    }

    private int findOpenPort(String ip, int startPort) {
        final int timeout = 200;
        for (int port = startPort; port <= 65535; port++) {
            if (isPortAvailable(ip, port, timeout)) {
                return port;
            }
        }
        throw new RuntimeException("There is no open port.");
    }

    private boolean isPortAvailable(String ip, int port, int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public LocalCastPlayback(Context context) {
        super(context);
    }

    @Override
    public void stop(boolean notifyListeners) {
        super.stop(notifyListeners);
        if (mHttpServer != null && mHttpServer.isAlive()) {
            mHttpServer.stop();
        }
    }

    protected void loadMedia(MediaSessionCompat.QueueItem item, boolean autoPlay) throws JSONException {
        String mediaId = item.getDescription().getMediaId();
        if (mediaId == null || mediaId.isEmpty()) {
            throw new IllegalArgumentException("Invalid mediaId");
        }
        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
        if (musicId == null || musicId.isEmpty()) {
            throw new IllegalArgumentException("Invalid mediaId");
        }
        if (!TextUtils.equals(mediaId, mCurrentMediaId) || mState != PlaybackStateCompat.STATE_PAUSED) {
            mCurrentMediaId = mediaId;
            mCurrentPosition = 0;
        }
        JSONObject customData = new JSONObject();
        customData.put(ITEM_ID, mediaId);
        if (mHttpServer == null || !mHttpServer.isAlive()) {
            startSever();
        }
        String url = "http://" + getWifiAddress() + ":" + mHttpServer.mPort;
        mHttpServer.setMedia(item);
        MediaInfo media = MediaItemHelper.convertToMediaInfo(item, url);
        mRemoteMediaClient.load(media, autoPlay, mCurrentPosition, customData);
    }
}
