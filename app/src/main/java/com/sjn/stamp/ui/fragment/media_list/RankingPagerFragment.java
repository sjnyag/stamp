package com.sjn.stamp.ui.fragment.media_list;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.sjn.stamp.MusicService;
import com.sjn.stamp.R;
import com.sjn.stamp.ui.custom.PeriodSelectLayout;
import com.sjn.stamp.ui.item.RankedArtistItem;
import com.sjn.stamp.ui.item.RankedSongItem;
import com.sjn.stamp.utils.AnalyticsHelper;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.QueueHelper;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public class RankingPagerFragment extends PagerFragment implements PagerFragment.PageFragmentContainer.Creator {

    private PeriodSelectLayout.Period mPeriod;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPeriod = PeriodSelectLayout.Period.Companion.latestWeek();
        setHasOptionsMenu(true);
        initializeFab(R.drawable.ic_play_arrow, ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.bt_accent)), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RankingFragment fragment = null;
                for (PageFragmentContainer fragmentContainer : mFragments) {
                    if (fragmentContainer.mFragment instanceof RankingFragment && ((FabFragment) fragmentContainer.mFragment).isVisibleToUser()) {
                        fragment = (RankingFragment) fragmentContainer.mFragment;
                    }
                }
                if (fragment == null) {
                    return;
                }
                Bundle bundle = new Bundle();

                List<MediaMetadataCompat> trackList = new ArrayList<>();
                for (AbstractFlexibleItem item : fragment.mItemList) {
                    if (item instanceof RankedSongItem) {
                        trackList.add(((RankedSongItem) item).getTrack());
                    } else if (item instanceof RankedArtistItem) {
                        trackList.add(((RankedArtistItem) item).getTrack());
                    }
                }
                if (trackList.isEmpty()) {
                    return;
                }
                bundle.putParcelable(MusicService.CUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_QUEUE, QueueHelper.INSTANCE.createQueue(trackList, MediaIDHelper.MEDIA_ID_MUSICS_BY_RANKING));
                bundle.putString(MusicService.CUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_TITLE, mPeriod.toString(getResources()));
                bundle.putString(MusicService.CUSTOM_ACTION_SET_QUEUE_BUNDLE_MEDIA_ID, MediaIDHelper.INSTANCE.createMediaID(trackList.get(0).getDescription().getMediaId(), MediaIDHelper.MEDIA_ID_MUSICS_BY_RANKING));
                fragment.mMediaBrowsable.sendCustomAction(MusicService.CUSTOM_ACTION_SET_QUEUE, bundle, null);
            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ranking, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.period:
                if (getActivity() != null) {
                    final PeriodSelectLayout periodSelectLayout = new PeriodSelectLayout(getActivity(), mPeriod);
                    new MaterialDialog.Builder(getContext())
                            .title(R.string.dialog_period_select)
                            .customView(periodSelectLayout, true)
                            .positiveText(R.string.dialog_ok)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    mPeriod = periodSelectLayout.getPeriod();
                                    if (mAdapter == null) {
                                        return;
                                    }
                                    setTitle(periodSelectLayout.getPeriod().toString(getResources()));
                                    AnalyticsHelper.INSTANCE.trackRankingTerm(getContext(), periodSelectLayout.getPeriod().from(), periodSelectLayout.getPeriod().to());
                                    for (int i = 0; i < mAdapter.getCount(); i++) {
                                        Fragment fragment = mAdapter.getItem(i);
                                        if (fragment != null && fragment instanceof RankingFragment) {
                                            ((RankingFragment) fragment).setPeriodAndReload(mPeriod);
                                        }
                                    }
                                }
                            })
                            .contentColorRes(android.R.color.white)
                            .backgroundColorRes(R.color.material_blue_grey_800)
                            .theme(Theme.DARK)
                            .show();
                }
                return false;
            default:
                break;
        }
        return false;
    }

    @Override
    List<PageFragmentContainer> setUpFragmentContainer() {
        List<PageFragmentContainer> fragmentContainerList = new ArrayList<>();
        fragmentContainerList.add(new PageFragmentContainer(getString(R.string.ranking_tab_my_songs), RankingFragment.RankKind.SONG.toString(), this));
        fragmentContainerList.add(new PageFragmentContainer(getString(R.string.ranking_tab_my_artists), RankingFragment.RankKind.ARTIST.toString(), this));
        return fragmentContainerList;
    }

    @Override
    public Fragment create(String fragmentHint) {
        Fragment fragment = new RankingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PAGER_KIND_KEY, fragmentHint);
        fragment.setArguments(bundle);
        return fragment;
    }

}