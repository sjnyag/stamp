package com.sjn.taggingplayer.ui.fragment;

import android.support.v4.view.ViewPager;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.utils.MediaIDHelper;


public class AllSongPagerFragment extends PagerFragment {

    private ViewPagerAdapter mAdapter;

    @Override
    protected void setupViewPager(ViewPager viewPager) {
        mAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mAdapter.addFragment(createSongFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST), getString(R.string.all_song_tab_artist));
        mAdapter.addFragment(createSongFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM), getString(R.string.all_song_tab_album));
        mAdapter.addFragment(createSongFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST), getString(R.string.all_song_tab_playlist));
        mAdapter.addFragment(createSongFragment(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL), getString(R.string.all_song_tab_all));
        viewPager.setAdapter(mAdapter);
    }

    private MediaBrowserFragment createSongFragment(String mediaId) {
        MediaBrowserFragment mediaBrowserFragment = new MediaBrowserFragment();
        mediaBrowserFragment.setMediaId(mediaId);
        return mediaBrowserFragment;
    }

}