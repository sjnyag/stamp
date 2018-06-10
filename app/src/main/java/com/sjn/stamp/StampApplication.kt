package com.sjn.stamp

import android.content.Context
import android.graphics.Color
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.sjn.stamp.utils.DrawerHelper
import com.sjn.stamp.utils.RealmHelper
import com.squareup.leakcanary.LeakCanary
import io.multimoon.colorful.*
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
        LeakCanary.install(this);
        val defaults = Defaults(
                primaryColor = ThemeColor.DEEP_PURPLE,
                accentColor = ThemeColor.GREEN,
                useDarkTheme = true,
                translucent = true)
        initColorful(this, defaults)
        RealmHelper.init(this)
        JodaTimeAndroid.init(this)
        DrawerHelper.initialize()
    }
}

fun ColorfulDelegate.getTextColor(): Int {
    return if (Colorful().getDarkTheme()) Color.WHITE else Color.BLACK
}

fun ColorfulDelegate.getCurrent(primary: Boolean = true, dark: Boolean = false): ColorfulColor {
    return if (dark) {
        if (primary) getPrimaryDark() else getAccentDark()
    } else {
        if (primary) getPrimary() else getAccent()
    }
}

fun ColorfulDelegate.getPrimary(): ColorfulColor {
    return Colorful().getPrimaryColor().getColorPack().normal()
}

fun ColorfulDelegate.getAccent(): ColorfulColor {
    return Colorful().getAccentColor().getColorPack().normal()
}

fun ColorfulDelegate.getPrimaryDark(): ColorfulColor {
    return Colorful().getPrimaryColor().getColorPack().dark()
}

fun ColorfulDelegate.getAccentDark(): ColorfulColor {
    return Colorful().getAccentColor().getColorPack().dark()
}