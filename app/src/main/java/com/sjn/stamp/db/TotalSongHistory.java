package com.sjn.stamp.db;

import com.sjn.stamp.constant.RecordType;

import io.realm.RealmObject;
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
public class TotalSongHistory extends RealmObject {
    @PrimaryKey
    public long mId;
    private Song mSong;

    public int mPlayCount;
    public int mSkipCount;
    public int mCompleteCount;

    public void setPlayCountIfOver(int playCount) {
        if (playCount <= mPlayCount) {
            return;
        }
        mPlayCount = playCount;
    }

    public void setSkipCountIfOver(int skipCount) {
        if (skipCount <= mSkipCount) {
            return;
        }
        mSkipCount = skipCount;
    }

    public void setCompleteCountIfOver(int completeCount) {
        if (completeCount <= mCompleteCount) {
            return;
        }
        mCompleteCount = completeCount;
    }

    public void incrementPlayCount(int playCount) {
        mPlayCount += playCount;
    }

    public void incrementSkipCount(int skipCount) {
        mSkipCount += skipCount;
    }

    public void incrementCompleteCount(int completeCount) {
        mCompleteCount += completeCount;
    }

    public void parseSongQueue(Song song, RecordType recordType) {
        switch (recordType) {
            case PLAY:
                setValues(song, 1, 0, 0);
                break;
            case SKIP:
                setValues(song, 0, 1, 0);
                break;
            case COMPLETE:
                setValues(song, 0, 0, 1);
                break;
            default:
                setValues(song, 0, 0, 0);
                break;
        }
    }

    public void setValues(Song song, int playCount, int skipCount, int completeCount) {
        setSong(song);
        setPlayCount(playCount);
        setSkipCount(skipCount);
        setCompleteCount(completeCount);
    }
}