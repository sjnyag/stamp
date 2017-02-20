package com.sjn.taggingplayer.ui.activity;

import android.support.v4.app.Fragment;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.ui.MediaBrowserFragment;
import com.sjn.taggingplayer.utils.MediaIDHelper;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
@Getter
enum DrawerMenu {
    HOME(R.id.navigation_home, null) {
        @Override
        Fragment getFragment() {
            return new MediaBrowserFragment();
        }
    },
    TIMELINE(R.id.navigation_timeline, MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE) {
        @Override
        Fragment getFragment() {
            return new MediaBrowserFragment();
        }
    },
    QUEUE(R.id.navigation_queue, MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE) {
        @Override
        Fragment getFragment() {
            return new MediaBrowserFragment();
        }
    },
    TAG(R.id.navigation_tag, MediaIDHelper.MEDIA_ID_MUSICS_BY_TAG) {
        @Override
        Fragment getFragment() {
            return new MediaBrowserFragment();
        }
    },
    NEW(R.id.navigation_new, MediaIDHelper.MEDIA_ID_MUSICS_BY_NEW) {
        @Override
        Fragment getFragment() {
            return new MediaBrowserFragment();
        }
    },
    TOP_SONG(R.id.navigation_top_song, MediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_SONG) {
        @Override
        Fragment getFragment() {
            return new MediaBrowserFragment();
        }
    },
    ALL(R.id.navigation_all_music, null) {
        @Override
        Fragment getFragment() {
            return new MediaBrowserFragment();
        }
    },
    RANKING(R.id.navigation_ranking, null) {
        @Override
        Fragment getFragment() {
            return new MediaBrowserFragment();
        }
    },
    EDIT_PLAYLIST(R.id.navigation_edit_playlist, null) {
        @Override
        Fragment getFragment() {
            return new MediaBrowserFragment();
        }
    },
    SETTING(R.id.navigation_setting, null) {
        @Override
        Fragment getFragment() {
            return new MediaBrowserFragment();
        }
    },;
    final int mMenuId;
    final String mMediaId;

    DrawerMenu(int menuId, String mediaId) {
        mMenuId = menuId;
        mMediaId = mediaId;
    }

    abstract Fragment getFragment();

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
