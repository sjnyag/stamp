package com.sjn.stamp

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import android.widget.ImageView

import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.sjn.stamp.utils.RealmHelper
import com.squareup.picasso.Picasso

import net.danlew.android.joda.JodaTimeAndroid

class StampApplication : MultiDexApplication() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        RealmHelper.init(this)
        JodaTimeAndroid.init(this)
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView?, uri: Uri?, placeholder: Drawable?) {
                Picasso.with(imageView!!.context).load(uri).placeholder(placeholder).into(imageView)
            }

            override fun cancel(imageView: ImageView?) {
                Picasso.with(imageView!!.context).cancelRequest(imageView)
            }
        })
    }
}
