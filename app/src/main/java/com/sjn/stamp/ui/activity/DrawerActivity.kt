package com.sjn.stamp.ui.activity

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.github.sjnyag.forceanimateappbarlayout.ForceAnimateAppBarLayout
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.sjn.stamp.R
import com.sjn.stamp.ui.DrawerMenu
import com.sjn.stamp.ui.fragment.media.PagerFragment
import com.sjn.stamp.utils.DrawerHelper
import com.sjn.stamp.utils.LogHelper

abstract class DrawerActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    private var toolbar: Toolbar? = null
    protected var drawer: DrawerHelper.Drawer? = null

    abstract fun onOptionsItemSelected(itemId: Int): Boolean

    open fun setToolbarTitle(title: CharSequence?) = setTitle(title ?: drawer?.selectingDrawerName
    ?: "")

    open fun navigateToBrowser(fragment: Fragment, addToBackStack: Boolean, sharedElements: List<Pair<String, View>> = emptyList()) {
        if (!addToBackStack) {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, fragment, FRAGMENT_TAG)
            if (addToBackStack) {
                addToBackStack(null)
            }
            sharedElements.forEach { pair -> addSharedElement(pair.second, pair.first) }
        }.commit()
    }

    fun updateAppbar(title: String?, function: (activity: Activity, imageView: ImageView) -> Unit) {
        findViewById<Toolbar>(R.id.toolbar)?.also { toolbar ->
            toolbar.title = title
        }
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.also { toolbar ->
            toolbar.isTitleEnabled = true
            toolbar.title = title
        }
        findViewById<ImageView>(R.id.app_bar_image)?.also { imageView ->
            imageView.visibility = View.VISIBLE
            function(this, imageView)
        }
        findViewById<ForceAnimateAppBarLayout>(R.id.app_bar).run {
            animatedExpand()
        }
    }

    fun updateAppbar() {
        val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
        findViewById<AppBarLayout>(R.id.app_bar).run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (fragment is PagerFragment) {
                    elevation = 0F
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (fragment !is PagerFragment) {
                    elevation = 8F
                }
            }
        }
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.also { toolbar ->
            toolbar.isTitleEnabled = false
        }
        findViewById<ImageView>(R.id.app_bar_image)?.also { imageView ->
            imageView.visibility = View.GONE
        }
    }

    protected fun initializeToolbar() {
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar?
        toolbar?.let {
            it.inflateMenu(R.menu.main)
            setSupportActionBar(it)
            drawer = DrawerHelper.Drawer(this, it, object : DrawerHelper.Drawer.Listener {
                override fun changeFragmentByDrawer(menu: Long) {
                    val drawerMenu = DrawerMenu.of(menu) ?: return
                    navigateToBrowser(drawerMenu.fragment, false, menu)
                    setToolbarTitle(null)
                }
            })
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
        drawer?.updateDrawerToggleState()
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
        drawer?.sync()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawer?.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        drawer?.let {
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
        drawer?.let {
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
        toolbar?.title = title
    }

    override fun setTitle(titleId: Int) {
        super.setTitle(titleId)
        toolbar?.setTitle(titleId)
    }

    private fun navigateToBrowser(fragment: Fragment, addToBackStack: Boolean, selection: Long) {
        navigateToBrowser(fragment, addToBackStack)
        drawer?.setSelection(selection)
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(DrawerActivity::class.java)
        const val FRAGMENT_TAG = "fragment_container"
    }
}
