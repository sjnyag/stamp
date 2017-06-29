package com.sjn.stamp.ui.fragment.media_list;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
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
import com.sjn.stamp.ui.item.SongItem;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.utils.LogHelper;
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
        if (item instanceof SongItem) {
            mMediaBrowsable.onMediaItemSelected(((SongItem) item).getMediaItem().getMediaId());
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
            public void updateItemList(TermSelectLayout.Term term, SongHistoryController songHistoryController, Realm realm, List<AbstractFlexibleItem> itemList) {
                itemList.clear();
                List<RankedSong> list = songHistoryController.getRankedSongList(realm, term);
                for (RankedSong rankedSong : list) {
                    itemList.add(newSimpleItem(rankedSong));
                }
            }

            private SongItem newSimpleItem(RankedSong rankedSong) {
                MediaDescriptionCompat copy = new MediaDescriptionCompat.Builder()
                        .setMediaId(rankedSong.getSong().getMediaId())
                        .setTitle(rankedSong.getSong().getTitle())
                        .setSubtitle(rankedSong.getSong().getArtist())
                        .setIconUri(Uri.parse(rankedSong.getSong().getAlbumArtUri()))
                        .build();
                MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(copy, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
                SongItem item = new SongItem(mediaItem, null);
                item.setTitle(rankedSong.getSong().getTitle());
                return item;
            }

        },

        ARTIST {
            @Override
            public List<Tweetable> getRankingTweet(SongHistoryController controller, Realm realm, TermSelectLayout.Term term) {
                List<Tweetable> tweetableList = new ArrayList<>();
                for (Tweetable tweetable : controller.getRankedArtistList(term)) {
                    tweetableList.add(tweetable);
                }
                return tweetableList;
            }

            @Override
            public void updateItemList(TermSelectLayout.Term term, SongHistoryController songHistoryController, Realm realm, List<AbstractFlexibleItem> itemList) {
                itemList.clear();
                List<RankedArtist> list = songHistoryController.getRankedArtistList(term);
                for (RankedArtist rankedArtist : list) {
                    //itemList.add(newSimpleItem(rankedArtist));
                }
            }

            private SongItem newSimpleItem(RankedArtist rankedArtist) {
                return null;
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

        public abstract void updateItemList(TermSelectLayout.Term term, SongHistoryController songHistoryController, Realm realm, List<AbstractFlexibleItem> itemList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mRankKind = parseArgRankKind();
        final View rootView = inflater.inflate(R.layout.fragment_ranking, container, false);
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
        mAdapter.addScrollableFooter();


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
        mRankKind.updateItemList(mTerm, mSongHistoryController, mRealm, mItemList);
        for (AbstractFlexibleItem item : mItemList) {
            mAdapter.addItem(item);
        }
        mAdapter.notifyDataSetChanged();
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