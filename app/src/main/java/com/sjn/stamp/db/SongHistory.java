package com.sjn.stamp.db;

import android.content.res.Resources;

import com.sjn.stamp.R;
import com.sjn.stamp.constant.RecordType;
import com.sjn.stamp.utils.TimeHelper;

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
    @Index
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

    private void setRecordType(RecordType recordType) {
        this.mRecordType = recordType.getValue();
    }

    public String toLabel(Resources resources) {
        return resources.getString(R.string.song_history_label, mSong.getTitle(), mSong.getArtist().getName(), TimeHelper.formatYYYYMMDDHHMMSS(mRecordedAt));
    }
}