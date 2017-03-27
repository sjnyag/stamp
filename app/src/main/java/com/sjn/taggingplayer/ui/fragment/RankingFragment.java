package com.sjn.taggingplayer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.controller.SongHistoryController;
import com.sjn.taggingplayer.db.RankedArtist;
import com.sjn.taggingplayer.db.RankedSong;
import com.sjn.taggingplayer.db.Tweetable;
import com.sjn.taggingplayer.ui.custom.RankingSelectLayout;
import com.sjn.taggingplayer.ui.custom.TermSelectLayout;
import com.sjn.taggingplayer.ui.holder.RankedArtistItemViewHolder;
import com.sjn.taggingplayer.ui.holder.RankedSongItemViewHolder;
import com.sjn.taggingplayer.utils.TweetHelper;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

public class RankingFragment extends Fragment {

    private TermSelectLayout.Term mTerm = new TermSelectLayout.Term();
    private RankKind mRankKind;
    private ListView mListView;
    private ArrayAdapter mAdapter;

    @Getter
    @Accessors(prefix = "m")
    @AllArgsConstructor
    enum RankKind {

        SONG {
            @Override
            public List<Tweetable> getRankingTweet(@NonNull Context context, TermSelectLayout.Term term) {
                List<Tweetable> tweetableList = new ArrayList<>();
                for (Tweetable tweetable : new SongHistoryController(context).getRankedSongList()) {
                    tweetableList.add(tweetable);
                }
                return tweetableList;
            }

            @Override
            public ArrayAdapter getAdapter(@NonNull Context context, TermSelectLayout.Term term) {
                return new SongRankingAdapter(context, new SongHistoryController(context).getRankedSongList());
            }
        },

        ARTIST {
            @Override
            public List<Tweetable> getRankingTweet(@NonNull Context context, TermSelectLayout.Term term) {
                List<Tweetable> tweetableList = new ArrayList<>();
                for (Tweetable tweetable : new SongHistoryController(context).getRankedArtistList()) {
                    tweetableList.add(tweetable);
                }
                return tweetableList;
            }

            @Override
            public ArrayAdapter getAdapter(@NonNull Context context, TermSelectLayout.Term term) {
                return new ArtistRankingAdapter(context, new SongHistoryController(context).getRankedArtistList());
            }
        },;

        public abstract List<Tweetable> getRankingTweet(@NonNull Context context, TermSelectLayout.Term term);

        public abstract ArrayAdapter getAdapter(@NonNull Context context, TermSelectLayout.Term term);

        public static RankKind of(String value) {
            for (RankKind rankKind : RankKind.values()) {
                if (rankKind.toString().equals(value)) return rankKind;
            }
            return null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_ranking, container, false);

        mListView = (ListView) rootView.findViewById(R.id.result_list);

        mRankKind = parseArgRankKind();
        rootView.findViewById(R.id.tweetButton).setOnClickListener(new View.OnClickListener() {
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
                                    TweetHelper.tweet(getActivity(), getResources().getString(R.string.tweet_ranking, term.toString(), tweet(mRankKind.getRankingTweet(getContext(), term), songNum)));
                                }
                            })
                            .contentColorRes(android.R.color.white)
                            .backgroundColorRes(R.color.material_blue_grey_800)
                            .theme(Theme.DARK)
                            .show();
                }
            }
        });
        drawRanking();
        return rootView;
    }

    public void setTermAndReload(TermSelectLayout.Term term) {
        mTerm = term;
        drawRanking();
    }

    private void drawRanking() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter = mRankKind.getAdapter(getContext(), mTerm);
                mListView.setAdapter(mAdapter);
            }
        });
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

    private static class SongRankingAdapter extends ArrayAdapter<RankedSong> {

        SongRankingAdapter(Context context, List<RankedSong> rankedSongList) {
            super(context, R.layout.list_item_ranked_song, rankedSongList);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            RankedSong item = getItem(position);
            return RankedSongItemViewHolder.setupView((Activity) getContext(), convertView, parent, item, position);
        }
    }

    private static class ArtistRankingAdapter extends ArrayAdapter<RankedArtist> {

        ArtistRankingAdapter(Context context, List<RankedArtist> rankedArtistList) {
            super(context, R.layout.list_item_ranked_song, rankedArtistList);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            RankedArtist item = getItem(position);
            return RankedArtistItemViewHolder.setupView((Activity) getContext(), convertView, parent, item, position);
        }
    }
}