package com.sjn.stamp.ui.activity;

import android.support.v4.app.Fragment;

import com.sjn.stamp.R;
import com.sjn.stamp.ui.fragment.SettingFragment;
import com.sjn.stamp.ui.fragment.media_list.AllSongPagerFragment;
import com.sjn.stamp.ui.fragment.media_list.QueueListFragment;
import com.sjn.stamp.ui.fragment.media_list.RankingPagerFragment;
import com.sjn.stamp.ui.fragment.media_list.SongListFragment;
import com.sjn.stamp.ui.fragment.media_list.TimelineFragment;
import com.sjn.stamp.utils.MediaIDHelper;

public enum DrawerMenu {
    HOME(R.id.navigation_home, null) {
        @Override
        Fragment getFragment() {
            return new AllSongPagerFragment();
        }
    },
    TIMELINE(R.id.navigation_timeline, MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE) {
        @Override
        Fragment getFragment() {
            return new TimelineFragment();
        }
    },
    QUEUE(R.id.navigation_queue, MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE) {
        @Override
        Fragment getFragment() {
            return new QueueListFragment();
        }
    },
    STAMP(R.id.navigation_stamp, MediaIDHelper.MEDIA_ID_MUSICS_BY_STAMP) {
        @Override
        SongListFragment getFragment() {
            return getDefaultFragment(mMediaId);
        }
    },
    RANKING(R.id.navigation_ranking, null) {
        @Override
        Fragment getFragment() {
            return new RankingPagerFragment();
        }
    },
    SETTING(R.id.navigation_setting, null) {
        @Override
        Fragment getFragment() {
            return new SettingFragment();
        }
    },;
    final int mMenuId;
    final String mMediaId;

    DrawerMenu(int menuId, String mediaId) {
        mMenuId = menuId;
        mMediaId = mediaId;
    }

    public int getMenuId() {
        return mMenuId;
    }

    public String getMediaId() {
        return mMediaId;
    }

    abstract Fragment getFragment();

    private static SongListFragment getDefaultFragment(String mediaId) {
        SongListFragment songListFragment = new SongListFragment();
        songListFragment.setMediaId(mediaId);
        return songListFragment;
    }

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
