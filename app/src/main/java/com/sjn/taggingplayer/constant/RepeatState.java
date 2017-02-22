package com.sjn.taggingplayer.constant;

public enum RepeatState implements QueueState {
    ALL(0),
    ONE(1),
    NONE(2);
    final int mNo;

    RepeatState(int no) {
        mNo = no;
    }

    public int getNo() {
        return mNo;
    }

    public RepeatState toggle() {
        int no = (mNo + 1) % RepeatState.values().length;
        return RepeatState.of(no);
    }

    public static RepeatState of(int no) {
        for (RepeatState state : RepeatState.values()) {
            if (state.mNo == no) return state;
        }
        return null;
    }

    public static RepeatState getDefault() {
        return NONE;
    }
}
