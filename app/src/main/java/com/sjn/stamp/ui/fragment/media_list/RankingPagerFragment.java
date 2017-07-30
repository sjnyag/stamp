package com.sjn.stamp.ui.fragment.media_list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.sjn.stamp.R;
import com.sjn.stamp.ui.custom.TermSelectLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RankingPagerFragment extends PagerFragment implements PagerFragment.PageFragmentContainer.Creator {

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