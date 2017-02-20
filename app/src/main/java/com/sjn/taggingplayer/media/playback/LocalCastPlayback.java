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
package com.sjn.taggingplayer.media.playback;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.sjn.taggingplayer.media.provider.MusicProvider;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.MediaIDHelper;
import com.sjn.taggingplayer.utils.TimeHelper;

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
import static com.sjn.taggingplayer.media.source.MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE;
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
        MediaMetadataCompat mMedia;

        HttpServer(int port) throws IOException {
            super(port);
            mPort = port;
        }

        void setMedia(MediaMetadataCompat media) {
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
                return new Response(NOT_FOUND, MIME_PLAINTEXT, mMedia.getString(CUSTOM_METADATA_TRACK_SOURCE));
            } else {
                return serveMusic();
            }
        }

        private Response serveMusic() {
            InputStream stream = null;
            try {
                stream = new FileInputStream(
                        mMedia.getString(CUSTOM_METADATA_TRACK_SOURCE));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return new Response(OK, "audio/mp3", stream);
        }

        private Response serveImage() {
            InputStream stream = null;
            try {
                stream = mAppContext.getContentResolver().openInputStream(Uri.parse(
                        mMedia.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)));
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

    public LocalCastPlayback(MusicProvider musicProvider, Context context) {
        super(musicProvider, context);
    }

    @Override
    public void stop(boolean notifyListeners) {
        super.stop(notifyListeners);
        if (mHttpServer != null && mHttpServer.isAlive()) {
            mHttpServer.stop();
        }
    }

    protected void loadMedia(String mediaId, boolean autoPlay) throws JSONException {
        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
        MediaMetadataCompat track = mMusicProvider.getMusicByMusicId(musicId);
        if (track == null) {
            throw new IllegalArgumentException("Invalid mediaId " + mediaId);
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
        mHttpServer.setMedia(track);
        MediaInfo media = toCastMediaMetadata(url, track);
        mRemoteMediaClient.load(media, autoPlay, mCurrentPosition, customData);
    }

    private MediaInfo toCastMediaMetadata(String url, MediaMetadataCompat track) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE,
                track.getDescription().getTitle() == null ? "" :
                        track.getDescription().getTitle().toString());
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE,
                track.getDescription().getSubtitle() == null ? "" :
                        track.getDescription().getSubtitle().toString());
        mediaMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST,
                track.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        mediaMetadata.putString(MediaMetadata.KEY_ARTIST,
                track.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        mediaMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE,
                track.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
        WebImage image = new WebImage(
                new Uri.Builder().encodedPath(url + "/image/" + TimeHelper.getJapanNow().toString()).build());
        // First image is used by the receiver for showing the audio album art.
        mediaMetadata.addImage(image);
        // Second image is used by Cast Companion Library on the full screen activity that is shown
        // when the cast dialog is clicked.
        mediaMetadata.addImage(image);

        return new MediaInfo.Builder(url)
                .setContentType("audio/mpeg")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
    }

}
