package com.sjn.stamp.ui.activity

import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.sjn.stamp.R
import com.sjn.stamp.ui.DrawerMenu
import com.sjn.stamp.utils.DrawerHelper
import com.sjn.stamp.utils.LogHelper

abstract class DrawerActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    private var mToolbar: Toolbar? = null
    private var mToolbarInitialized: Boolean = false
    protected var mDrawer: DrawerHelper.Drawer? = null

    abstract fun onOptionsItemSelected(itemId: Int): Boolean

    open fun setToolbarTitle(title: CharSequence?) = setTitle(title ?: mDrawer?.selectingDrawerName ?: "")

    open fun navigateToBrowser(fragment: Fragment, addToBackStack: Boolean) {
        if (!addToBackStack) {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, fragment, FRAGMENT_TAG)
            if (addToBackStack) { addToBackStack(null) }
        }.commit()
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true, true)
    }

    protected fun initializeToolbar() {
        mToolbar = findViewById<View>(R.id.toolbar) as Toolbar
        if (mToolbar == null) {
            throw IllegalStateException("Layout is required to include a Toolbar with id " + "'toolbar'")
        }
        mToolbar?.let {
            it.inflateMenu(R.menu.main)
            setSupportActionBar(it)
            mDrawer = DrawerHelper.Drawer(this, it, object : DrawerHelper.Drawer.Listener {
                override fun changeFragmentByDrawer(menu: Long) {
                    val drawerMenu = DrawerMenu.of(menu) ?: return
                    navigateToBrowser(drawerMenu.fragment, false, menu)
                    setToolbarTitle(null)
                }
            })
            mToolbarInitialized = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main, menu)
        try {
            CastButtonFactory.setUpMediaRouteButton(applicationContext, menu, R.id.media_route_menu_item)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        return true
    }

    override fun onBackStackChanged() {
        mDrawer?.updateDrawerToggleState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogHelper.d(TAG, "Activity onCreate")
        try {
            CastContext.getSharedInstance(this)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }

    }

    override fun onStart() {
        super.onStart()
        if (!mToolbarInitialized) {
            throw IllegalStateException("You must run super.initializeToolbar at " + "the end of your onCreate method")
        }
    }

    override fun onResume() {
        super.onResume()
        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    override fun onPause() {
        super.onPause()
        supportFragmentManager.removeOnBackStackChangedListener(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawer?.sync()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawer?.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        mDrawer?.let {
            if (it.onOptionItemSelected(item)) {
                return true
            }
        }
        if (item != null && item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        } else if (item != null) {
            if (!onOptionsItemSelected(item.itemId)) {
                return super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        mDrawer?.let {
            if (it.closeDrawer()) {
                return
            }
        }
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {
            moveTaskToBack(true)
        }
    }

    override fun setTitle(title: CharSequence) {
        super.setTitle(title)
        mToolbar?.title = title
    }

    override fun setTitle(titleId: Int) {
        super.setTitle(titleId)
        mToolbar?.setTitle(titleId)
    }

    private fun navigateToBrowser(fragment: Fragment, addToBackStack: Boolean, selection: Long) {
        navigateToBrowser(fragment, addToBackStack)
        mDrawer?.setSelection(selection)
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(DrawerActivity::class.java)
        const val FRAGMENT_TAG = "fragment_container"
    }
}
