package com.sjn.taggingplayer.ui.fragment.media_list;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Iterables;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.controller.SongHistoryController;
import com.sjn.taggingplayer.db.SongHistory;
import com.sjn.taggingplayer.ui.SongAdapter;
import com.sjn.taggingplayer.ui.item.AbstractItem;
import com.sjn.taggingplayer.ui.item.DateHeaderItem;
import com.sjn.taggingplayer.ui.item.SongHistoryItem;
import com.sjn.taggingplayer.ui.observer.TagEditStateObserver;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.RealmHelper;
import com.sjn.taggingplayer.utils.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.helpers.UndoHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.utils.Utils;
import io.realm.Realm;

public class TimelineFragment extends MediaBrowserListFragment implements
        UndoHelper.OnUndoListener, FlexibleAdapter.OnItemSwipeListener {

    private static final String TAG = LogHelper.makeLogTag(TimelineFragment.class);

    private SongAdapter mAdapter;
    private SongHistoryController mSongHistoryController;
    protected List<SongHistory> mAllSongHistoryList = new ArrayList<>();
    private Realm mRealm;

    /**
     * {@link ListFragment}
     */
    @Override
    public int getMenuResourceId() {
        return R.menu.timeline;
    }

    /**
     * {@link MediaBrowserListFragment}
     */
    @Override
    public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {

    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {

    }

    @Override
    void onMediaBrowserChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {

    }

    @Override
    void onMediaBrowserError(@NonNull String parentId) {

    }

    /**
     * {@link SwipeRefreshLayout.OnRefreshListener}
     */
    @Override
    public void onRefresh() {
        if (mSwipeRefreshLayout == null || getActivity() == null || mSongHistoryController == null || mListener == null) {
            return;
        }
        mListener.destroyActionModeIfCan();
        mAllSongHistoryList = mSongHistoryController.getManagedTimeline(mRealm);
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.updateDataSet(getItemList(0, 30));
    }

    /**
     * {@link FastScroller.OnScrollStateChangeListener}
     */
    @Override
    public void onFastScrollerStateChange(boolean scrolling) {
        if (scrolling) {
            hideFab();
        } else {
            showFab();
        }
    }

    /**
     * {@link FlexibleAdapter.OnItemClickListener}
     */
    @Override
    public boolean onItemClick(int position) {
        LogHelper.d(TAG, "onItemClick ");
        SongHistoryItem item = (SongHistoryItem) mAdapter.getItem(position);
        mMediaBrowsable.onMediaItemSelected(item.getSongHistory().getSong().getMediaId());
        return false;
    }

    /**
     * {@link FlexibleAdapter.OnItemLongClickListener}
     */
    @Override
    public void onItemLongClick(int position) {
        mListener.startActionModeByLongClick(position);
    }

    /**
     * {@link FlexibleAdapter.EndlessScrollListener}
     */
    @Override
    public void noMoreLoad(int newItemsSize) {
        LogHelper.d(TAG, "newItemsSize=" + newItemsSize);
        LogHelper.d(TAG, "Total pages loaded=" + mAdapter.getEndlessCurrentPage());
        LogHelper.d(TAG, "Total items loaded=" + mAdapter.getMainItemCount());
    }

    @Override
    public void onLoadMore(int lastPosition, int currentPage) {
        mAdapter.onLoadMoreComplete(getItemList(mAdapter.getMainItemCount() - mAdapter.getHeaderItems().size(), 30), 5000L);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        mSongHistoryController = new SongHistoryController(getContext());

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);

        mRealm = RealmHelper.getRealmInstance();
        mAllSongHistoryList = mSongHistoryController.getManagedTimeline(mRealm);
        mAdapter = new SongAdapter(getItemList(0, 30), this);
        mAdapter.setNotifyChangeOfUnfilteredItems(true)
                .setAnimationOnScrolling(false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(getActivity()));
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
        //mAdapter.showLayoutInfo(savedInstanceState == null);
//        mAdapter.addScrollableFooter();


        // EndlessScrollListener - OnLoadMore (v5.0.0)
        mAdapter//.setLoadingMoreAtStartUp(true) //To call only if the list is empty
                //.setEndlessPageSize(3) //Endless is automatically disabled if newItems < 3
                //.setEndlessTargetCount(15) //Endless is automatically disabled if totalItems >= 15
                //.setEndlessScrollThreshold(1); //Default=1
                .setEndlessScrollListener(this, mProgressItem);
        initializeFabWithStamp();
        notifyFragmentChange();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upward:
                if (mRecyclerView != null) {
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.scrollToPosition(calcGoToTopBufferedPosition(15));
                            mRecyclerView.smoothScrollToPosition(0);
                        }
                    });
                }
                return false;
            default:
                break;
        }
        return false;
    }

    private int calcGoToTopBufferedPosition(int bufferSize) {
        int position = calcCurrentPosition();
        if (position > bufferSize) {
            position = bufferSize;
        }
        return position;
    }

    private int calcCurrentPosition() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) mRecyclerView.getLayoutManager());
        return layoutManager.findFirstVisibleItemPosition();
    }

    public static DateHeaderItem newHeader(SongHistory songHistory) {
        return new DateHeaderItem(songHistory.getRecordedAt());
    }

    public static SongHistoryItem newSimpleItem(SongHistory songHistory, IHeader header) {
        SongHistoryItem item = new SongHistoryItem(songHistory, (DateHeaderItem) header);
        item.setTitle(songHistory.getSong().getTitle());
        return item;
    }

    private List<AbstractFlexibleItem> getItemList(int startPosition, int size) {
        int end = startPosition + size;
        if (end >= mAllSongHistoryList.size()) {
            end = mAllSongHistoryList.size();
        }
        List<AbstractFlexibleItem> headerItemList = new ArrayList<>();
        DateHeaderItem header = mAdapter == null ? null : (DateHeaderItem) Iterables.getLast(mAdapter.getHeaderItems());

        for (int i = startPosition; i < mAllSongHistoryList.size(); i++) {
            if (header == null || !header.isDateOf(mAllSongHistoryList.get(i).getRecordedAt())) {
                if (i >= end) {
                    break;
                }
                header = newHeader(mAllSongHistoryList.get(i));
            }
            headerItemList.add(newSimpleItem(mAllSongHistoryList.get(i), header));
        }
        return headerItemList;
    }

    @Override
    public void onStateChange(TagEditStateObserver.State state) {
        super.onStateChange(state);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemSwipe(final int position, int direction) {
        LogHelper.i(TAG, "onItemSwipe position=" + position +
                " direction=" + (direction == ItemTouchHelper.LEFT ? "LEFT" : "RIGHT"));

        // Option 1 FULL_SWIPE: Direct action no Undo Action
        // Do something based on direction when item has been swiped:
        //   A) update item, set "read" if an email etc.
        //   B) remove the item from the adapter;

        // Option 2 FULL_SWIPE: Delayed action with Undo Action
        // Show action button and start a new Handler:
        //   A) on time out do something based on direction (open dialog with options);

        // Create list for single position (only in onItemSwipe)
        List<Integer> positions = new ArrayList<>(1);
        positions.add(position);
        // Build the message
        IFlexible abstractItem = mAdapter.getItem(position);
        StringBuilder message = new StringBuilder();
        message.append(abstractItem.toString()).append(" ");
        // Experimenting NEW feature
        if (abstractItem.isSelectable()) {
            mAdapter.setRestoreSelectionOnUndo(false);
        }
        // Perform different actions
        // Here, option 2A) is implemented
        if (direction == ItemTouchHelper.LEFT) {
            message.append(getString(R.string.action_archived));

            // Example of UNDO color
            int actionTextColor;
            if (Utils.hasMarshmallow()) {
                actionTextColor = ContextCompat.getColor(getActivity(), R.color.material_color_orange_500);
            } else {
                //noinspection deprecation
                actionTextColor = getResources().getColor(R.color.material_color_orange_500);
            }

            new UndoHelper(mAdapter, this)
                    .withPayload(null) //You can pass any custom object (in this case Boolean is enough)
                    .withAction(UndoHelper.ACTION_UPDATE, new UndoHelper.SimpleActionListener() {
                        @Override
                        public boolean onPreAction() {
                            // Return true to avoid default immediate deletion.
                            // Ask to the user what to do, open a custom dialog. On option chosen,
                            // remove the item from Adapter list as usual.
                            return true;
                        }
                    })
                    .withActionTextColor(actionTextColor)
                    .remove(positions, getActivity().findViewById(R.id.main_view), message,
                            getString(R.string.undo), UndoHelper.UNDO_TIMEOUT);

            //Here, option 1B) is implemented
        } else if (direction == ItemTouchHelper.RIGHT) {
            message.append(getString(R.string.action_deleted));
            mSwipeRefreshLayout.setRefreshing(true);
            new UndoHelper(mAdapter, this)
                    .withPayload(null) //You can pass any custom object (in this case Boolean is enough)
                    .withAction(UndoHelper.ACTION_REMOVE, new UndoHelper.SimpleActionListener() {
                        @Override
                        public void onPostAction() {
                            // Handle ActionMode title
                            if (mAdapter.getSelectedItemCount() == 0) {
                                mListener.destroyActionModeIfCan();
                            } else {
                                mListener.updateContextTitle(mAdapter.getSelectedItemCount());
                            }
                        }
                    })
                    .remove(positions, getActivity().findViewById(R.id.main_view), message,
                            getString(R.string.undo), UndoHelper.UNDO_TIMEOUT);
        }
    }

    @Override
    public void onActionStateChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        LogHelper.i(TAG, "onActionStateChanged actionState=" + actionState);

        mSwipeRefreshLayout.setEnabled(actionState == ItemTouchHelper.ACTION_STATE_IDLE);
    }

    @Override
    public void onUndoConfirmed(int action) {
        LogHelper.i(TAG, "onUndoConfirmed action=" + action);
        if (action == UndoHelper.ACTION_UPDATE) {
            //TODO: Complete click animation on swiped item
//			final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForLayoutPosition(mSwipedPosition);
//			if (holder instanceof ItemTouchHelperCallback.ViewHolderCallback) {
//				final View view = ((ItemTouchHelperCallback.ViewHolderCallback) holder).getFrontView();
//				Animator animator = ObjectAnimator.ofFloat(view, "translationX", view.getTranslationX(), 0);
//				animator.addListener(new SimpleAnimatorListener() {
//					@Override
//					public void onAnimationCancel(Animator animation) {
//						view.setTranslationX(0);
//					}
//				});
//				animator.start();
//			}
        } else if (action == UndoHelper.ACTION_REMOVE) {
            // Custom action is restore deleted items
            mAdapter.restoreDeletedItems();
            // Disable Refreshing
            mSwipeRefreshLayout.setRefreshing(false);
            // Check also selection restoration
            if (mAdapter.isRestoreWithSelection()) {
                mListener.restoreSelection();
            }
        }

    }

    @Override
    public void onDeleteConfirmed(int action) {
        LogHelper.i(TAG, "onDeleteConfirmed action=" + action);
        // Disable Refreshing
        mSwipeRefreshLayout.setRefreshing(false);
        // Removing items from Database. Example:
        for (AbstractFlexibleItem adapterItem : mAdapter.getDeletedItems()) {
            try {
                // NEW! You can take advantage of AutoMap and differentiate logic by viewType using "switch" statement
                switch (adapterItem.getLayoutRes()) {
                    case R.layout.recycler_simple_item:
                        AbstractItem subItem = (AbstractItem) adapterItem;
                        subItem.delete(getActivity());
                        LogHelper.i(TAG, "Confirm removed " + subItem.toString());
                        break;
                }

            } catch (IllegalStateException e) {
//                // AutoMap is disabled, fallback to if-else with "instanceof" statement
//                if (adapterItem instanceof SubItem) {
//                    // SubItem
//                    SubItem subItem = (SubItem) adapterItem;
//                    IExpandable expandable = mAdapter.getExpandableOf(subItem);
//                    DatabaseService.getInstance().removeSubItem(expandable, subItem);
//                    Log.d(TAG, "Confirm removed " + subItem.getTitle());
//                } else if (adapterItem instanceof SimpleItem || adapterItem instanceof ExpandableItem) {
//                    DatabaseService.getInstance().removeItem(adapterItem);
//                    Log.d(TAG, "Confirm removed " + adapterItem);
//                }
//            }
            }
        }
    }
}