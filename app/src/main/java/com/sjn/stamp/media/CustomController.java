package com.sjn.stamp.media;

import android.content.Context;

import com.sjn.stamp.constant.ShuffleState;
import com.sjn.stamp.controller.UserSettingController;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.constant.RepeatState;

import java.util.ArrayList;
import java.util.List;

public class CustomController {
    private static final String TAG = LogHelper.makeLogTag(CustomController.class);

    private static CustomController sInstance;

    private List<ShuffleStateListener> mShuffleStateListenerSet = new ArrayList<>();

    private List<RepeatStateListener> mRepeatStateListenerSet = new ArrayList<>();

    public ShuffleState getShuffleState() {
        return mShuffleState;
    }

    public RepeatState getRepeatState() {
        return mRepeatState;
    }

    private ShuffleState mShuffleState;

    private RepeatState mRepeatState;

    public interface RepeatStateListener {
        void onRepeatStateChanged(RepeatState state);
    }

    public interface ShuffleStateListener {
        void onShuffleStateChanged(ShuffleState state);
    }

    public void addShuffleStateListenerSet(ShuffleStateListener listener) {
        mShuffleStateListenerSet.add(listener);
    }

    public void removeShuffleStateListenerSet(ShuffleStateListener listener) {
        mShuffleStateListenerSet.add(listener);
    }

    public void addRepeatStateListenerSet(RepeatStateListener listener) {
        mRepeatStateListenerSet.add(listener);
    }

    public void removeRepeatStateListenerSet(RepeatStateListener listener) {
        mRepeatStateListenerSet.add(listener);
    }

    public void setRepeatState(Context context, RepeatState state) {
        mRepeatState = state;
        for (RepeatStateListener repeatStateListener : mRepeatStateListenerSet) {
            repeatStateListener.onRepeatStateChanged(state);
        }
        UserSettingController userSettingController = new UserSettingController();
        userSettingController.setRepeatState(state);
    }

    public void setShuffleState(Context context, ShuffleState state) {
        mShuffleState = state;
        for (ShuffleStateListener shuffleStateListener : mShuffleStateListenerSet) {
            shuffleStateListener.onShuffleStateChanged(state);
        }
        UserSettingController userSettingController = new UserSettingController();
        userSettingController.setShuffleState(state);
    }

    public void toggleRepeatState(Context context) {
        setRepeatState(context, mRepeatState.toggle());
    }

    public void toggleShuffleState(Context context) {
        setShuffleState(context, mShuffleState.toggle());
    }

    public static CustomController getInstance() {
        if (sInstance == null) {
            sInstance = new CustomController();
        }
        return sInstance;
    }

    private CustomController() {
        mShuffleState = ShuffleState.Companion.getDefault();
        mRepeatState = RepeatState.Companion.getDefault();
    }

}
