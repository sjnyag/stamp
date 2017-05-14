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
package com.sjn.taggingplayer.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Iterables;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.controller.SongHistoryController;
import com.sjn.taggingplayer.db.Song;
import com.sjn.taggingplayer.db.SongHistory;
import com.sjn.taggingplayer.ui.MediaBrowserProvider;
import com.sjn.taggingplayer.ui.adapter.SongHistoryAdapter;
import com.sjn.taggingplayer.ui.holder.MediaItemViewHolder;
import com.sjn.taggingplayer.ui.item.DateHeaderItem;
import com.sjn.taggingplayer.ui.item.ProgressItem;
import com.sjn.taggingplayer.ui.item.SongHistoryItem;
import com.sjn.taggingplayer.ui.observer.MediaControllerObserver;
import com.sjn.taggingplayer.utils.CompatibleHelper;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.MediaIDHelper;
import com.sjn.taggingplayer.utils.NetworkHelper;
import com.sjn.taggingplayer.utils.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.helpers.ActionModeHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import io.realm.Realm;

import static eu.davidea.flexibleadapter.SelectableAdapter.MODE_MULTI;

/**
 * A Fragment that lists all the various browsable queues available
 * from a {@link android.service.media.MediaBrowserService}.
 * <p/>
 * It uses a {@link MediaBrowserCompat} to connect to the {@link com.sjn.taggingplayer.MusicService}.
 * Once connected, the fragment subscribes to get all the children.
 * All {@link MediaBrowserCompat.MediaItem}'s that can be browsed are shown in a ListView.
 */
public class MediaBrowserFragment extends Fragment implements MediaControllerObserver.Listener, SwipeRefreshLayout.OnRefreshListener,
        ActionMode.Callback, FastScroller.OnScrollStateChangeListener, FlexibleAdapter.OnItemLongClickListener,
        FlexibleAdapter.EndlessScrollListener,FlexibleAdapter.OnItemClickListener {

    private static final String TAG = LogHelper.makeLogTag(MediaBrowserFragment.class);

    private static final String ARG_MEDIA_ID = "media_id";
/*
    private BrowseAdapter mBrowserAdapter;
    private View mErrorView;
    private TextView mErrorMessage;
    */
    private String mMediaId;
    private MediaFragmentListener mMediaFragmentListener;


    private ActionModeHelper mActionModeHelper;
    private RecyclerView mRecyclerView;
    private SongHistoryAdapter mAdapter;
    private SongHistoryController mSongHistoryController;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    protected List<SongHistory> mAllSongHistoryList = new ArrayList<>();
    private Realm mRealm;
    private FloatingActionButton mFab;
    private ProgressItem mProgressItem = new ProgressItem();

    private final BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        private boolean oldOnline = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            LogHelper.d(TAG, "BroadcastReceiver#onReceive ");
            // We don't care about network changes while this fragment is not associated
            // with a media ID (for example, while it is being initialized)
            if (mMediaId != null) {
                boolean isOnline = NetworkHelper.isOnline(context);
                if (isOnline != oldOnline) {
                    oldOnline = isOnline;
                    checkForUserVisibleErrors(false);
                    if (isOnline) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        LogHelper.d(TAG, "Received metadata change to media ",
                metadata.getDescription().getMediaId());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
        LogHelper.d(TAG, "onPlaybackStateChanged ");
        checkForUserVisibleErrors(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemLongClick(int position) {
        mActionModeHelper.onLongClick((AppCompatActivity) getActivity(), position);
    }


    protected LinearLayoutManager createNewLinearLayoutManager() {
        return new SmoothScrollLinearLayoutManager(getActivity());
    }

    public static SongHistoryItem newSimpleItem(MediaBrowserCompat.MediaItem mediaItem, IHeader header) {
        SongHistory songHistory = new SongHistory();
        songHistory.setSong(new Song());
        SongHistoryItem item = new SongHistoryItem(songHistory, (DateHeaderItem) header);
        item.setTitle(songHistory.getSong().getTitle());
        return item;
    }

    public static DateHeaderItem newHeader(SongHistory songHistory) {
        return new DateHeaderItem(songHistory.getRecordedAt());
    }

    private void initializeActionModeHelper() {
        mActionModeHelper = new ActionModeHelper(mAdapter, R.menu.menu_context, this) {
            @Override
            public void updateContextTitle(int count) {
                if (mActionMode != null) {//You can use the internal ActionMode instance
                    mActionMode.setTitle(count == 1 ?
                            getString(R.string.action_selected_one, Integer.toString(count)) :
                            getString(R.string.action_selected_many, Integer.toString(count)));
                }
            }
        }.withDefaultMode(MODE_MULTI);
    }

    @Override
    public void onFastScrollerStateChange(boolean scrolling) {
        if (scrolling) {
            hideFab();
        } else {
            showFab();
        }
    }

    private void hideFab() {
        if (mFab == null) {
            return;
        }
        ViewCompat.animate(mFab)
                .scaleX(0f).scaleY(0f)
                .alpha(0f).setDuration(100)
                .start();
    }

    private void showFab() {
        if (mFab == null) {
            return;
        }
        ViewCompat.animate(mFab)
                .scaleX(1f).scaleY(1f)
                .alpha(1f).setDuration(200)
                .setStartDelay(300L)
                .start();
    }


    @Override
    public void onRefresh() {
        if (mSwipeRefreshLayout == null || getActivity() == null || mSongHistoryController == null || mActionModeHelper == null) {
            return;
        }
        mActionModeHelper.destroyActionModeIfCan();
        mAllSongHistoryList = mSongHistoryController.getManagedTimeline(mRealm);
        mSwipeRefreshLayout.setRefreshing(false);
        //mAdapter.updateDataSet(getItemList(0, 30));
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (CompatibleHelper.hasMarshmallow()) {
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.color_accent_dark, getActivity().getTheme()));
        } else if (CompatibleHelper.hasLollipop()) {
            //noinspection deprecation
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.color_accent_dark));
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
                mActionModeHelper.updateContextTitle(mAdapter.getSelectedItemCount());
                // We consume the event
                return true;

            case R.id.action_delete:
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
                return true;
            default:
                // If an item is not implemented we don't consume the event, so we finish the ActionMode
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (CompatibleHelper.hasMarshmallow()) {
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.color_primary_dark, getActivity().getTheme()));
        } else if (CompatibleHelper.hasLollipop()) {
            //noinspection deprecation
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.color_primary_dark));
        }
    }

    private String extractTitleFrom(IFlexible flexibleItem) {
        return "";
    }


    @Override
    public void noMoreLoad(int newItemsSize) {
        Log.d(TAG, "newItemsSize=" + newItemsSize);
        Log.d(TAG, "Total pages loaded=" + mAdapter.getEndlessCurrentPage());
        Log.d(TAG, "Total items loaded=" + mAdapter.getMainItemCount());

    }

    @Override
    public void onLoadMore(int lastPosition, int currentPage) {
        //mAdapter.onLoadMoreComplete(getItemList(mAdapter.getMainItemCount() - mAdapter.getHeaderItems().size(), 30), 5000L);
    }

    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    try {
                        LogHelper.d(TAG, "fragment onChildrenLoaded, parentId=" + parentId +
                                "  count=" + children.size());
                        checkForUserVisibleErrors(children.isEmpty());
                        mAdapter.clear();
                        for (MediaBrowserCompat.MediaItem item : children) {
                            mAdapter.addItem(newSimpleItem(item, null));
                        }
                        mAdapter.notifyDataSetChanged();
                    } catch (Throwable t) {
                        LogHelper.e(TAG, "Error on childrenloaded", t);
                    }
                }

                @Override
                public void onError(@NonNull String id) {
                    LogHelper.e(TAG, "browse fragment subscription onError, id=" + id);
                    Toast.makeText(getActivity(), R.string.error_loading_media, Toast.LENGTH_LONG).show();
                    checkForUserVisibleErrors(true);
                }
            };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // If used on an activity that doesn't implement MediaFragmentListener, it
        // will throw an exception as expected:
        mMediaFragmentListener = (MediaFragmentListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogHelper.d(TAG, "fragment.onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
/*
        mErrorView = rootView.findViewById(R.id.playback_error);
        mErrorMessage = (TextView) mErrorView.findViewById(R.id.error_message);
*/


        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);

        mAdapter = new SongHistoryAdapter(new ArrayList<AbstractFlexibleItem>(), this);
        mAdapter.setNotifyChangeOfUnfilteredItems(true)
                .setAnimationOnScrolling(false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
        mRecyclerView.setAdapter(mAdapter);
        //mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter.setFastScroller((FastScroller) rootView.findViewById(R.id.fast_scroller),
                ViewHelper.getColorAccent(getActivity()), this);

        mAdapter.setLongPressDragEnabled(true)
                .setHandleDragEnabled(true)
                .setSwipeEnabled(true)
                .setUnlinkAllItemsOnRemoveHeaders(false)
                .setDisplayHeadersAtStartUp(false)
                .setStickyHeaders(true)
                .showAllHeaders();
        mAdapter.addUserLearnedSelection(savedInstanceState == null);
        //mAdapter.addScrollableHeaderWithDelay(new DateHeaderItem(TimeHelper.getJapanNow().toDate()), 900L, false);
        mAdapter.showLayoutInfo(savedInstanceState == null);
        mAdapter.addScrollableFooter();


        // EndlessScrollListener - OnLoadMore (v5.0.0)
        mAdapter//.setLoadingMoreAtStartUp(true) //To call only if the list is empty
                //.setEndlessPageSize(3) //Endless is automatically disabled if newItems < 3
                //.setEndlessTargetCount(15) //Endless is automatically disabled if totalItems >= 15
                //.setEndlessScrollThreshold(1); //Default=1
                .setEndlessScrollListener(this, mProgressItem);

        initializeActionModeHelper();
        /*
        mBrowserAdapter = new BrowseAdapter(getActivity());

        ListView listView = (ListView) rootView.findViewById(R.id.list_view);
        listView.setAdapter(mBrowserAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogHelper.d(TAG, "onItemClick ");
                checkForUserVisibleErrors(false);
                MediaBrowserCompat.MediaItem item = mBrowserAdapter.getItem(position);
                mMediaFragmentListener.onMediaItemSelected(item);
            }
        });
        */

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // fetch browsing information to fill the listview:
        MediaBrowserCompat mediaBrowser = mMediaFragmentListener.getMediaBrowser();

        LogHelper.d(TAG, "fragment.onStart, mediaId=", mMediaId,
                "  onConnected=" + mediaBrowser.isConnected());

        if (mediaBrowser.isConnected()) {
            onConnected();
        }

        // Registers BroadcastReceiver to track network connection changes.
        this.getActivity().registerReceiver(mConnectivityChangeReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onStop() {
        super.onStop();
        MediaBrowserCompat mediaBrowser = mMediaFragmentListener.getMediaBrowser();
        if (mediaBrowser != null && mediaBrowser.isConnected() && mMediaId != null) {
            mediaBrowser.unsubscribe(mMediaId);
        }
        MediaControllerObserver.getInstance().removeListener(this);
        this.getActivity().unregisterReceiver(mConnectivityChangeReceiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMediaFragmentListener = null;
    }

    public String getMediaId() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString(ARG_MEDIA_ID);
        }
        return null;
    }

    public void setMediaId(String mediaId) {
        Bundle args = new Bundle(1);
        args.putString(MediaBrowserFragment.ARG_MEDIA_ID, mediaId);
        setArguments(args);
    }

    // Called when the MediaBrowser is connected. This method is either called by the
    // fragment.onStart() or explicitly by the activity in the case where the connection
    // completes after the onStart()
    @Override
    public void onConnected() {
        if (isDetached() || mMediaFragmentListener == null) {
            return;
        }
        mMediaId = getMediaId();
        if (mMediaId == null) {
            mMediaId = mMediaFragmentListener.getMediaBrowser().getRoot();
        }
        updateTitle();

        // Unsubscribing before subscribing is required if this mediaId already has a subscriber
        // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
        // the callback, but won't trigger the initial callback.onChildrenLoaded.
        //
        // This is temporary: A bug is being fixed that will make subscribe
        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
        // subscriber or not. Currently this only happens if the mediaID has no previous
        // subscriber or if the media content changes on the service side, so we need to
        // unsubscribe first.
        mMediaFragmentListener.getMediaBrowser().unsubscribe(mMediaId);

        mMediaFragmentListener.getMediaBrowser().subscribe(mMediaId, mSubscriptionCallback);

        // Add MediaController callback so we can redraw the list when metadata changes:
        MediaControllerObserver.getInstance().addListener(this);
    }

    private void checkForUserVisibleErrors(boolean forceError) {
        /*
        boolean showError = forceError;
        // If offline, message is about the lack of connectivity:
        if (!NetworkHelper.isOnline(getActivity())) {
            mErrorMessage.setText(R.string.error_no_connection);
            showError = true;
        } else {
            // otherwise, if state is ERROR and metadata!=null, use playback state error message:
            MediaControllerCompat controller = ((FragmentActivity) getActivity())
                    .getSupportMediaController();
            if (controller != null
                    && controller.getMetadata() != null
                    && controller.getPlaybackState() != null
                    && controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_ERROR
                    && controller.getPlaybackState().getErrorMessage() != null) {
                mErrorMessage.setText(controller.getPlaybackState().getErrorMessage());
                showError = true;
            } else if (forceError) {
                // Finally, if the caller requested to show error, show a generic message:
                mErrorMessage.setText(R.string.error_loading_media);
                showError = true;
            }
        }
        mErrorView.setVisibility(showError ? View.VISIBLE : View.GONE);
        LogHelper.d(TAG, "checkForUserVisibleErrors. forceError=", forceError,
                " showError=", showError,
                " isOnline=", NetworkHelper.isOnline(getActivity()));
                */
    }

    private void updateTitle() {
        if (MediaIDHelper.MEDIA_ID_ROOT.equals(mMediaId)) {
            mMediaFragmentListener.setToolbarTitle(null);
            return;
        }

        MediaBrowserCompat mediaBrowser = mMediaFragmentListener.getMediaBrowser();
        mediaBrowser.getItem(mMediaId, new MediaBrowserCompat.ItemCallback() {
            @Override
            public void onItemLoaded(MediaBrowserCompat.MediaItem item) {
                mMediaFragmentListener.setToolbarTitle(
                        item.getDescription().getTitle());
            }
        });
    }

    @Override
    public boolean onItemClick(int position) {
        LogHelper.d(TAG, "onItemClick ");
        checkForUserVisibleErrors(false);
        /*TODO
        MediaBrowserCompat.MediaItem item = mAdapter.getItem(position);
        mMediaFragmentListener.onMediaItemSelected(item);
        */
        return false;
    }

    // An adapter for showing the list of browsed MediaItem's
    private static class BrowseAdapter extends ArrayAdapter<MediaBrowserCompat.MediaItem> {

        public BrowseAdapter(Activity context) {
            super(context, R.layout.list_item_media, new ArrayList<MediaBrowserCompat.MediaItem>());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MediaBrowserCompat.MediaItem item = getItem(position);
            return MediaItemViewHolder.setupListView((Activity) getContext(), convertView, parent,
                    item);
        }
    }

    public interface MediaFragmentListener extends MediaBrowserProvider {
        void onMediaItemSelected(MediaBrowserCompat.MediaItem item);

        void setToolbarTitle(CharSequence title);
    }

}
