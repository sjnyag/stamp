package com.sjn.taggingplayer.db;

import java.util.Date;

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
public class DailySongHistory extends RealmObject {
    @PrimaryKey
    public long mId;
    private Song mSong;

    public Date mDate;
    public int mPlayCount;
    public int mSkipCount;

    public void incrementPlayCount(int playCount) {
        mPlayCount += playCount;
    }

    public void incrementSkipCount(int skipCount) {
        mSkipCount += skipCount;
    }

    public void setValues(Song song, int playCount, int skipCount) {
        setSong(song);
        setDate(new Date());
        setPlayCount(playCount);
        setSkipCount(skipCount);
    }

    public void parseSongQueue(SongHistory songHistory) {
        switch (songHistory.getRecordType()) {
            case PLAY:
                setValues(songHistory.getSong(), 1, 0);
                break;
            case SKIP:
                setValues(songHistory.getSong(), 0, 1);
                break;
            default:
                setValues(songHistory.getSong(), 0, 0);
                break;
        }
    }
}