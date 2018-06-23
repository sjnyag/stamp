package com.sjn.stamp

import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.sjn.stamp.utils.ColorfulHelper
import com.sjn.stamp.utils.DrawerHelper
import com.sjn.stamp.utils.RealmHelper
import com.squareup.leakcanary.LeakCanary
import net.danlew.android.joda.JodaTimeAndroid

@Suppress("unused")
class StampApplication : MultiDexApplication() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
        ColorfulHelper.init(this)
        RealmHelper.init(this)
        JodaTimeAndroid.init(this)
    }
}