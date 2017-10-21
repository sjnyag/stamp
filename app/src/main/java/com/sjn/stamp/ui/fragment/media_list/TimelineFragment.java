package com.sjn.stamp.ui.fragment.media_list;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.res.Resources;
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
import android.support.wearable.view.SimpleAnimatorListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Iterables;
import com.sjn.stamp.R;
import com.sjn.stamp.controller.SongHistoryController;
import com.sjn.stamp.db.SongHistory;
import com.sjn.stamp.ui.DialogFacade;
import com.sjn.stamp.ui.SongAdapter;
import com.sjn.stamp.ui.item.AbstractItem;
import com.sjn.stamp.ui.item.DateHeaderItem;
import com.sjn.stamp.ui.item.SongHistoryItem;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.RealmHelper;
import com.sjn.stamp.utils.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback;
import eu.davidea.flexibleadapter.helpers.UndoHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.utils.Utils;
import eu.davidea.viewholders.FlexibleViewHolder;
import io.realm.Realm;

public class TimelineFragment extends MediaBrowserListFragment implements
        UndoHelper.OnUndoListener, FlexibleAdapter.OnItemSwipeListener {

    private static final String TAG = LogHelper.makeLogTag(TimelineFragment.class);

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

    @Override
    public String emptyMessage() {
        return getString(R.string.empty_message_timeline);
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
        mSwipeRefreshLayout.setRefreshing(false);
        draw();
    }


    /**
     * {@link FlexibleAdapter.OnItemClickListener}
     */
    @Override
    public boolean onItemClick(int position) {
        LogHelper.d(TAG, "onItemClick ");
        AbstractFlexibleItem item = mAdapter.getItem(position);
        if (item instanceof SongHistoryItem) {
            mMediaBrowsable.onMediaItemSelected(MediaIDHelper.extractMusicIDFromMediaID(((SongHistoryItem) item).getMediaId()));
        }
        return false;
    }

    /**
     * {@link FlexibleAdapter.OnItemLongClickListener}
     */
    @Override
    public void onItemLongClick(int position) {
        if (mListener == null) {
            return;
        }
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
        mAdapter.onLoadMoreComplete(createItemList(mAdapter.getMainItemCount() - mAdapter.getHeaderItems().size(), 30), 5000L);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        mSongHistoryController = new SongHistoryController(getContext());
        mRealm = RealmHelper.getRealmInstance();

        mLoading = rootView.findViewById(R.id.progressBar);
        mEmptyView = rootView.findViewById(R.id.empty_view);
        mFastScroller = rootView.findViewById(R.id.fast_scroller);
        mEmptyTextView = rootView.findViewById(R.id.empty_text);

        mSwipeRefreshLayout = rootView.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);

        mAdapter = new SongAdapter(mItemList, this);
        mAdapter.setNotifyChangeOfUnfilteredItems(true)
                .setAnimationOnScrolling(false);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }
        RecyclerView.LayoutManager layoutManager = new SmoothScrollLinearLayoutManager(getActivity());
        if (mListState != null) {
            layoutManager.onRestoreInstanceState(mListState);
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setFastScroller((FastScroller) rootView.findViewById(R.id.fast_scroller),
                ViewHelper.getColorAccent(getActivity()), this);

        mAdapter.setLongPressDragEnabled(false)
                .setHandleDragEnabled(false)
                .setSwipeEnabled(true)
                .setUnlinkAllItemsOnRemoveHeaders(false)
                .setDisplayHeadersAtStartUp(false)
                .setStickyHeaders(true)
                .showAllHeaders();
        mAdapter.addUserLearnedSelection(savedInstanceState == null);
        mAdapter.setEndlessScrollListener(this, mProgressItem);
        initializeFabWithStamp();
        notifyFragmentChange();
        if (mItemList == null || mItemList.isEmpty()) {
            mLoading.setVisibility(View.VISIBLE);
            draw();
        } else {
            mLoading.setVisibility(View.GONE);
        }

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

    synchronized void draw() {
        if (mAllSongHistoryList == null || mAdapter == null) {
            return;
        }
        mAllSongHistoryList = mSongHistoryController.getManagedTimeline(mRealm);
        mItemList = createItemList(0, 30);
        if (mLoading != null) {
            mLoading.setVisibility(View.INVISIBLE);
        }
        mAdapter.updateDataSet(mItemList);
        if (mItemList.isEmpty()) {
            hideFab();
        } else {
            showFab();
        }
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

    public SongHistoryItem newSimpleItem(SongHistory songHistory, IHeader header, Resources resources) {
        SongHistoryItem item = new SongHistoryItem(songHistory, (DateHeaderItem) header, resources, getActivity());
        item.setTitle(songHistory.getSong().getTitle());
        return item;
    }

    private List<AbstractFlexibleItem> createItemList(int startPosition, int size) {
        int end = startPosition + size;
        if (end >= mAllSongHistoryList.size()) {
            end = mAllSongHistoryList.size();
        }
        List<AbstractFlexibleItem> headerItemList = new ArrayList<>();
        DateHeaderItem header = (mAdapter == null || mAdapter.getHeaderItems().isEmpty()) ? null : (DateHeaderItem) Iterables.getLast(mAdapter.getHeaderItems());

        for (int i = startPosition; i < mAllSongHistoryList.size(); i++) {
            if (header == null || !header.isDateOf(mAllSongHistoryList.get(i).getRecordedAt())) {
                if (i >= end) {
                    break;
                }
                header = newHeader(mAllSongHistoryList.get(i));
            }
            headerItemList.add(newSimpleItem(mAllSongHistoryList.get(i), header, getResources()));
        }
        return headerItemList;
    }

    @Override
    public void onItemSwipe(final int position, int direction) {
        LogHelper.i(TAG, "onItemSwipe position=" + position +
                " direction=" + (direction == ItemTouchHelper.LEFT ? "LEFT" : "RIGHT"));

        // Option 1 FULL_SWIPE: Direct action no Undo Action
        // Do something based on direction when item has been swiped:
        //   A) update item, set "read" if an email etc.
        //   B) delete the item from the adapter;

        // Option 2 FULL_SWIPE: Delayed action with Undo Action
        // Show action button and start a new Handler:
        //   A) on time out do something based on direction (open dialog with options);

        // Create list for single position (only in onItemSwipe)
        final List<Integer> positions = new ArrayList<>(1);
        positions.add(position);
        // Build the message
        IFlexible abstractItem = mAdapter.getItem(position);
        final StringBuilder message = new StringBuilder();
        message.append(abstractItem.toString()).append(" ");
        // Experimenting NEW feature
        if (abstractItem.isSelectable()) {
            mAdapter.setRestoreSelectionOnUndo(false);
        }
        // Perform different actions
        // Here, option 2A) is implemented
        if (direction == ItemTouchHelper.RIGHT) {
            AbstractItem subItem = (AbstractItem) abstractItem;
            DialogFacade.createHistoryDeleteDialog(getActivity(), subItem.toString(), new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            switch (which) {
                                case NEGATIVE:
                                    final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForLayoutPosition(position);

                                    if (holder instanceof ItemTouchHelperCallback.ViewHolderCallback) {
                                        final View view = ((ItemTouchHelperCallback.ViewHolderCallback) holder).getFrontView();
                                        Animator animator = ObjectAnimator.ofFloat(view, "translationX", view.getTranslationX(), 0);
                                        animator.addListener(new SimpleAnimatorListener() {
                                            @Override
                                            public void onAnimationCancel(Animator animation) {
                                                view.setTranslationX(0);
                                            }
                                        });
                                        animator.start();

                                        if (holder instanceof FlexibleViewHolder) {
                                            ((FlexibleViewHolder) holder).onActionStateChanged(position, ItemTouchHelper.ACTION_STATE_IDLE);
                                        }
                                    }
                                    return;
                                case POSITIVE:
                                    message.append(getString(R.string.action_deleted));
                                    mSwipeRefreshLayout.setRefreshing(true);
                                    new UndoHelper(mAdapter, TimelineFragment.this)
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
                                    break;
                            }
                        }
                    },
                    new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForLayoutPosition(position);
                            if (holder instanceof ItemTouchHelperCallback.ViewHolderCallback) {
                                final View view = ((ItemTouchHelperCallback.ViewHolderCallback) holder).getFrontView();
                                Animator animator = ObjectAnimator.ofFloat(view, "translationX", view.getTranslationX(), 0);
                                animator.addListener(new SimpleAnimatorListener() {
                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                        view.setTranslationX(0);
                                    }
                                });
                                animator.start();

                                if (holder instanceof FlexibleViewHolder) {
                                    ((FlexibleViewHolder) holder).onActionStateChanged(position, ItemTouchHelper.ACTION_STATE_IDLE);
                                }
                            }
                        }
                    }).show();
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
                    case R.layout.recycler_song_history_item:
                        AbstractItem subItem = (AbstractItem) adapterItem;
                        if (getActivity() != null) {
                            subItem.delete(getActivity());
                        }
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