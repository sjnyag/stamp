package com.sjn.stamp.ui.fragment.media

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sjn.stamp.R
import com.sjn.stamp.utils.ViewHelper
import java.util.*

abstract class PagerFragment : FabFragment() {

    private var viewPager: ViewPager? = null
    protected var viewPagerAdapter: ViewPagerAdapter? = null
    protected var fragments: List<PageFragmentContainer>? = null

    val current: Fragment?
        get() = viewPager?.let {
            viewPagerAdapter?.getItem(it.currentItem)
        }

    internal abstract fun setUpFragmentContainer(): List<PageFragmentContainer>

    private fun setupViewPager(viewPager: ViewPager, savedInstanceState: Bundle?) {
        fragments ?: run {
            fragments = setUpFragmentContainer()
        }
        viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        fragments?.let {
            for (fragmentContainer in it) {
                fragmentContainer.apply {
                    findOrCreate(savedInstanceState, childFragmentManager)
                    fragmentContainer.fragment?.let {
                        viewPagerAdapter?.addFragment(it, fragmentContainer.label)
                    }
                }
            }
        }
        viewPager.adapter = viewPagerAdapter
    }

    class PageFragmentContainer(var label: String, private var fragmentHint: String, private var creator: Creator) {
        var fragment: Fragment? = null
        var tag: String = label + fragmentHint

        fun findOrCreate(savedInstanceState: Bundle?, fragmentManager: FragmentManager) {
            if (fragment != null) {
                return
            }
            fragment = if (fragmentManager.findFragmentByTag(tag) != null) {
                fragmentManager.getFragment(savedInstanceState, tag)
            } else {
                creator.create(fragmentHint)
            }
        }

        interface Creator {
            fun create(fragmentHint: String): Fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_pager, container, false)
        viewPager = rootView.findViewById<View>(R.id.viewpager) as ViewPager
        viewPager?.let {
            setupViewPager(it, savedInstanceState)
        }
        val tabLayout = rootView.findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(viewPager)
        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragments?.let {
            it.filter { it.fragment?.isAdded == true }
                    .forEach { childFragmentManager.putFragment(outState, it.tag, it.fragment) }

        }
    }

    protected fun setTitle(title: String) {
        ViewHelper.setFragmentTitle(activity, title)
    }

    inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val fragmentList = ArrayList<Fragment>()
        private val fragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }
    }

    companion object {
        internal const val PAGER_KIND_KEY = "PAGER_KIND"
    }


}