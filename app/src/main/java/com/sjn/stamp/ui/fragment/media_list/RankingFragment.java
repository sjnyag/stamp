package com.sjn.stamp.ui.fragment.media_list;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sjn.stamp.MusicService;
import com.sjn.stamp.R;
import com.sjn.stamp.controller.SongHistoryController;
import com.sjn.stamp.db.RankedArtist;
import com.sjn.stamp.db.RankedSong;
import com.sjn.stamp.db.Shareable;
import com.sjn.stamp.ui.SongAdapter;
import com.sjn.stamp.ui.custom.PeriodSelectLayout;
import com.sjn.stamp.ui.item.RankedArtistItem;
import com.sjn.stamp.ui.item.RankedSongItem;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.MediaItemHelper;
import com.sjn.stamp.utils.QueueHelper;
import com.sjn.stamp.utils.RealmHelper;
import com.sjn.stamp.utils.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import io.realm.Realm;

import static com.sjn.stamp.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_RANKING;

public class RankingFragment extends MediaBrowserListFragment {

    private static final String TAG = LogHelper.makeLogTag(RankingFragment.class);

    private PeriodSelectLayout.Period mPeriod = PeriodSelectLayout.Period.latestWeek();
    private RankKind mRankKind;
    private SongHistoryController mSongHistoryController;
    private CalculateAsyncTask mAsyncTask;
    private ProgressDialog mProgressDialog;

    /**
     * {@link ListFragment}
     */
    @Override
    public int getMenuResourceId() {
        return R.menu.ranking;
    }

    @Override
    public String emptyMessage() {
        return getString(R.string.empty_message_ranking);
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
        if (mRankKind == null || mSwipeRefreshLayout == null || getActivity() == null || mSongHistoryController == null || mListener == null) {
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
        if (item instanceof RankedSongItem) {
            mMediaBrowsable.onMediaItemSelected(((RankedSongItem) item).getMediaId());
        } else if (item instanceof RankedArtistItem) {
            mMediaBrowsable.onMediaItemSelected(MediaItemHelper.createArtistMediaItem(
                    ((RankedArtistItem) item).getArtistName()
            ));
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
    }

    @Override
    public void onLoadMore(int lastPosition, int currentPage) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(false);
        final View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        mRankKind = parseArgRankKind();
        mLoading = rootView.findViewById(R.id.progressBar);
        mSongHistoryController = new SongHistoryController(getContext());

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
        //mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter.setFastScroller((FastScroller) rootView.findViewById(R.id.fast_scroller),
                ViewHelper.getColorAccent(getActivity()), this);

        mAdapter.setLongPressDragEnabled(false)
                .setHandleDragEnabled(false)
                .setSwipeEnabled(false)
                .setUnlinkAllItemsOnRemoveHeaders(false)
                .setDisplayHeadersAtStartUp(false)
                .setStickyHeaders(false)
                .showAllHeaders();
        mAdapter.addUserLearnedSelection(savedInstanceState == null);
        //mAdapter.addScrollableHeaderWithDelay(new DateHeaderItem(TimeHelper.getJapanNow().toDate()), 900L, false);
        //mAdapter.showLayoutInfo(savedInstanceState == null);
        //mAdapter.addScrollableFooter();


        // EndlessScrollListener - OnLoadMore (v5.0.0)
        //mAdapter//.setLoadingMoreAtStartUp(true) //To call only if the list is empty
        //.setEndlessPageSize(3) //Endless is automatically disabled if newItems < 3
        //.setEndlessTargetCount(15) //Endless is automatically disabled if totalItems >= 15
        //.setEndlessScrollThreshold(1); //Default=1
        //.setEndlessScrollListener(this, mProgressItem);
//        initializeFab(R.drawable.ic_share, ColorStateList.valueOf(Color.WHITE), new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (getActivity() != null) {
//                    final RankingSelectLayout periodSelectLayout = new RankingSelectLayout(getActivity(), null, mPeriod);
//                    new MaterialDialog.Builder(getContext())
//                            .title(getString(R.string.dialog_ranking_target))
//                            .customView(periodSelectLayout, true)
//                            .positiveText(R.string.dialog_ok)
//                            .onPositive(new MaterialDialog.SingleButtonCallback() {
//                                @Override
//                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                                    final PeriodSelectLayout.Period period = periodSelectLayout.getPeriod();
//                                    final int songNum = periodSelectLayout.getSongNum();
//                                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
//                                    progressDialog.setMessage(getString(R.string.message_processing));
//                                    progressDialog.show();
//                                    new Thread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            ShareHelper.share(getActivity(), getResources().getString(R.string.share_ranking, periodSelectLayout.getPeriod().toString(getResources()), mRankKind.getRankingShareMessage(getResources(), mSongHistoryController, period, songNum)));
//                                            progressDialog.dismiss();
//                                        }
//                                    }).start();
//                                }
//                            })
//                            .contentColorRes(android.R.color.white)
//                            .backgroundColorRes(R.color.material_blue_grey_800)
//                            .theme(Theme.DARK)
//                            .show();
//                }
//            }
//        });
        initializeFab(R.drawable.ic_play_arrow, ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.bt_accent)), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null && mIsVisibleToUser) {
                    Bundle bundle = new Bundle();

                    List<MediaMetadataCompat> trackList = new ArrayList<>();
                    for (AbstractFlexibleItem item : mItemList) {
                        if (item instanceof RankedSongItem) {
                            trackList.add(((RankedSongItem) item).getTrack());
                        } else if (item instanceof RankedArtistItem) {
                            trackList.add(((RankedArtistItem) item).getTrack());
                        }
                    }
                    if (trackList.isEmpty()) {
                        return;
                    }
                    bundle.putParcelable(MusicService.Companion.getCUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_QUEUE(), QueueHelper.createQueue(trackList, MEDIA_ID_MUSICS_BY_RANKING));
                    bundle.putString(MusicService.Companion.getCUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_TITLE(), mPeriod.toString(getResources()));
                    bundle.putString(MusicService.Companion.getCUSTOM_ACTION_SET_QUEUE_BUNDLE_MEDIA_ID(), MediaIDHelper.createMediaID(trackList.get(0).getDescription().getMediaId(), MEDIA_ID_MUSICS_BY_RANKING));
                    mMediaBrowsable.sendCustomAction(MusicService.Companion.getCUSTOM_ACTION_SET_QUEUE(), bundle, null);
                }
            }
        });
        if (mIsVisibleToUser) {
            notifyFragmentChange();
        }
        if (mItemList == null || mItemList.isEmpty()) {
            mLoading.setVisibility(View.VISIBLE);
            draw();
        } else {
            mLoading.setVisibility(View.GONE);
        }
        return rootView;
    }

    synchronized void draw() {
        if (mRankKind == null) {
            return;
        }
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        mAsyncTask = new CalculateAsyncTask(this, mAdapter, mRankKind, mPeriod, mSongHistoryController);
        mAsyncTask.execute();
    }

    public void setPeriodAndReload(PeriodSelectLayout.Period period) {
        mPeriod = period;
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.message_processing));
        mProgressDialog.show();
        draw();
    }

    private RankKind parseArgRankKind() {
        Bundle bundle = getArguments();
        return RankKind.of(bundle.getString(PagerFragment.PAGER_KIND_KEY));
    }

    private static class CalculateAsyncTask extends AsyncTask<Void, Void, Void> {

        RankingFragment fragment;
        FlexibleAdapter adapter;
        Callback mCallback;
        PeriodSelectLayout.Period mPeriod;
        SongHistoryController mSongHistoryController;

        CalculateAsyncTask(RankingFragment fragment, FlexibleAdapter adapter, Callback callback, PeriodSelectLayout.Period period, SongHistoryController songHistoryController) {
            this.fragment = fragment;
            this.adapter = adapter;
            mCallback = callback;
            mPeriod = period;
            mSongHistoryController = songHistoryController;
        }

        interface Callback {
            List<AbstractFlexibleItem> createItemList(Realm realm, PeriodSelectLayout.Period period, SongHistoryController songHistoryController);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Realm realm = null;
            try {
                realm = RealmHelper.getRealmInstance();
                fragment.mItemList = mCallback.createItemList(realm, mPeriod, mSongHistoryController);
                if (fragment.getActivity() == null) {
                    return null;
                }
                fragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    synchronized public void run() {
                        if (!fragment.isAdded()) {
                            return;
                        }
                        if (fragment.mLoading != null) {
                            fragment.mLoading.setVisibility(View.INVISIBLE);
                        }
                        adapter.updateDataSet(fragment.mItemList);
                    }
                });
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
            if (fragment.mProgressDialog != null) {
                fragment.mProgressDialog.dismiss();
            }
            return null;
        }

    }

    enum RankKind implements CalculateAsyncTask.Callback {

        SONG {
            @Override
            public String getRankingShareMessage(Resources resource, SongHistoryController controller, PeriodSelectLayout.Period period, int songNum) {
                List<Shareable> shareableList = new ArrayList<>();
                Realm realm = RealmHelper.getRealmInstance();
                for (Shareable shareable : controller.getRankedSongList(realm, period)) {
                    shareableList.add(shareable);
                }
                String shareMessage = createShareMessage(resource, shareableList, songNum);
                realm.close();
                return shareMessage;
            }

            @Override
            public List<AbstractFlexibleItem> createItemList(Realm realm, PeriodSelectLayout.Period period, SongHistoryController songHistoryController) {
                List<AbstractFlexibleItem> itemList = new ArrayList<>();
                int order = 1;
                for (RankedSong rankedSong : songHistoryController.getRankedSongList(realm, period)) {
                    itemList.add(newSimpleItem(rankedSong, order++));
                }
                return itemList;
            }

            private RankedSongItem newSimpleItem(RankedSong rankedSong, int order) {
                return new RankedSongItem(rankedSong.getSong().buildMediaMetadataCompat(), rankedSong.getPlayCount(), order);
            }
        },

        ARTIST {
            @Override
            public String getRankingShareMessage(Resources resource, SongHistoryController controller, PeriodSelectLayout.Period period, int songNum) {
                List<Shareable> shareableList = new ArrayList<>();
                Realm realm = RealmHelper.getRealmInstance();
                for (Shareable shareable : controller.getRankedArtistList(realm, period)) {
                    shareableList.add(shareable);
                }
                String shareMessage = createShareMessage(resource, shareableList, songNum);
                realm.close();
                return shareMessage;
            }

            @Override
            public List<AbstractFlexibleItem> createItemList(Realm realm, PeriodSelectLayout.Period period, SongHistoryController songHistoryController) {
                List<AbstractFlexibleItem> itemList = new ArrayList<>();
                int order = 1;
                for (RankedArtist rankedArtist : songHistoryController.getRankedArtistList(realm, period)) {
                    itemList.add(newSimpleItem(rankedArtist, order++));
                }
                return itemList;
            }

            private RankedArtistItem newSimpleItem(RankedArtist rankedArtist, int order) {
                return new RankedArtistItem(rankedArtist.mostPlayedSong().buildMediaMetadataCompat(), rankedArtist.getArtist().getName(), rankedArtist.getArtist().getAlbumArtUri(), rankedArtist.getPlayCount(), order);
            }

        },;

        public abstract String getRankingShareMessage(Resources resources, SongHistoryController controller, PeriodSelectLayout.Period period, int songNum);

        public static RankKind of(String value) {
            for (RankKind rankKind : RankKind.values()) {
                if (rankKind.toString().equals(value)) return rankKind;
            }
            return null;
        }

        private static String createShareMessage(Resources resources, List<Shareable> shareableList, int songNum) {
            String content = "";
            int order = 1;
            for (Shareable shareable : shareableList) {
                content = content + resources.getString(R.string.share_each_rank, order++, shareable.share(resources));
                if (order > songNum) {
                    break;
                }
            }
            return content;
        }
    }

}