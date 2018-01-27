package com.sjn.stamp.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.sjn.stamp.ui.DrawerMenu
import com.sjn.stamp.utils.MediaIDHelper.getHierarchy
import java.util.*

object AnalyticsHelper {
    private const val CREATE_STAMP = "create_stamp"
    private const val PLAY_CATEGORY = "play_category"
    private const val CHANGE_SCREEN = "change_screen"
    private const val CHANGE_SETTING = "change_setting"
    private const val CHANGE_RANKING_TERM = "change_ranking_term"

    fun trackCategory(context: Context, mediaId: String) {
        val hierarchy = getHierarchy(mediaId)
        if (hierarchy.isEmpty()) {
            return
        }
        FirebaseAnalytics.getInstance(context).logEvent(PLAY_CATEGORY, Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_CATEGORY, hierarchy[0])
        })
    }

    fun trackScreen(context: Context, drawerMenu: DrawerMenu?) {
        drawerMenu?.let {
            FirebaseAnalytics.getInstance(context).logEvent(CHANGE_SCREEN, Bundle().apply {
                putString(FirebaseAnalytics.Param.CONTENT, drawerMenu.toString())
            })
        }
    }

    fun trackStamp(context: Context, stamp: String) {
        FirebaseAnalytics.getInstance(context).logEvent(CREATE_STAMP, Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, stamp)
        })
    }

    fun trackSetting(context: Context, name: String) {
        FirebaseAnalytics.getInstance(context).logEvent(CHANGE_SETTING, Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        })
    }

    fun trackSetting(context: Context, name: String, value: String) {
        FirebaseAnalytics.getInstance(context).logEvent(CHANGE_SETTING, Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, name)
            putString(FirebaseAnalytics.Param.VALUE, value)
        })
    }

    fun trackRankingTerm(context: Context, start: Date?, end: Date?) {
        FirebaseAnalytics.getInstance(context).logEvent(CHANGE_RANKING_TERM, Bundle().apply {
            start?.let {
                putString(FirebaseAnalytics.Param.START_DATE, it.toString())
            }
            end?.let {
                putString(FirebaseAnalytics.Param.END_DATE, it.toString())
            }
        })
    }
}
