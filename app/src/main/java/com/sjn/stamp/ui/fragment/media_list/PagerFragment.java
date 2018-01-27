package com.sjn.stamp.ui.fragment.media_list;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sjn.stamp.R;
import com.sjn.stamp.utils.ViewHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class PagerFragment extends FabFragment {
    static final String PAGER_KIND_KEY = "PAGER_KIND";

    protected ViewPagerAdapter mAdapter;
    protected ViewPager mViewPager;
    protected List<PageFragmentContainer> mFragments;

    abstract List<PageFragmentContainer> setUpFragmentContainer();

    protected void setupViewPager(ViewPager viewPager, Bundle savedInstanceState) {
        if (mFragments == null) {
            mFragments = setUpFragmentContainer();
        }
        mAdapter = new ViewPagerAdapter(getChildFragmentManager());
        for (PageFragmentContainer fragmentContainer : mFragments) {
            fragmentContainer.findOrCreate(savedInstanceState, getChildFragmentManager());
            mAdapter.addFragment(fragmentContainer.mFragment, fragmentContainer.mLabel);
        }
        viewPager.setAdapter(mAdapter);
    }

    static class PageFragmentContainer {
        Fragment mFragment;
        String mLabel;
        String mTag;
        String mFragmentHint;
        Creator mCreator;

        PageFragmentContainer(String label, String fragmentHint, Creator creator) {
            mLabel = label;
            mFragmentHint = fragmentHint;
            mCreator = creator;
            mTag = label + fragmentHint;
        }

        void findOrCreate(Bundle savedInstanceState, FragmentManager fragmentManager) {
            if (mFragment != null) {
                return;
            }
            if (fragmentManager.findFragmentByTag(mTag) != null) {
                mFragment = fragmentManager.getFragment(savedInstanceState, mTag);
            } else {
                mFragment = mCreator.create(mFragmentHint);
            }
        }

        interface Creator {
            Fragment create(String fragmentHint);
        }
    }

    public Fragment getCurrent() {
        if (mAdapter == null) {
            return null;
        }
        return mAdapter.getItem(mViewPager.getCurrentItem());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pager, container, false);
        mViewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        //mViewPager.setOffscreenPageLimit(4);
        setupViewPager(mViewPager, savedInstanceState);
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        for (PageFragmentContainer fragmentContainer : mFragments) {
            if (fragmentContainer.mFragment != null && fragmentContainer.mFragment.isAdded()) {
                getChildFragmentManager().putFragment(outState, fragmentContainer.mTag, fragmentContainer.mFragment);
            }
        }
    }

    protected void setTitle(String title) {
        ViewHelper.INSTANCE.setFragmentTitle(getActivity(), title);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
    }


}