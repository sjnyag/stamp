package com.sjn.taggingplayer.utils;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmHelper {
    private static final String TAG = LogHelper.makeLogTag(RealmHelper.class);

    public static void init(Context context){
        Realm.init(context);
        Realm.setDefaultConfiguration(buildConfig());
    }

    public static Realm getRealmInstance() {
        return Realm.getDefaultInstance();
    }

    public static RealmConfiguration buildConfig(){
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder();
        if (com.sjn.taggingplayer.BuildConfig.BUILD_TYPE.equals("debug")) {
            //builder.deleteRealmIfMigrationNeeded();
        }
        return builder.build();
    }
}
