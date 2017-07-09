package com.sjn.stamp.ui.fragment.media_list;

import android.content.res.ColorStateList;
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
import android.util.Log;
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
import com.sjn.stamp.db.SongHistory;
import com.sjn.stamp.db.Tweetable;
import com.sjn.stamp.ui.SongAdapter;
import com.sjn.stamp.ui.custom.RankingSelectLayout;
import com.sjn.stamp.ui.custom.TermSelectLayout;
import com.sjn.stamp.ui.item.RankedArtistItem;
import com.sjn.stamp.ui.item.RankedSongItem;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
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
    private Realm mRealm;

    private ProgressBar mLoading;

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
        draw();
        mSwipeRefreshLayout.setRefreshing(false);
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
        mListener.startActionModeByLongClick(position);
    }

    /**
     * {@link FlexibleAdapter.EndlessScrollListener}
     */
    @Override
    public void noMoreLoad(int newItemsSize) {
        Log.d(TAG, "newItemsSize=" + newItemsSize);
        Log.d(TAG, "Total pages loaded=" + mAdapter.getEndlessCurrentPage());
        Log.d(TAG, "Total items loaded=" + mAdapter.getMainItemCount());
    }

    @Override
    public void onLoadMore(int lastPosition, int currentPage) {

    }

    @Getter
    @Accessors(prefix = "m")
    enum RankKind {

        SONG {
            @Override
            public List<Tweetable> getRankingTweet(SongHistoryController controller, Realm realm, TermSelectLayout.Term term) {
                List<Tweetable> tweetableList = new ArrayList<>();
                for (Tweetable tweetable : controller.getRankedSongList(realm, term)) {
                    tweetableList.add(tweetable);
                }
                return tweetableList;
            }

            @Override
            public void updateItemList(RankingFragment fragment, final FlexibleAdapter adapter, final TermSelectLayout.Term term, final SongHistoryController songHistoryController, Realm realm, final List<AbstractFlexibleItem> itemList) {

                new CalculateAsyncTask(fragment, adapter) {
                    @Override
                    List<AbstractFlexibleItem> createItemList(Realm realm) {
                        itemList.clear();
                        int order = 1;
                        for (RankedSong rankedSong : songHistoryController.getRankedSongList(realm, term)) {
                            itemList.add(newSimpleItem(rankedSong, order++));
                        }
                        return itemList;
                    }
                }.execute();
            }

            private RankedSongItem newSimpleItem(RankedSong rankedSong, int order) {
                return new RankedSongItem(rankedSong.getSong().buildMediaMetadataCompat(), rankedSong.getPlayCount(), order);
            }
        },

        ARTIST {
            @Override
            public List<Tweetable> getRankingTweet(SongHistoryController controller, Realm realm, TermSelectLayout.Term term) {
                List<Tweetable> tweetableList = new ArrayList<>();
                for (Tweetable tweetable : controller.getRankedArtistList(realm, term)) {
                    tweetableList.add(tweetable);
                }
                return tweetableList;
            }

            @Override
            public void updateItemList(RankingFragment fragment, final FlexibleAdapter adapter, final TermSelectLayout.Term term, final SongHistoryController songHistoryController, Realm realm, final List<AbstractFlexibleItem> itemList) {
                new CalculateAsyncTask(fragment, adapter) {
                    @Override
                    List<AbstractFlexibleItem> createItemList(Realm realm) {
                        itemList.clear();
                        int order = 1;
                        for (RankedArtist rankedArtist : songHistoryController.getRankedArtistList(realm, term)) {
                            itemList.add(newSimpleItem(rankedArtist, order++));
                        }
                        return itemList;
                    }
                }.execute();
            }

            private RankedArtistItem newSimpleItem(RankedArtist rankedArtist, int order) {
                return new RankedArtistItem(rankedArtist.mostPlayedSong().getTitle(), rankedArtist.getArtist().getName(), rankedArtist.getArtist().getAlbumArtUri(), rankedArtist.getPlayCount(), order);
            }

        },;

        protected List<SongHistory> mAllSongHistoryList = new ArrayList<>();

        public abstract List<Tweetable> getRankingTweet(SongHistoryController controller, Realm realm, TermSelectLayout.Term term);

        public static RankKind of(String value) {
            for (RankKind rankKind : RankKind.values()) {
                if (rankKind.toString().equals(value)) return rankKind;
            }
            return null;
        }

        abstract class CalculateAsyncTask extends AsyncTask<Void, Void, Void> {

            RankingFragment fragment;
            FlexibleAdapter adapter;

            CalculateAsyncTask(RankingFragment fragment, FlexibleAdapter adapter) {
                this.fragment = fragment;
                this.adapter = adapter;
            }

            abstract List<AbstractFlexibleItem> createItemList(Realm realm);

            @Override
            protected Void doInBackground(Void... params) {
                Realm realm = null;
                try {
                    realm = RealmHelper.getRealmInstance();
                    final List<AbstractFlexibleItem> list = createItemList(realm);
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
                            for (AbstractFlexibleItem item : list) {
                                adapter.addItem(item);
                            }
                            adapter.notifyDataSetChanged();
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

        public abstract void updateItemList(RankingFragment fragment, FlexibleAdapter adapter, TermSelectLayout.Term term, SongHistoryController songHistoryController, Realm realm, List<AbstractFlexibleItem> itemList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mRankKind = parseArgRankKind();
        final View rootView = inflater.inflate(R.layout.fragment_ranking, container, false);
        mLoading = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mSongHistoryController = new SongHistoryController(getContext());

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
        mEmptyView = rootView.findViewById(R.id.empty_view);
        mFastScroller = (FastScroller) rootView.findViewById(R.id.fast_scroller);
        mEmptyTextView = (TextView) rootView.findViewById(R.id.empty_text);

        mRealm = RealmHelper.getRealmInstance();
        mAdapter = new SongAdapter(new ArrayList<AbstractFlexibleItem>(), this);
        mAdapter.setNotifyChangeOfUnfilteredItems(true)
                .setAnimationOnScrolling(false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(getActivity()));
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
                                    TweetHelper.tweet(getActivity(), getResources().getString(R.string.tweet_ranking, term.toString(), tweet(mRankKind.getRankingTweet(mSongHistoryController, mRealm, term), songNum)));
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
        draw();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
    }

    public void draw() {
        if (mRankKind == null) {
            return;
        }
        mAdapter.clear();

        if (mLoading != null && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                synchronized public void run() {
                    mLoading.setVisibility(View.VISIBLE);
                }
            });
        }
        mRankKind.updateItemList(this, mAdapter, mTerm, mSongHistoryController, mRealm, mItemList);
    }

    public void setTermAndReload(TermSelectLayout.Term term) {
        mTerm = term;
        draw();
    }

    private RankKind parseArgRankKind() {
        Bundle bundle = getArguments();
        return RankKind.of(bundle.getString(PagerFragment.PAGER_KIND_KEY));
    }

    public String tweet(List<Tweetable> tweetableList, int songNum) {
        String content = "";
        int order = 1;
        for (Tweetable tweetable : tweetableList) {
            content = content + getResources().getString(R.string.tweet_each_rank, order++, tweetable.tweet(getResources()));
            if (order > songNum) {
                break;
            }
        }
        return content;
    }

    @Override
    public void onStateChange(StampEditStateObserver.State state) {
        super.onStateChange(state);
        mAdapter.notifyDataSetChanged();
    }
}