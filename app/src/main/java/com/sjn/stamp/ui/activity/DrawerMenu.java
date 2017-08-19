package com.sjn.stamp.ui.activity;

import android.support.v4.app.Fragment;

import com.sjn.stamp.R;
import com.sjn.stamp.ui.fragment.SettingFragment;
import com.sjn.stamp.ui.fragment.media_list.AllSongPagerFragment;
import com.sjn.stamp.ui.fragment.media_list.QueueListFragment;
import com.sjn.stamp.ui.fragment.media_list.RankingPagerFragment;
import com.sjn.stamp.ui.fragment.media_list.SongListFragment;
import com.sjn.stamp.ui.fragment.media_list.StampPagerFragment;
import com.sjn.stamp.ui.fragment.media_list.TimelineFragment;
import com.sjn.stamp.utils.MediaIDHelper;

public enum DrawerMenu {
    HOME(R.id.navigation_home) {
        @Override
        Fragment getFragment() {
            return new AllSongPagerFragment();
        }
    },
    TIMELINE(R.id.navigation_timeline) {
        @Override
        Fragment getFragment() {
            return new TimelineFragment();
        }
    },
    QUEUE(R.id.navigation_queue) {
        @Override
        Fragment getFragment() {
            return new QueueListFragment();
        }
    },
    STAMP(R.id.navigation_stamp) {
        @Override
        Fragment getFragment() {
            return new StampPagerFragment();
        }
    },
    RANKING(R.id.navigation_ranking) {
        @Override
        Fragment getFragment() {
            return new RankingPagerFragment();
        }
    },
    SETTING(R.id.navigation_setting) {
        @Override
        Fragment getFragment() {
            return new SettingFragment();
        }
    },;
    final int mMenuId;

    DrawerMenu(int menuId) {
        mMenuId = menuId;
    }

    public int getMenuId() {
        return mMenuId;
    }

    abstract Fragment getFragment();

    public static DrawerMenu of(long menuId) {
        for (DrawerMenu drawerMenu : DrawerMenu.values()) {
            if (drawerMenu.mMenuId == menuId) return drawerMenu;
        }
        return null;
    }

    public static DrawerMenu first() {
        return HOME;
    }
}
