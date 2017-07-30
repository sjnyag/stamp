package com.sjn.stamp.ui.fragment.media_list;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sjn.stamp.R;
import com.sjn.stamp.utils.MediaIDHelper;


public class AllSongPagerFragment extends PagerFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            return rootView;
        }
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        return rootView;
    }

    @Override
    protected void setupViewPager(ViewPager viewPager) {
        mAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mAdapter.addFragment(createSongListFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_SONG), getString(R.string.all_song_tab_top_song));
        mAdapter.addFragment(createSongListFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST), getString(R.string.all_song_tab_artist));
        mAdapter.addFragment(createSongListFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM), getString(R.string.all_song_tab_album));
        mAdapter.addFragment(createSongListFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST), getString(R.string.all_song_tab_playlist));
        mAdapter.addFragment(createSongListFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL), getString(R.string.all_song_tab_all));
        mAdapter.addFragment(createSongListFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_NEW), getString(R.string.all_song_tab_new));
        viewPager.setAdapter(mAdapter);
    }

    private Fragment createSongListFragment(String mediaId) {
        SongListFragment songListFragment = new SongListFragment();
        songListFragment.setMediaId(mediaId);
        return songListFragment;
    }

}