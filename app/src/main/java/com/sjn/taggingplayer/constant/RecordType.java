package com.sjn.taggingplayer.constant;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.sjn.taggingplayer.R;

@SuppressWarnings({"unused"})
public enum RecordType {
    PLAY("play", R.drawable.record_type_play),
    SKIP("skip", R.drawable.record_type_skip),
    START("start", R.drawable.record_type_start),
    COMPLETE("complete", R.drawable.record_type_complete);

    final public String mValue;
    int mDrawableId;


    public String getValue() {
        return mValue;
    }

    RecordType(String value, int drawableId) {
        mValue = value;
        mDrawableId = drawableId;
    }

    public static RecordType of(String value) {
        for (RecordType recordType : RecordType.values()) {
            if (recordType.getValue().equals(value)) return recordType;
        }
        return null;
    }

    public String getText() {
        return this.toString();
    }

    public Drawable getDrawable(Context context) {
        if (mDrawableId == -1) {
            return null;
        }
        return ContextCompat.getDrawable(context, mDrawableId);
    }
}