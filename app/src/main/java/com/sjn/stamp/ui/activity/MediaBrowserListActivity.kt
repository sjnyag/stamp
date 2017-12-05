package com.sjn.stamp.ui.activity

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.support.v4.view.MenuItemCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.view.ActionMode
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import com.sjn.stamp.R
import com.sjn.stamp.ui.fragment.media_list.ListFragment
import com.sjn.stamp.ui.observer.StampEditStateObserver
import com.sjn.stamp.utils.CompatibleHelper
import com.sjn.stamp.utils.LogHelper
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.helpers.ActionModeHelper
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import java.util.*

abstract class MediaBrowserListActivity : MediaBrowserActivity(), SearchView.OnQueryTextListener, ListFragment.FragmentInteractionListener, ActionMode.Callback {

    private var mSearchView: SearchView? = null
    private var mAdapter: FlexibleAdapter<AbstractFlexibleItem<*>>? = null
    private var mActionModeHelper: ActionModeHelper? = null

    abstract val currentMediaItems: List<AbstractFlexibleItem<*>>
    abstract val menuResourceId: Int

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        LogHelper.d(TAG, "onPrepareOptionsMenu START")
        if (mSearchView != null && mAdapter != null) {
            //Has searchText?
            if (!mAdapter!!.hasSearchText()) {
                LogHelper.i(TAG, "onPrepareOptionsMenu Clearing SearchView!")
                mSearchView!!.isIconified = true// This also clears the text in SearchView widget
            } else {
                //Necessary after the restoreInstanceState
                menu.findItem(R.id.action_search).expandActionView()//must be called first
                //This restores the text, must be after the expandActionView()
                mSearchView!!.setQuery(mAdapter!!.searchText, false)//submit = false!!!
                mSearchView!!.clearFocus()//Optionally the keyboard can be closed
                //mSearchView.setIconified(false);//This is not necessary
            }
        }
        LogHelper.d(TAG, "onPrepareOptionsMenu END")
        return super.onPrepareOptionsMenu(menu)
    }

    override fun initSearchView(menu: Menu) {
        // Associate searchable configuration with the SearchView
        LogHelper.d(TAG, "initSearchView START")
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.action_search)
        if (searchItem != null) {
            mSearchView = MenuItemCompat.getActionView(searchItem) as SearchView
            mSearchView!!.inputType = InputType.TYPE_TEXT_VARIATION_FILTER
            mSearchView!!.imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_FULLSCREEN
            mSearchView!!.queryHint = getString(R.string.action_search)
            mSearchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            mSearchView!!.setOnQueryTextListener(this)
        }
        LogHelper.d(TAG, "initSearchView END")
    }

    private fun initializeActionModeHelper(mode: Int) {
        LogHelper.d(TAG, "initializeActionModeHelper START")
        mActionModeHelper = object : ActionModeHelper(mAdapter!!, menuResourceId, this) {
            override fun updateContextTitle(count: Int) {
                if (mActionMode != null) {//You can use the internal ActionMode instance
                    mActionMode.title = if (count == 1)
                        getString(R.string.action_selected_one, Integer.toString(count))
                    else
                        getString(R.string.action_selected_many, Integer.toString(count))
                }
            }
        }.withDefaultMode(mode)
        LogHelper.d(TAG, "initializeActionModeHelper END")
    }

    override fun startActionModeByLongClick(position: Int) {
        LogHelper.d(TAG, "startActionModeByLongClick START")
        if (mActionModeHelper != null) {
            mActionModeHelper!!.onLongClick(this, position)
        }
        LogHelper.d(TAG, "startActionModeByLongClick END")
    }

    override fun destroyActionModeIfCan() {
        LogHelper.d(TAG, "destroyActionModeIfCan START")
        if (mActionModeHelper != null) {
            mActionModeHelper!!.destroyActionModeIfCan()
        }
        LogHelper.d(TAG, "destroyActionModeIfCan END")
    }

    override fun onFragmentChange(swipeRefreshLayout: SwipeRefreshLayout, recyclerView: RecyclerView, mode: Int) {
        LogHelper.d(TAG, "onFragmentChange START")
        //mRecyclerView = recyclerView;
        mAdapter = recyclerView.adapter as FlexibleAdapter<AbstractFlexibleItem<*>>
        //mSwipeRefreshLayout = swipeRefreshLayout;
        //initializeSwipeToRefresh();
        initializeActionModeHelper(mode)
        LogHelper.d(TAG, "onFragmentChange END")
    }

    override fun onQueryTextChange(newText: String): Boolean {
        LogHelper.d(TAG, "onQueryTextChange START")
        if (mAdapter!!.hasNewSearchText(newText)) {
            LogHelper.i(TAG, "onQueryTextChange newText: " + newText)
            mAdapter!!.searchText = newText
            // Fill and Filter mItems with your custom list and automatically animate the changes
            // Watch out! The original list must be a copy
            var items: List<AbstractFlexibleItem<*>>? = currentMediaItems
            if (items == null) {
                items = ArrayList()
            }
            LogHelper.i(TAG, "onQueryTextChange items.size(): " + items.size)
            mAdapter!!.filterItems(items, 200)
        }
        // Disable SwipeRefresh if search is active!!
        //mSwipeRefreshLayout.setEnabled(!mAdapter.hasSearchText());
        LogHelper.d(TAG, "onQueryTextChange END")
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        LogHelper.d(TAG, "onQueryTextSubmit START")
        return onQueryTextChange(query)
    }

    override fun onBackPressed() {
        if (StampEditStateObserver.getInstance().isStampMode) {
            StampEditStateObserver.getInstance().notifyStateChange(StampEditStateObserver.State.NO_EDIT)
            return
        }
        // If ActionMode is active, back key closes it
        if (mActionModeHelper != null && mActionModeHelper!!.destroyActionModeIfCan()) {
            return
        }
        // If SearchView is visible, back key cancels search and iconify it
        if (mSearchView != null && !mSearchView!!.isIconified) {
            mSearchView!!.isIconified = true
            return
        }
        super.onBackPressed()
    }

    @SuppressLint("NewApi")
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        LogHelper.d(TAG, "onCreateActionMode START")
        if (CompatibleHelper.hasMarshmallow()) {
            window.statusBarColor = resources.getColor(R.color.color_accent_dark, theme)
        } else if (CompatibleHelper.hasLollipop()) {

            window.statusBarColor = resources.getColor(R.color.color_accent_dark)
        }
        LogHelper.d(TAG, "onCreateActionMode END")
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = true

    @SuppressLint("NewApi")
    override fun onDestroyActionMode(mode: ActionMode) {
        LogHelper.d(TAG, "onDestroyActionMode START")
        if (CompatibleHelper.hasMarshmallow()) {
            window.statusBarColor = resources.getColor(R.color.color_primary_dark, theme)
        } else if (CompatibleHelper.hasLollipop()) {

            window.statusBarColor = resources.getColor(R.color.color_primary_dark)
        }
        LogHelper.d(TAG, "onDestroyActionMode END")
    }

    override fun updateContextTitle(selectedItemCount: Int) {
        LogHelper.d(TAG, "updateContextTitle START")
        mActionModeHelper!!.updateContextTitle(selectedItemCount)
        LogHelper.d(TAG, "updateContextTitle END")
    }


    override fun restoreSelection() {
        LogHelper.d(TAG, "restoreSelection START")
        mActionModeHelper!!.restoreSelection(this)
        LogHelper.d(TAG, "restoreSelection END")
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(MediaBrowserListActivity::class.java)
    }

}
