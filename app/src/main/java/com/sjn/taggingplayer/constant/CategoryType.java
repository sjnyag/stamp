package com.sjn.taggingplayer.constant;

import android.support.v4.media.MediaMetadataCompat;

public enum CategoryType {
    ALBUM("album", MediaMetadataCompat.METADATA_KEY_ALBUM),
    ARTIST("artist", MediaMetadataCompat.METADATA_KEY_ARTIST),
    GENRE("genre", MediaMetadataCompat.METADATA_KEY_GENRE),;

    final public String mValue;
    final public String mKey;


    public String getValue() {
        return mValue;
    }

    public String getKey() {
        return mKey;
    }

    CategoryType(String value, String key) {
        mValue = value;
        mKey = key;
    }

    public static CategoryType of(String value) {
        for (CategoryType recordType : CategoryType.values()) {
            if (recordType.getValue().equals(value)) return recordType;
        }
        return null;
    }
}