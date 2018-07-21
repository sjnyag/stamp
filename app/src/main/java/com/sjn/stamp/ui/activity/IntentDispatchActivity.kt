package com.sjn.stamp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.sjn.stamp.MusicService
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.NotificationHelper
import com.sjn.stamp.utils.ShareHelper
import com.sjn.stamp.utils.startForegroundServiceCompatible

class IntentDispatchActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogHelper.d(TAG, "onCreate")
        if (intent.getStringExtra("Shortcut") == "Play") {
            startForegroundServiceCompatible(Intent(this, MusicService::class.java).apply {
                action = NotificationHelper.ACTION_CMD
                putExtra(NotificationHelper.CMD_NAME, MusicService.NOTIFICATION_CMD_PLAY)
            })
            finish()
            return
        }
        val newIntent = Intent(this, MusicPlayerListActivity::class.java).apply {
            intent?.let { currentIntent ->
                this.action = currentIntent.action
                this.data = currentIntent.data
                currentIntent.extras?.let { extras ->
                    this.putExtras(extras)
                }
            }
        }
        if (NotificationHelper.ACTION_CMD == newIntent.action && NotificationHelper.CMD_SHARE == newIntent.getStringExtra(NotificationHelper.CMD_NAME)) {
            ShareHelper.share(this, newIntent.extras.getString(NotificationHelper.SHARE_MESSAGE), newIntent.extras.getStringArrayList(NotificationHelper.HASH_TAG_LIST))
        } else {
            startActivity(newIntent)
        }
        finish()
    }

    companion object {
        private val TAG = LogHelper.makeLogTag(IntentDispatchActivity::class.java)
    }
}
