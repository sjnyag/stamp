package com.sjn.stamp.utils

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.MediaMetadataCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.Nameable
import com.sjn.stamp.R
import com.sjn.stamp.ui.DrawerMenu
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

object DrawerHelper {

    class Drawer(private val activity: AppCompatActivity, toolbar: Toolbar, private val listener: Listener) {
        interface Listener {
            fun changeFragmentByDrawer(menu: Long)
        }

        private val accountHeader: AccountHeader = AccountHeaderBuilder().apply {
            withSelectionListEnabledForSingleProfile(false)
            withActivity(activity)
            withHeaderBackground(R.drawable.drawer_header)
        }.build()
        private val drawer = DrawerBuilder().apply {
            withActivity(activity)
            withAccountHeader(accountHeader)
            withToolbar(toolbar)
            inflateMenu(R.menu.drawer)
            withSelectedItem(currentMenuId)
            withOnDrawerNavigationListener {
                //this method is only called if the Arrow icon is shown. The hamburger is automatically managed by the MaterialDrawer
                //if the back arrow is shown. close the activity
                activity.onBackPressed()
                //return true if we have consumed the event
                true
            }
            withOnDrawerItemClickListener { _, _, drawerItem ->
                AnalyticsHelper.trackScreen(activity, DrawerMenu.of(drawerItem.identifier))
                nextDrawerMenu = drawerItem.identifier
                false
            }
            withOnDrawerListener(object : com.mikepenz.materialdrawer.Drawer.OnDrawerListener {
                override fun onDrawerOpened(view: View) {}

                override fun onDrawerClosed(view: View) {
                    if (selectingDrawerMenu == nextDrawerMenu) {
                        return
                    }
                    selectingDrawerMenu = nextDrawerMenu
                    listener.changeFragmentByDrawer(nextDrawerMenu)
                }

                override fun onDrawerSlide(view: View, v: Float) {}
            })
        }.build()
        private val drawerToggle: ActionBarDrawerToggle = drawer!!.actionBarDrawerToggle
        private var selectingDrawerMenu: Long = currentMenuId
        private var nextDrawerMenu: Long = 0

        private val currentMenuId: Long
            get() = drawer?.currentSelection ?: DrawerMenu.first().menuId.toLong()

        val selectingDrawerName: String
            get() = if (drawer?.getDrawerItem(drawer.currentSelection) is Nameable<*>) {
                (drawer.getDrawerItem(drawer.currentSelection) as Nameable<*>).name.getText(activity)
            } else activity.getString(R.string.app_name)

        fun updateDrawerToggleState() {
            val isRoot = activity.supportFragmentManager.backStackEntryCount == 0
            drawerToggle.isDrawerIndicatorEnabled = isRoot
            activity.supportActionBar?.let {
                it.setDisplayShowHomeEnabled(!isRoot)
                it.setDisplayHomeAsUpEnabled(!isRoot)
                it.setHomeButtonEnabled(!isRoot)
            }
            if (isRoot) sync()
        }

        fun sync() {
            drawerToggle.syncState()
        }

        fun onConfigurationChanged(newConfig: Configuration) {
            drawerToggle.onConfigurationChanged(newConfig)
        }

        fun setSelection(selection: Long) {
            drawer?.setSelection(selection)
        }

        fun closeDrawer(): Boolean {
            if (drawer?.isDrawerOpen == true) {
                drawer.closeDrawer()
                return true
            }
            return false
        }

        fun onOptionItemSelected(item: MenuItem?): Boolean {
            if (drawerToggle.onOptionsItemSelected(item)) {
                return true
            }
            return false
        }

        fun updateHeader(metadata: MediaMetadataCompat?) {
            metadata?.let {
                activity.runOnUiThread({
                    it.description?.iconUri?.let mediaIcon@ { uri ->
                        ViewHelper.readBitmapAsync(activity, uri.toString(), AccountHeaderLoader(accountHeader, metadata))
                        return@mediaIcon
                    }
                    createDefaultHeader(metadata, accountHeader)
                })
            }
        }
    }

    class AccountHeaderLoader(private val accountHeader: AccountHeader, private val metadata: MediaMetadataCompat) : Target {
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            accountHeader.clear()
            accountHeader.addProfiles(createProfileItem(metadata).apply {
                withIcon(bitmap)
            })
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            createDefaultHeader(metadata, accountHeader)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            createDefaultHeader(metadata, accountHeader)
        }
    }

    private fun createProfileItem(metadata: MediaMetadataCompat): ProfileDrawerItem {
        return ProfileDrawerItem().apply {
            metadata.description?.let { description ->
                description.title?.let {
                    withName(it.toString())
                }
                description.subtitle?.let {
                    withEmail(it.toString())
                }
            }
        }
    }

    private fun createDefaultHeader(metadata: MediaMetadataCompat, accountHeader: AccountHeader) {
        accountHeader.clear()
        accountHeader.addProfiles(createProfileItem(metadata).apply {
            withIcon(ViewHelper.toBitmap(ViewHelper.createTextDrawable(metadata.description.title)))
        })
    }

}