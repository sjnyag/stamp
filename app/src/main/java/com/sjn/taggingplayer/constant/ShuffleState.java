package com.sjn.taggingplayer.constant;

public enum ShuffleState implements QueueState {
    SHUFFLE(0),
    NONE(1);
    final int mNo;

    ShuffleState(int no) {
        mNo = no;
    }

    public int getNo() {
        return mNo;
    }

    public ShuffleState toggle() {
        int no = (mNo + 1) % ShuffleState.values().length;
        return ShuffleState.of(no);
    }

    public static ShuffleState of(int no) {
        for (ShuffleState state : ShuffleState.values()) {
            if (state.mNo == no) return state;
        }
        return null;
    }

    public static ShuffleState getDefault() {
        return NONE;
    }
}