package com.sjn.stamp.utils;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.sjn.stamp.ui.DrawerMenu;

import java.util.Date;

import static com.sjn.stamp.utils.MediaIDHelper.getHierarchy;

public class AnalyticsHelper {
    private static final String CREATE_STAMP = "create_stamp";
    private static final String PLAY_CATEGORY = "play_category";
    private static final String CHANGE_SCREEN = "change_screen";
    private static final String CHANGE_SETTING = "change_setting";
    private static final String CHANGE_RANKING_TERM = "change_ranking_term";

    public static void trackCategory(Context context, String mediaId) {
        Bundle payload = new Bundle();
        String[] hierarchy = getHierarchy(mediaId);
        if (hierarchy.length < 1) {
            return;
        }
        String category = hierarchy[0];
        payload.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, category);
        FirebaseAnalytics.getInstance(context).logEvent(PLAY_CATEGORY, payload);
    }

    public static void trackScreen(Context context, DrawerMenu drawerMenu) {
        if (drawerMenu == null) {
            return;
        }
        Bundle payload = new Bundle();
        payload.putString(FirebaseAnalytics.Param.CONTENT, drawerMenu.toString());
        FirebaseAnalytics.getInstance(context).logEvent(CHANGE_SCREEN, payload);
    }

    public static void trackStamp(Context context, String stamp) {
        Bundle payload = new Bundle();
        payload.putString(FirebaseAnalytics.Param.ITEM_NAME, stamp);
        FirebaseAnalytics.getInstance(context).logEvent(CREATE_STAMP, payload);
    }

    public static void trackSetting(Context context, String name) {
        Bundle payload = new Bundle();
        payload.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        FirebaseAnalytics.getInstance(context).logEvent(CHANGE_SETTING, payload);
    }

    public static void trackSetting(Context context, String name, String value) {
        Bundle payload = new Bundle();
        payload.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        payload.putString(FirebaseAnalytics.Param.VALUE, value);
        FirebaseAnalytics.getInstance(context).logEvent(CHANGE_SETTING, payload);
    }

    public static void trackRankingTerm(Context context, Date start, Date end) {
        Bundle payload = new Bundle();
        if(start != null) {
            payload.putString(FirebaseAnalytics.Param.START_DATE, start.toString());
        }
        if(end != null) {
            payload.putString(FirebaseAnalytics.Param.END_DATE, end.toString());
        }
        FirebaseAnalytics.getInstance(context).logEvent(CHANGE_RANKING_TERM, payload);
    }
}
