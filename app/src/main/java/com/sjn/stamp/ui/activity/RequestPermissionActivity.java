package com.sjn.stamp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaRetrieveHelper;
import com.sjn.stamp.utils.PermissionHelper;

import java.util.Arrays;

public class RequestPermissionActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.makeLogTag(RequestPermissionActivity.class);

    private final int REQUEST_PERMISSION = 1;
    public static final String KEY_PERMISSIONS = "KEY_PERMISSIONS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogHelper.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String[] permissionList = intent.getStringArrayExtra(KEY_PERMISSIONS);
        boolean requested = PermissionHelper.requestPermissions(this, permissionList,
                REQUEST_PERMISSION);
        if (!requested) {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (Arrays.asList(permissions).contains(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            MediaRetrieveHelper.retrieveAndUpdateCache(this, new MediaRetrieveHelper.PermissionRequiredCallback() {
                @Override
                public void onPermissionRequired() {

                }
            });
        }
        if (requestCode == REQUEST_PERMISSION) {
            finish();
        }
    }

}