package com.sjn.taggingplayer.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sjn.taggingplayer.TaggingPlayerApplication;

abstract public class BaseActivity extends AppCompatActivity {

    protected TaggingPlayerApplication mTaddolApplication;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaddolApplication = (TaggingPlayerApplication) this.getApplicationContext();
    }

    protected void onResume() {
        super.onResume();
        mTaddolApplication.setCurrentActivity(this);
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
        Activity currActivity = mTaddolApplication.getCurrentActivity();
        if (this.equals(currActivity)) {
            mTaddolApplication.setCurrentActivity(null);
        }
    }
}