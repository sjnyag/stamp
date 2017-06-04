package com.sjn.taggingplayer.ui.fragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;

import eu.davidea.flexibleadapter.SelectableAdapter;

public interface FragmentInteractionListener {

    void onFragmentChange(SwipeRefreshLayout swipeRefreshLayout, RecyclerView recyclerView,
                          @SelectableAdapter.Mode int mode);

    void initSearchView(final Menu menu);

    void startActionModeByLongClick(int position);

    void destroyActionModeIfCan();
}