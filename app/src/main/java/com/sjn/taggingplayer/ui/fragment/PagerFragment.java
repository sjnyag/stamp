package com.sjn.taggingplayer.ui.fragment;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sjn.taggingplayer.R;

import java.util.ArrayList;
import java.util.List;

import static com.sjn.taggingplayer.utils.ViewHelper.setFragmentTitle;

public abstract class PagerFragment extends Fragment {
    static final String PAGER_KIND_KEY = "PAGER_KIND";

    private AppBarLayout mAppBarLayout;
    protected ViewPagerAdapter mAdapter;
    protected ViewPager mViewPager;

    abstract protected void setupViewPager(ViewPager viewPager);

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
        setupViewPager(mViewPager);
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.appbar);
        return rootView;
    }

    protected void setTitle(String title) {
        setFragmentTitle(getActivity(), title);
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