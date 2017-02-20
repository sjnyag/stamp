package com.sjn.taggingplayer.utils;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmHelper {
    private static final String TAG = LogHelper.makeLogTag(RealmHelper.class);

    public static Realm getRealmInstance(Context context) {
        return Realm.getInstance(new RealmConfiguration.Builder(context)
                .deleteRealmIfMigrationNeeded().build());
    }
}
