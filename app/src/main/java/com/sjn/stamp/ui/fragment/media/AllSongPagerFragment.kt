package com.sjn.stamp.ui.fragment.media

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sjn.stamp.R
import com.sjn.stamp.controller.SongHistoryController
import com.sjn.stamp.controller.StampController
import com.sjn.stamp.ui.SongListFragmentFactory
import com.sjn.stamp.utils.MediaIDHelper
import java.util.*


class AllSongPagerFragment : PagerFragment(), PagerFragment.PageFragmentContainer.Creator {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = super.onCreateView(inflater, container, savedInstanceState) ?: return null
        val tabLayout = rootView.findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        return rootView
    }

    override fun setUpFragmentContainer(): List<PagerFragment.PageFragmentContainer> =
            ArrayList<PagerFragment.PageFragmentContainer>().apply {
                context?.let {
                    if (StampController(it).isMyStampExists) {
                        add(PagerFragment.PageFragmentContainer(getString(R.string.stamp_tab_my_stamps), MediaIDHelper.MEDIA_ID_MUSICS_BY_MY_STAMP, this@AllSongPagerFragment))
                    }
                    if (StampController(it).isSmartStampExists) {
                        add(PagerFragment.PageFragmentContainer(getString(R.string.stamp_tab_smart_stamps), MediaIDHelper.MEDIA_ID_MUSICS_BY_SMART_STAMP, this@AllSongPagerFragment))
                    }
                    if (SongHistoryController(it).hasHistory) {
                        add(PagerFragment.PageFragmentContainer(getString(R.string.all_song_tab_most_played), MediaIDHelper.MEDIA_ID_MUSICS_BY_MOST_PLAYED, this@AllSongPagerFragment))
                    }
                }
                add(PagerFragment.PageFragmentContainer(getString(R.string.all_song_tab_artist), MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST, this@AllSongPagerFragment))
                add(PagerFragment.PageFragmentContainer(getString(R.string.all_song_tab_album), MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM, this@AllSongPagerFragment))
                add(PagerFragment.PageFragmentContainer(getString(R.string.all_song_tab_playlist), MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST, this@AllSongPagerFragment))
                add(PagerFragment.PageFragmentContainer(getString(R.string.all_song_tab_all), MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL, this@AllSongPagerFragment))
                add(PagerFragment.PageFragmentContainer(getString(R.string.all_song_tab_new), MediaIDHelper.MEDIA_ID_MUSICS_BY_NEW, this@AllSongPagerFragment))
            }

    override fun create(fragmentHint: String): Fragment =
            SongListFragmentFactory.create(fragmentHint)
}