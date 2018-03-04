package com.sjn.stamp.ui.fragment.media

import android.support.v4.app.Fragment
import com.sjn.stamp.R
import com.sjn.stamp.ui.SongListFragmentFactory
import com.sjn.stamp.utils.MediaIDHelper
import java.util.*

class StampPagerFragment : PagerFragment(), PagerFragment.PageFragmentContainer.Creator {

    override fun setUpFragmentContainer(): List<PagerFragment.PageFragmentContainer> =
            ArrayList<PagerFragment.PageFragmentContainer>().apply {
                add(PagerFragment.PageFragmentContainer(getString(R.string.stamp_tab_my_stamps), MediaIDHelper.MEDIA_ID_MUSICS_BY_MY_STAMP, this@StampPagerFragment))
                add(PagerFragment.PageFragmentContainer(getString(R.string.stamp_tab_smart_stamps), MediaIDHelper.MEDIA_ID_MUSICS_BY_SMART_STAMP, this@StampPagerFragment))
            }

    override fun create(fragmentHint: String): Fragment =
            SongListFragmentFactory.create(fragmentHint)

}