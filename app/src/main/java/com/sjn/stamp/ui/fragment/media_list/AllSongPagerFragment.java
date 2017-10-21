package com.sjn.stamp.ui.fragment.media_list;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sjn.stamp.R;
import com.sjn.stamp.ui.SongListFactory;
import com.sjn.stamp.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.List;


public class AllSongPagerFragment extends PagerFragment implements PagerFragment.PageFragmentContainer.Creator {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            return null;
        }
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        return rootView;
    }

    @Override
    List<PageFragmentContainer> setUpFragmentContainer() {
        List<PageFragmentContainer> fragmentContainerList = new ArrayList<>();
        fragmentContainerList.add(new PageFragmentContainer(getString(R.string.all_song_tab_most_played), MediaIDHelper.MEDIA_ID_MUSICS_BY_MOST_PLAYED, this));
        fragmentContainerList.add(new PageFragmentContainer(getString(R.string.all_song_tab_artist), MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST, this));
        fragmentContainerList.add(new PageFragmentContainer(getString(R.string.all_song_tab_album), MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM, this));
        fragmentContainerList.add(new PageFragmentContainer(getString(R.string.all_song_tab_playlist), MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST, this));
        fragmentContainerList.add(new PageFragmentContainer(getString(R.string.all_song_tab_all), MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL, this));
        fragmentContainerList.add(new PageFragmentContainer(getString(R.string.all_song_tab_new), MediaIDHelper.MEDIA_ID_MUSICS_BY_NEW, this));
        return fragmentContainerList;
    }

    @Override
    public Fragment create(String fragmentHint) {
        return SongListFactory.INSTANCE.create(fragmentHint);
    }
}