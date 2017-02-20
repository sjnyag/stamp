package com.sjn.taggingplayer.db;

import com.sjn.taggingplayer.constant.RepeatState;
import com.sjn.taggingplayer.constant.ShuffleState;

import io.realm.RealmObject;
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
public class UserSetting extends RealmObject {

    public boolean mIsAutoLogin;
    public int mRepeatState;
    public int mShuffleState;
    private String mQueueIdentifyMediaId;
    private String mLastMusicId;

    public void setRepeatState(RepeatState repeatState) {
        this.mRepeatState = repeatState.getNo();
    }

    public RepeatState getRepeatState() {
        return RepeatState.of(mRepeatState);
    }

    public void setShuffleState(ShuffleState shuffleState) {
        this.mShuffleState = shuffleState.getNo();
    }

    public ShuffleState getShuffleState() {
        return ShuffleState.of(mShuffleState);
    }

}
