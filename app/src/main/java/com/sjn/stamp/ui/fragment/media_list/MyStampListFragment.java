package com.sjn.stamp.ui.fragment.media_list;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sjn.stamp.R;
import com.sjn.stamp.controller.StampController;
import com.sjn.stamp.ui.DialogFacade;
import com.sjn.stamp.ui.SongAdapter;
import com.sjn.stamp.ui.item.SongItem;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback;
import eu.davidea.flexibleadapter.helpers.UndoHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class MyStampListFragment extends SongListFragment implements
        UndoHelper.OnUndoListener, FlexibleAdapter.OnItemSwipeListener {
    private static final String TAG = LogHelper.INSTANCE.makeLogTag(MyStampListFragment.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogHelper.INSTANCE.d(TAG, "onCreateView START" + getMediaId());
        final View rootView = inflater.inflate(R.layout.fragment_list, container, false);

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
        mAdapter.setFastScroller((FastScroller) rootView.findViewById(R.id.fast_scroller));
        mAdapter.setLongPressDragEnabled(false)
                .setHandleDragEnabled(false)
                .setSwipeEnabled(true)
                .setUnlinkAllItemsOnRemoveHeaders(false)
                .setDisplayHeadersAtStartUp(false)
                .setStickyHeaders(false)
                .showAllHeaders();
        initializeFabWithStamp();
        if (mIsVisibleToUser) {
            notifyFragmentChange();
        }
        draw();
        LogHelper.INSTANCE.d(TAG, "onCreateView END");
        return rootView;
    }


    @Override
    public void onItemSwipe(final int position, int direction) {
        LogHelper.INSTANCE.i(TAG, "onItemSwipe position=" + position +
                " direction=" + (direction == ItemTouchHelper.LEFT ? "LEFT" : "RIGHT"));
        final List<Integer> positions = new ArrayList<>(1);
        positions.add(position);
        IFlexible abstractItem = mAdapter.getItem(position);
        final StringBuilder message = new StringBuilder();
        if (abstractItem.isSelectable()) {
            mAdapter.setRestoreSelectionOnUndo(false);
        }
        if (direction == ItemTouchHelper.RIGHT) {
            SongItem subItem = (SongItem) abstractItem;
            StampController stampController = new StampController(getActivity());
            if (stampController.isCategoryStamp(MediaIDHelper.INSTANCE.extractBrowseCategoryValueFromMediaID(mMediaId), false, subItem.getMediaId())) {
                Toast.makeText(getActivity(), R.string.error_message_stamp_failed, Toast.LENGTH_LONG).show();
                final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForLayoutPosition(position);
                final View view = ((ItemTouchHelperCallback.ViewHolderCallback) holder).getFrontView();
                Animator animator = ObjectAnimator.ofFloat(view, "translationX", view.getTranslationX(), 0);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        view.setTranslationX(0);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                animator.start();
                if (holder instanceof FlexibleViewHolder) {
                    ((FlexibleViewHolder) holder).onActionStateChanged(position, ItemTouchHelper.ACTION_STATE_IDLE);
                }
                return;
            }

            message.append(subItem.getTitle()).append(" ");
            DialogFacade.INSTANCE.createRemoveStampSongDialog(getActivity(), subItem.getTitle(), MediaIDHelper.INSTANCE.extractBrowseCategoryValueFromMediaID(mMediaId), new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            switch (which) {
                                case NEGATIVE:
                                    final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForLayoutPosition(position);
                                    if (holder instanceof ItemTouchHelperCallback.ViewHolderCallback) {
                                        final View view = ((ItemTouchHelperCallback.ViewHolderCallback) holder).getFrontView();
                                        Animator animator = ObjectAnimator.ofFloat(view, "translationX", view.getTranslationX(), 0);
                                        animator.addListener(new Animator.AnimatorListener() {
                                            @Override
                                            public void onAnimationStart(Animator animator) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animator animator) {

                                            }

                                            @Override
                                            public void onAnimationCancel(Animator animation) {
                                                view.setTranslationX(0);
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animator animator) {

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
                                    new UndoHelper(mAdapter, MyStampListFragment.this)
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
                                animator.addListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animator) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animator) {

                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                        view.setTranslationX(0);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animator) {

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
        LogHelper.INSTANCE.i(TAG, "onActionStateChanged actionState=" + actionState);
        mSwipeRefreshLayout.setEnabled(actionState == ItemTouchHelper.ACTION_STATE_IDLE);
    }

    @Override
    public void onActionCanceled(int action) {
        LogHelper.INSTANCE.i(TAG, "onUndoConfirmed action=" + action);
        if (action == UndoHelper.ACTION_UPDATE) {
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
    public void onActionConfirmed(int action, int event) {
        LogHelper.INSTANCE.i(TAG, "onDeleteConfirmed action=" + action);
        mSwipeRefreshLayout.setRefreshing(false);
        for (AbstractFlexibleItem adapterItem : mAdapter.getDeletedItems()) {
            try {
                switch (adapterItem.getLayoutRes()) {
                    case R.layout.recycler_song_item:
                        SongItem subItem = (SongItem) adapterItem;
                        if (getActivity() != null) {
                            new StampController(getActivity()).removeSong(MediaIDHelper.INSTANCE.extractBrowseCategoryValueFromMediaID(mMediaId), false, subItem.getMediaId());
                        }
                        LogHelper.INSTANCE.i(TAG, "Confirm removed " + subItem.toString());
                        break;
                }

            } catch (IllegalStateException ignored) {
            }
        }
    }

}
