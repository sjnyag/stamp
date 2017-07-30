package com.sjn.stamp.ui.fragment.media_list;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.sjn.stamp.R;
import com.sjn.stamp.controller.SongHistoryController;
import com.sjn.stamp.db.RankedArtist;
import com.sjn.stamp.db.RankedSong;
import com.sjn.stamp.db.Tweetable;
import com.sjn.stamp.ui.SongAdapter;
import com.sjn.stamp.ui.custom.RankingSelectLayout;
import com.sjn.stamp.ui.custom.TermSelectLayout;
import com.sjn.stamp.ui.item.RankedArtistItem;
import com.sjn.stamp.ui.item.RankedSongItem;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.RealmHelper;
import com.sjn.stamp.utils.TweetHelper;
import com.sjn.stamp.utils.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import io.realm.Realm;
import lombok.Getter;
import lombok.experimental.Accessors;

public class RankingFragment extends MediaBrowserListFragment {

    private static final String TAG = LogHelper.makeLogTag(RankingFragment.class);

    private TermSelectLayout.Term mTerm = new TermSelectLayout.Term();
    private RankKind mRankKind;
    private SongHistoryController mSongHistoryController;

    private ProgressBar mLoading;
    private CalculateAsyncTask mAsyncTask;

    /**
     * {@link ListFragment}
     */
    @Override
    public int getMenuResourceId() {
        return R.menu.ranking;
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
            String artist = ((RankedArtistItem) item).getArtistName();
            MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                    .setMediaId(MediaIDHelper.createMediaID(null, MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST, artist))
                    .setTitle(MediaIDHelper.unescape(artist))
                    .build();
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
            mMediaBrowsable.onMediaItemSelected(mediaItem);
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
        final View rootView = inflater.inflate(R.layout.fragment_ranking, container, false);
        mRankKind = parseArgRankKind();
        mLoading = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mSongHistoryController = new SongHistoryController(getContext());

        mEmptyView = rootView.findViewById(R.id.empty_view);
        mFastScroller = (FastScroller) rootView.findViewById(R.id.fast_scroller);
        mEmptyTextView = (TextView) rootView.findViewById(R.id.empty_text);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);

        mAdapter = new SongAdapter(mItemList, this);
        mAdapter.setNotifyChangeOfUnfilteredItems(true)
                .setAnimationOnScrolling(false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
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
        initializeFab(R.drawable.ic_twitter, ColorStateList.valueOf(Color.WHITE), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    final RankingSelectLayout termSelectLayout = new RankingSelectLayout(getActivity(), null, mTerm);
                    new MaterialDialog.Builder(getContext())
                            .title("ランキング対象選択")
                            .customView(termSelectLayout, true)
                            .positiveText(R.string.dialog_ok)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    final TermSelectLayout.Term term = termSelectLayout.getTerm();
                                    final int songNum = termSelectLayout.getSongNum();
                                    TweetHelper.tweet(getActivity(), getResources().getString(R.string.tweet_ranking, term.toString(), mRankKind.getRankingTweet(getResources(), mSongHistoryController, term, songNum)));
                                }
                            })
                            .contentColorRes(android.R.color.white)
                            .backgroundColorRes(R.color.material_blue_grey_800)
                            .theme(Theme.DARK)
                            .show();
                }
            }
        });
        if (mIsVisibleToUser) {
            notifyFragmentChange();
        }
        if (mItemList == null || mItemList.isEmpty()) {
            mLoading.setVisibility(View.VISIBLE);
            draw();
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
        mAsyncTask = new CalculateAsyncTask(this, mAdapter, mRankKind, mTerm, mSongHistoryController);
        mAsyncTask.execute();
    }

    public void setTermAndReload(TermSelectLayout.Term term) {
        mTerm = term;
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
        TermSelectLayout.Term mTerm;
        SongHistoryController mSongHistoryController;

        CalculateAsyncTask(RankingFragment fragment, FlexibleAdapter adapter, Callback callback, TermSelectLayout.Term term, SongHistoryController songHistoryController) {
            this.fragment = fragment;
            this.adapter = adapter;
            mCallback = callback;
            mTerm = term;
            mSongHistoryController = songHistoryController;
        }

        interface Callback {
            List<AbstractFlexibleItem> createItemList(Realm realm, TermSelectLayout.Term term, SongHistoryController songHistoryController);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Realm realm = null;
            try {
                realm = RealmHelper.getRealmInstance();
                fragment.mItemList = mCallback.createItemList(realm, mTerm, mSongHistoryController);
                if (fragment.getActivity() == null) {
                    return null;
                }
                fragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    synchronized public void run() {
                        if (!fragment.isAdded()) {
                            return;
                        }
                        adapter.updateDataSet(fragment.mItemList);
                        if (fragment.mLoading != null) {
                            fragment.mLoading.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
            return null;
        }

    }

    @Getter
    @Accessors(prefix = "m")
    enum RankKind implements CalculateAsyncTask.Callback {

        SONG {
            @Override
            public String getRankingTweet(Resources resource, SongHistoryController controller, TermSelectLayout.Term term, int songNum) {
                List<Tweetable> tweetableList = new ArrayList<>();
                Realm realm = RealmHelper.getRealmInstance();
                for (Tweetable tweetable : controller.getRankedSongList(realm, term)) {
                    tweetableList.add(tweetable);
                }
                String tweet = tweet(resource, tweetableList, songNum);
                realm.close();
                return tweet;
            }

            @Override
            public List<AbstractFlexibleItem> createItemList(Realm realm, TermSelectLayout.Term term, SongHistoryController songHistoryController) {
                List<AbstractFlexibleItem> itemList = new ArrayList<>();
                int order = 1;
                for (RankedSong rankedSong : songHistoryController.getRankedSongList(realm, term)) {
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
            public String getRankingTweet(Resources resource, SongHistoryController controller, TermSelectLayout.Term term, int songNum) {
                List<Tweetable> tweetableList = new ArrayList<>();
                Realm realm = RealmHelper.getRealmInstance();
                for (Tweetable tweetable : controller.getRankedArtistList(realm, term)) {
                    tweetableList.add(tweetable);
                }
                String tweet = tweet(resource, tweetableList, songNum);
                realm.close();
                return tweet;
            }

            @Override
            public List<AbstractFlexibleItem> createItemList(Realm realm, TermSelectLayout.Term term, SongHistoryController songHistoryController) {
                List<AbstractFlexibleItem> itemList = new ArrayList<>();
                int order = 1;
                for (RankedArtist rankedArtist : songHistoryController.getRankedArtistList(realm, term)) {
                    itemList.add(newSimpleItem(rankedArtist, order++));
                }
                return itemList;
            }

            private RankedArtistItem newSimpleItem(RankedArtist rankedArtist, int order) {
                return new RankedArtistItem(rankedArtist.mostPlayedSong().getTitle(), rankedArtist.getArtist().getName(), rankedArtist.getArtist().getAlbumArtUri(), rankedArtist.getPlayCount(), order);
            }

        },;

        public abstract String getRankingTweet(Resources resources, SongHistoryController controller, TermSelectLayout.Term term, int songNum);

        public static RankKind of(String value) {
            for (RankKind rankKind : RankKind.values()) {
                if (rankKind.toString().equals(value)) return rankKind;
            }
            return null;
        }

        private static String tweet(Resources resources, List<Tweetable> tweetableList, int songNum) {
            String content = "";
            int order = 1;
            for (Tweetable tweetable : tweetableList) {
                content = content + resources.getString(R.string.tweet_each_rank, order++, tweetable.tweet(resources));
                if (order > songNum) {
                    break;
                }
            }
            return content;
        }
    }

}