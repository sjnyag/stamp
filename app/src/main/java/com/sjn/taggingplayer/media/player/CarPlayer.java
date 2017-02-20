package com.sjn.taggingplayer.media.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.media.MediaBrowserServiceCompat;

import com.sjn.taggingplayer.utils.CarHelper;
import com.sjn.taggingplayer.utils.LogHelper;


public class CarPlayer {
    private static final String TAG = LogHelper.makeLogTag(CarPlayer.class);

    private MediaBrowserServiceCompat mService;
    private boolean mIsConnectedToCar;
    private BroadcastReceiver mCarConnectionReceiver;

    public CarPlayer(MediaBrowserServiceCompat service) {
        mService = service;
    }

    public void registerCarConnectionReceiver() {
        IntentFilter filter = new IntentFilter(CarHelper.ACTION_MEDIA_STATUS);
        mCarConnectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String connectionEvent = intent.getStringExtra(CarHelper.MEDIA_CONNECTION_STATUS);
                mIsConnectedToCar = CarHelper.MEDIA_CONNECTED.equals(connectionEvent);
                LogHelper.i(TAG, "Connection event to Android Auto: ", connectionEvent,
                        " isConnectedToCar=", mIsConnectedToCar);
            }
        };
        mService.registerReceiver(mCarConnectionReceiver, filter);
    }

    public void unregisterCarConnectionReceiver() {
        mService.unregisterReceiver(mCarConnectionReceiver);
    }
}
