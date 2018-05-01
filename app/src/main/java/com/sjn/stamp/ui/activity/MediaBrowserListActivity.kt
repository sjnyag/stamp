package com.sjn.stamp.ui.activity

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.graphics.Color
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.view.ActionMode
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import com.sjn.stamp.R
import com.sjn.stamp.ui.fragment.media.ListFragment
import com.sjn.stamp.ui.observer.StampEditStateObserver
import com.sjn.stamp.utils.CompatibleHelper
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.ViewHelper
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.helpers.ActionModeHelper
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import java.util.*


abstract class MediaBrowserListActivity : MediaBrowserActivity(), SearchView.OnQueryTextListener, ListFragment.FragmentInteractionListener, ActionMode.Callback {

    private var searchView: SearchView? = null
    private var adapter: FlexibleAdapter<AbstractFlexibleItem<*>>? = null
    private var actionModeHelper: ActionModeHelper? = null

    abstract val currentMediaItems: List<AbstractFlexibleItem<*>>
    abstract val menuResourceId: Int

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        LogHelper.d(TAG, "onPrepareOptionsMenu START")
        adapter?.let { adapter ->
            searchView?.let { searchView ->
                //Has searchText?
                if (!adapter.hasSearchText()) {
                    LogHelper.i(TAG, "onPrepareOptionsMenu Clearing SearchView!")
                    searchView.isIconified = true// This also clears the text in SearchView widget
                } else {
                    //Necessary after the restoreInstanceState
                    menu.findItem(R.id.action_search).expandActionView()//must be called first
                    //This restores the text, must be after the expandActionView()
                    searchView.setQuery(adapter.searchText, false)//submit = false!!!
                    searchView.clearFocus()//Optionally the keyboard can be closed
                    //mSearchView.setIconified(false);//This is not necessary
                }
            }
        }
        LogHelper.d(TAG, "onPrepareOptionsMenu END")
        return super.onPrepareOptionsMenu(menu)
    }

    override fun initSearchView(menu: Menu?) {
        // Associate searchable configuration with the SearchView
        LogHelper.d(TAG, "initSearchView START")
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        menu?.findItem(R.id.action_search)?.let { item ->
            searchView = (item.actionView as SearchView).apply {
                //inputType = InputType.TYPE_TEXT_VARIATION_FILTER
                imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_FULLSCREEN
                queryHint = getString(R.string.action_search)
                setSearchableInfo(searchManager.getSearchableInfo(componentName))
                setOnQueryTextListener(this@MediaBrowserListActivity)
            }
        }
        LogHelper.d(TAG, "initSearchView END")
    }

    private fun initializeActionModeHelper(mode: Int) {
        LogHelper.d(TAG, "initializeActionModeHelper START")
        adapter?.let {
            actionModeHelper = object : ActionModeHelper(it, menuResourceId, this) {
                override fun updateContextTitle(count: Int) {
                    //You can use the internal ActionMode instance
                    mActionMode?.title = if (count == 1) getString(R.string.action_selected_one, Integer.toString(count)) else getString(R.string.action_selected_many, Integer.toString(count))
                }
            }.withDefaultMode(mode)
        }
        LogHelper.d(TAG, "initializeActionModeHelper END")
    }

    override fun startActionModeByLongClick(position: Int) {
        LogHelper.d(TAG, "startActionModeByLongClick START")
        actionModeHelper?.onLongClick(this, position)
        LogHelper.d(TAG, "startActionModeByLongClick END")
    }

    override fun destroyActionModeIfCan() {
        LogHelper.d(TAG, "destroyActionModeIfCan START")
        actionModeHelper?.destroyActionModeIfCan()
        LogHelper.d(TAG, "destroyActionModeIfCan END")
    }

    override fun onFragmentChange(swipeRefreshLayout: SwipeRefreshLayout?, recyclerView: RecyclerView?, mode: Int) {
        LogHelper.d(TAG, "onFragmentChange START")
        //recyclerView = recyclerView;

        if (recyclerView?.adapter is FlexibleAdapter<*>) {
            adapter = recyclerView.adapter as FlexibleAdapter<AbstractFlexibleItem<*>>
        }
        //swipeRefreshLayout = swipeRefreshLayout;
        //initializeSwipeToRefresh();
        initializeActionModeHelper(mode)
        updateAppbar()
        LogHelper.d(TAG, "onFragmentChange END")
    }

    override fun onQueryTextChange(newText: String): Boolean {
        LogHelper.d(TAG, "onQueryTextChange START")
        adapter?.let {
            if (it.hasNewSearchText(newText)) {
                LogHelper.i(TAG, "onQueryTextChange newText: $newText")
                it.searchText = newText
                // Fill and Filter mItems with your custom list and automatically animate the changes
                // Watch out! The original list must be a copy
                it.filterItems(ArrayList(currentMediaItems), 200)
            }

        }
        // Disable SwipeRefresh if search is active!!
        //swipeRefreshLayout.setEnabled(!adapter.hasSearchText());
        LogHelper.d(TAG, "onQueryTextChange END")
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        LogHelper.d(TAG, "onQueryTextSubmit START")
        return onQueryTextChange(query)
    }

    override fun onBackPressed() {
        if (StampEditStateObserver.isStampMode) {
            StampEditStateObserver.notifyStateChange(StampEditStateObserver.State.NO_EDIT)
            return
        }
        // If ActionMode is active, back key closes it
        if (actionModeHelper?.destroyActionModeIfCan() == true) {
            return
        }
        // If SearchView is visible, back key cancels search and iconify it
        if (searchView?.isIconified == false) {
            searchView?.isIconified = true
            return
        }
        super.onBackPressed()
    }

    @SuppressLint("NewApi")
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        LogHelper.d(TAG, "onCreateActionMode START")
        CompatibleHelper.getColor(resources, ViewHelper.getThemeColor(this, R.attr.colorAccent, Color.DKGRAY), theme)?.let { window.statusBarColor = it }
        LogHelper.d(TAG, "onCreateActionMode END")
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = true

    @SuppressLint("NewApi")
    override fun onDestroyActionMode(mode: ActionMode) {
        LogHelper.d(TAG, "onDestroyActionMode START")
        window.statusBarColor = ViewHelper.getThemeColor(this, R.attr.colorPrimaryDark, Color.DKGRAY)
        LogHelper.d(TAG, "onDestroyActionMode END")
    }

    override fun updateContextTitle(selectedItemCount: Int) {
        LogHelper.d(TAG, "updateContextTitle START")
        actionModeHelper?.updateContextTitle(selectedItemCount)
        LogHelper.d(TAG, "updateContextTitle END")
    }


    override fun restoreSelection() {
        LogHelper.d(TAG, "restoreSelection START")
        actionModeHelper?.restoreSelection(this)
        LogHelper.d(TAG, "restoreSelection END")
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(MediaBrowserListActivity::class.java)
    }

}
