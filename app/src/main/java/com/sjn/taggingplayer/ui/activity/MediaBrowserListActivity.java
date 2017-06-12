/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sjn.taggingplayer.ui.activity;

import android.app.SearchManager;
import android.content.Context;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.ui.fragment.media_list.ListFragment;
import com.sjn.taggingplayer.utils.CompatibleHelper;
import com.sjn.taggingplayer.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.ActionModeHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * Base activity for activities that need to show a playback control fragment when media is playing.
 */
public abstract class MediaBrowserListActivity extends MediaBrowserActivity
        implements SearchView.OnQueryTextListener, ListFragment.FragmentInteractionListener, ActionMode.Callback {

    private static final String TAG = LogHelper.makeLogTag(MediaBrowserListActivity.class);

    private SearchView mSearchView;
    private FlexibleAdapter<AbstractFlexibleItem> mAdapter;
    private ActionModeHelper mActionModeHelper;

    abstract public List<AbstractFlexibleItem> getCurrentMediaItems();

    abstract public int getMenuResourceId();

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mSearchView != null && mAdapter != null) {
            //Has searchText?
            if (!mAdapter.hasSearchText()) {
                LogHelper.i(TAG, "onPrepareOptionsMenu Clearing SearchView!");
                mSearchView.setIconified(true);// This also clears the text in SearchView widget
            } else {
                //Necessary after the restoreInstanceState
                menu.findItem(R.id.action_search).expandActionView();//must be called first
                //This restores the text, must be after the expandActionView()
                mSearchView.setQuery(mAdapter.getSearchText(), false);//submit = false!!!
                mSearchView.clearFocus();//Optionally the keyboard can be closed
                //mSearchView.setIconified(false);//This is not necessary
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void initSearchView(final Menu menu) {
        // Associate searchable configuration with the SearchView
        LogHelper.i(TAG, "onCreateOptionsMenu setup SearchView!");
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            MenuItemCompat.setOnActionExpandListener(
                    searchItem, new MenuItemCompat.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
//                            MenuItem listTypeItem = menu.findItem(R.id.action_list_type);
//                            if (listTypeItem != null)
//                                listTypeItem.setVisible(false);
                            //hideFab();
                            return true;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
//                            MenuItem listTypeItem = menu.findItem(R.id.action_list_type);
//                            if (listTypeItem != null)
//                                listTypeItem.setVisible(true);
                            //showFab();
                            return true;
                        }
                    });
            mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER);
            mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_FULLSCREEN);
            mSearchView.setQueryHint(getString(R.string.action_search));
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            mSearchView.setOnQueryTextListener(this);
        }
    }

    private void initializeActionModeHelper(int mode) {
        mActionModeHelper = new ActionModeHelper(mAdapter, getMenuResourceId(), this) {
            @Override
            public void updateContextTitle(int count) {
                if (mActionMode != null) {//You can use the internal ActionMode instance
                    mActionMode.setTitle(count == 1 ?
                            getString(R.string.action_selected_one, Integer.toString(count)) :
                            getString(R.string.action_selected_many, Integer.toString(count)));
                }
            }
        }.withDefaultMode(mode);
    }

    @Override
    public void startActionModeByLongClick(int position) {
        if (mActionModeHelper != null) {
            mActionModeHelper.onLongClick(this, position);
        }
    }

    @Override
    public void destroyActionModeIfCan() {
        if (mActionModeHelper != null) {
            mActionModeHelper.destroyActionModeIfCan();
        }
    }

    @Override
    public void onFragmentChange(SwipeRefreshLayout swipeRefreshLayout, RecyclerView recyclerView, int mode) {
        //mRecyclerView = recyclerView;
        mAdapter = (FlexibleAdapter) recyclerView.getAdapter();
        //mSwipeRefreshLayout = swipeRefreshLayout;
        //initializeSwipeToRefresh();
        initializeActionModeHelper(mode);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (mAdapter.hasNewSearchText(newText)) {
            LogHelper.i(TAG, "onQueryTextChange newText: " + newText);
            mAdapter.setSearchText(newText);
            // Fill and Filter mItems with your custom list and automatically animate the changes
            // Watch out! The original list must be a copy
            List<AbstractFlexibleItem> items = getCurrentMediaItems();
            if (items == null) {
                items = new ArrayList<>();
            }
            LogHelper.i(TAG, "onQueryTextChange items.size(): " + items.size());
            mAdapter.filterItems(new ArrayList<>(items), 200);
        }
        // Disable SwipeRefresh if search is active!!
        //mSwipeRefreshLayout.setEnabled(!mAdapter.hasSearchText());
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        LogHelper.i(TAG, "onQueryTextSubmit called!");
        return onQueryTextChange(query);
    }

    @Override
    public void onBackPressed() {
        // If ActionMode is active, back key closes it
        if (mActionModeHelper != null && mActionModeHelper.destroyActionModeIfCan()) return;
        // If SearchView is visible, back key cancels search and iconify it
        if (mSearchView != null && !mSearchView.isIconified()) {
            mSearchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (CompatibleHelper.hasMarshmallow()) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.color_accent_dark, getTheme()));
        } else if (CompatibleHelper.hasLollipop()) {
            //noinspection deprecation
            getWindow().setStatusBarColor(getResources().getColor(R.color.color_accent_dark));
        }
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_all:
                mAdapter.selectAll();
                if (mActionModeHelper != null) {
                    mActionModeHelper.updateContextTitle(mAdapter.getSelectedItemCount());
                }
                // We consume the event
                return true;

            case R.id.action_delete:
                /*
                // Build message before delete, for the SnackBar
                StringBuilder message = new StringBuilder();
                message.append(getString(R.string.action_deleted)).append(" ");
                for (Integer pos : mAdapter.getSelectedPositions()) {
                    message.append(extractTitleFrom(mAdapter.getItem(pos)));
                    if (mAdapter.getSelectedItemCount() > 1)
                        message.append(", ");
                }

                // Experimenting NEW feature
                mAdapter.setRestoreSelectionOnUndo(true);
                // We consume the event
                */
                return true;
            default:
                // If an item is not implemented we don't consume the event, so we finish the ActionMode
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (CompatibleHelper.hasMarshmallow()) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.color_primary_dark, getTheme()));
        } else if (CompatibleHelper.hasLollipop()) {
            //noinspection deprecation
            getWindow().setStatusBarColor(getResources().getColor(R.color.color_primary_dark));
        }
    }


}
