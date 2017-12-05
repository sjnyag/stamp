package com.sjn.stamp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.NotificationHelper.*
import com.sjn.stamp.utils.ShareHelper

class IntentDispatchActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogHelper.d(TAG, "onCreate")
        val newIntent = Intent(this, MusicPlayerListActivity::class.java)
        if (intent != null) {
            newIntent.action = intent.action
            newIntent.data = intent.data
            newIntent.putExtras(intent.extras)
        }
        if (ACTION_CMD == newIntent.action && CMD_SHARE == newIntent.getStringExtra(CMD_NAME)) {
            ShareHelper.share(this, newIntent.extras.getString(SHARE_MESSAGE), newIntent.extras.getStringArrayList(HASH_TAG_LIST))
        } else {
            startActivity(newIntent)
        }
        finish()
    }

    companion object {
        private val TAG = LogHelper.makeLogTag(IntentDispatchActivity::class.java)
    }
}
