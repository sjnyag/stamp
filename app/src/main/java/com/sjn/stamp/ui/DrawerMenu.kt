package com.sjn.stamp.ui

import android.support.v4.app.Fragment
import com.sjn.stamp.R
import com.sjn.stamp.ui.fragment.SettingFragment
import com.sjn.stamp.ui.fragment.media_list.*

enum class DrawerMenu(val menuId: Int) {
    HOME(R.id.navigation_home) {
        override val fragment: Fragment
            get() = AllSongPagerFragment()
    },
    TIMELINE(R.id.navigation_timeline) {
        override val fragment: Fragment
            get() = TimelineFragment()
    },
    QUEUE(R.id.navigation_queue) {
        override val fragment: Fragment
            get() = QueueListFragment()
    },
    STAMP(R.id.navigation_stamp) {
        override val fragment: Fragment
            get() = StampPagerFragment()
    },
    RANKING(R.id.navigation_ranking) {
        override val fragment: Fragment
            get() = RankingPagerFragment()
    },
    SETTING(R.id.navigation_setting) {
        override val fragment: Fragment
            get() = SettingFragment()
    };

    abstract val fragment: Fragment

    companion object {

        fun of(menuId: Long): DrawerMenu? = DrawerMenu.values().firstOrNull { it.menuId.toLong() == menuId }

        fun first(): DrawerMenu = HOME
    }
}
