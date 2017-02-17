package com.sjn.taggingplayer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sjn.taggingplayer.utils.PermissionHelper;

public class RequestPermissionActivity extends AppCompatActivity {

    private final int REQUEST_PERMISSION = 1;
    public static final String KEY_PERMISSIONS = "KEY_PERMISSIONS";

    //TODO: grouping requesting permission
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String[] permissionList = intent.getStringArrayExtra(KEY_PERMISSIONS);
        if (PermissionHelper.shouldShowRequestPermissionRationale(this, permissionList)) {
            PermissionHelper.requestPermissions(this, permissionList,
                    REQUEST_PERMISSION);
        } else {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            finish();
        }
    }

}