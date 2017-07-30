package com.sjn.stamp.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sjn.stamp.StampApplication;

abstract public class BaseActivity extends AppCompatActivity {

    protected StampApplication mApplication;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (StampApplication) this.getApplicationContext();
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