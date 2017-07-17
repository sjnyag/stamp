package com.sjn.stamp.ui.fragment.media_list;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.sjn.stamp.R;
import com.sjn.stamp.utils.MediaIDHelper;


public class AllSongPagerFragment extends PagerFragment {

    @Override
    protected void setupViewPager(ViewPager viewPager) {
        mAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mAdapter.addFragment(createSongListFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST), getString(R.string.all_song_tab_artist));
        mAdapter.addFragment(createSongListFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM), getString(R.string.all_song_tab_album));
        mAdapter.addFragment(createSongListFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST), getString(R.string.all_song_tab_playlist));
        mAdapter.addFragment(createSongListFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL), getString(R.string.all_song_tab_all));
        viewPager.setAdapter(mAdapter);
    }

    private Fragment createSongListFragment(String mediaId) {
        SongListFragment songListFragment = new SongListFragment();
        songListFragment.setMediaId(mediaId);
        return songListFragment;
    }

}