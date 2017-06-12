package com.sjn.taggingplayer.ui.fragment.media_list;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.ui.custom.TermSelectLayout;

import java.util.Locale;


public class RankingPagerFragment extends PagerFragment {

    private TermSelectLayout.Term mTerm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mTerm = new TermSelectLayout.Term();
        updateTitleByTerm();
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.ranking, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.term:
                if (getActivity() != null) {
                    final TermSelectLayout termSelectLayout = new TermSelectLayout(getActivity(), null, mTerm);
                    new MaterialDialog.Builder(getContext())
                            .title(R.string.dialog_term_select)
                            .customView(termSelectLayout, true)
                            .positiveText(R.string.dialog_ok)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    mTerm = termSelectLayout.getTerm();
                                    updateTitleByTerm();
                                    if (mAdapter == null) {
                                        return;
                                    }
                                    for (int i = 0; i < mAdapter.getCount(); i++) {
                                        Fragment fragment = mAdapter.getItem(i);
                                        if (fragment != null && fragment instanceof RankingFragment) {
                                            ((RankingFragment) fragment).setTermAndReload(mTerm);
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

    private void updateTitleByTerm() {
        setTitle(String.format(Locale.JAPANESE, "%s(%s)", getResources().getString(R.string.drawer_ranking), mTerm.toString()));
    }

    @Override
    protected void setupViewPager(ViewPager viewPager) {
        Resources resources = getResources();
        mAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mAdapter.addFragment(createRankingFragment(RankingFragment.RankKind.SONG), resources.getString(R.string.ranking_tab_my_songs));
        mAdapter.addFragment(createRankingFragment(RankingFragment.RankKind.ARTIST), resources.getString(R.string.ranking_tab_my_artists));
        viewPager.setAdapter(mAdapter);
    }

    private Fragment createRankingFragment(RankingFragment.RankKind rankKind) {
        Fragment fragment = new RankingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PAGER_KIND_KEY, rankKind.toString());
        fragment.setArguments(bundle);
        return fragment;
    }

}