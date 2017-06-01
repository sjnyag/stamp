package com.sjn.taggingplayer.db;

import android.graphics.Color;

import com.sjn.taggingplayer.constant.RecordType;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
@AllArgsConstructor(suppressConstructorProperties = true)
@NoArgsConstructor
@Getter
@Setter
public class SongHistory extends RealmObject {

    @PrimaryKey
    public long mId;
    private Song mSong;
    public Date mRecordedAt;
    //TODO
    //@Index
    public String mRecordType;
    public Device mDevice;
    public int mCount;
    public float mLatitude;
    public float mLongitude;
    public float mAccuracy;
    public float mAltitude;

    public void setValues(Song song, RecordType recordType, Device device, Date date, int count) {
        setSong(song);
        setRecordedAt(date);
        setRecordType(recordType);
        setDevice(device);
        setCount(count);
    }

    public void setRecordType(RecordType recordType) {
        this.mRecordType = recordType.getValue();
    }

    public RecordType getRecordType() {
        return RecordType.of(mRecordType);
    }

    public int getColor() {
//        return Color.rgb(255, 255, 255 - getYellow());
        return Color.rgb(0, 0, 0);
    }

    private int getYellow() {
        if (getCount() * 2.55 > 255) {
            return 255;
        }
        return (int) (getCount() * 2.55);
    }
}