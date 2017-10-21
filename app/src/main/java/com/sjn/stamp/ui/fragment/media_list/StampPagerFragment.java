package com.sjn.stamp.ui.fragment.media_list;

import android.support.v4.app.Fragment;

import com.sjn.stamp.R;
import com.sjn.stamp.ui.SongListFactory;
import com.sjn.stamp.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.List;

public class StampPagerFragment extends PagerFragment implements PagerFragment.PageFragmentContainer.Creator {

    @Override
    List<PageFragmentContainer> setUpFragmentContainer() {
        List<PageFragmentContainer> fragmentContainerList = new ArrayList<>();
        fragmentContainerList.add(new PageFragmentContainer(getString(R.string.stamp_tab_my_stamps), MediaIDHelper.MEDIA_ID_MUSICS_BY_MY_STAMP, this));
        fragmentContainerList.add(new PageFragmentContainer(getString(R.string.stamp_tab_stamp_stamps), MediaIDHelper.MEDIA_ID_MUSICS_BY_SMART_STAMP, this));
        return fragmentContainerList;
    }

    @Override
    public Fragment create(String fragmentHint) {
        return SongListFactory.INSTANCE.create(fragmentHint);
    }

}