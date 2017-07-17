package com.sjn.stamp.media.provider;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.utils.LogHelper;

import java.io.IOException;
import java.util.HashMap;

class ProviderCache {

    private static final String TAG = LogHelper.makeLogTag(ProviderCache.class);
    private static final String SUB_DIR = "/media_provider";
    private static final String CACHE_KEY = "media_map_cache";

    static HashMap<String, MediaMetadataCompat> readCache(Context context) {
        MediaMapCache mediaMapCache = null;
        try {
            ParcelDiskCache cache = ParcelDiskCache.open(context, MediaMapCache.class.getClassLoader(), SUB_DIR, 1024 * 1024 * 10);
            mediaMapCache = (MediaMapCache) cache.get(CACHE_KEY);
            cache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mediaMapCache == null) {
            return new HashMap<>();
        }
        return mediaMapCache.mMap;
    }

    private static class MediaMapCache implements Parcelable {
        final HashMap<String, MediaMetadataCompat> mMap;

        MediaMapCache(HashMap<String, MediaMetadataCompat> map) {
            mMap = map;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mMap.size());
            for (HashMap.Entry<String, MediaMetadataCompat> entry : mMap.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeParcelable(entry.getValue(), PARCELABLE_WRITE_RETURN_VALUE);
            }

        }

        public static final Parcelable.Creator<MediaMapCache> CREATOR =
                new Parcelable.Creator<MediaMapCache>() {
                    @Override
                    public MediaMapCache createFromParcel(Parcel in) {
                        HashMap<String, MediaMetadataCompat> map = new HashMap<>();
                        int size = in.readInt();
                        for (int i = 0; i < size; i++) {
                            String key = in.readString();
                            MediaMetadataCompat value = in.readParcelable(MediaMetadataCompat.class.getClassLoader());
                            map.put(key, value);
                        }
                        return new MediaMapCache(map);
                    }

                    @Override
                    public MediaMapCache[] newArray(int size) {
                        return new MediaMapCache[size];
                    }
                };

    }


    static void saveCache(Context context, HashMap<String, MediaMetadataCompat> map) {
        MediaMapCache mediaMapCache = new MediaMapCache(map);
        try {
            ParcelDiskCache cache = ParcelDiskCache.open(context, MediaMapCache.class.getClassLoader(), SUB_DIR, 1024 * 1024 * 10);
            cache.set(CACHE_KEY, mediaMapCache);
            cache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
