package com.sjn.taggingplayer.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sjn.taggingplayer.TaggingPlayerApplication;

abstract public class BaseActivity extends AppCompatActivity {

    protected TaggingPlayerApplication mApplication;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (TaggingPlayerApplication) this.getApplicationContext();
    }

    protected void onResume() {
        super.onResume();
        mApplication.setCurrentActivity(this);
    }

    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences() {
        Activity currentActivity = mApplication.getCurrentActivity();
        if (this.equals(currentActivity)) {
            mApplication.setCurrentActivity(null);
        }
    }
}