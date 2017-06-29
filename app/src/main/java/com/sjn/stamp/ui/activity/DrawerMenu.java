package com.sjn.stamp.ui.activity;

import android.support.v4.app.Fragment;

import com.sjn.stamp.ui.fragment.SettingFragment;
import com.sjn.stamp.ui.fragment.media_list.QueueListFragment;
import com.sjn.stamp.ui.fragment.media_list.RankingPagerFragment;
import com.sjn.stamp.ui.fragment.media_list.TimelineFragment;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.R;
import com.sjn.stamp.ui.fragment.media_list.AllSongPagerFragment;
import com.sjn.stamp.ui.fragment.media_list.SongListFragment;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
@Getter
public enum DrawerMenu {
    HOME(R.id.navigation_home, null) {
        @Override
        Fragment getFragment() {
            return new SongListFragment();
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
    TAG(R.id.navigation_stamp, MediaIDHelper.MEDIA_ID_MUSICS_BY_STAMP) {
        @Override
        SongListFragment getFragment() {
            return getDefaultFragment(mMediaId);
        }
    },
    NEW(R.id.navigation_new, MediaIDHelper.MEDIA_ID_MUSICS_BY_NEW) {
        @Override
        SongListFragment getFragment() {
            return getDefaultFragment(mMediaId);
        }
    },
    TOP_SONG(R.id.navigation_top_song, MediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_SONG) {
        @Override
        SongListFragment getFragment() {
            return getDefaultFragment(mMediaId);
        }
    },
    ALL(R.id.navigation_all_music, null) {
        @Override
        Fragment getFragment() {
            return new AllSongPagerFragment();
        }
    },
    RANKING(R.id.navigation_ranking, null) {
        @Override
        Fragment getFragment() {
            return new RankingPagerFragment();
        }
    },
    EDIT_PLAYLIST(R.id.navigation_edit_playlist, null) {
        @Override
        Fragment getFragment() {
            return new SongListFragment();
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

    public static DrawerMenu start() {
        return TIMELINE;
    }
}
