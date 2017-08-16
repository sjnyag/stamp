package com.sjn.stamp.controller;

import android.content.Context;

import com.sjn.stamp.constant.RecordType;
import com.sjn.stamp.db.Song;
import com.sjn.stamp.db.SongHistory;
import com.sjn.stamp.db.dao.SongHistoryDao;
import com.sjn.stamp.utils.RealmHelper;

import io.realm.Realm;

class SmartStampController {
    enum SmartStamp {
        HEAVY_ROTATION("Heavy Rotation") {
            @Override
            boolean isTarget(Context context, Song song, int playCount, RecordType recordType) {
                boolean result = false;
                SongHistoryDao songHistoryDao = SongHistoryDao.getInstance();
                Realm realm = RealmHelper.getRealmInstance();
                int counter = 0;
                for (SongHistory songHistory : songHistoryDao.findAll(realm, RecordType.PLAY.getValue())) {
                    if (songHistory.getSong() == null || !songHistory.getSong().equals(song)) {
                        break;
                    }
                    counter++;
                    if (counter >= 10) {
                        result = true;
                        break;
                    }
                }
                realm.close();
                return result;
            }

            @Override
            public void register(Context context, Song song, int playCount, RecordType recordType) {
                registerStamp(context, mStamp);
                SongController songController = new SongController(context);
                songController.registerSystemStamp(mStamp, song);
            }
        },
        ARTIST_BEST("Artist Best") {
            @Override
            boolean isTarget(Context context, Song song, int playCount, RecordType recordType) {
                return false;
            }

            @Override
            public void register(Context context, Song song, int playCount, RecordType recordType) {
                registerStamp(context, mStamp);

            }
        },
        BREAK_SONG("Break Song") {
            @Override
            boolean isTarget(Context context, Song song, int playCount, RecordType recordType) {
                return false;
            }

            @Override
            public void register(Context context, Song song, int playCount, RecordType recordType) {
                registerStamp(context, mStamp);

            }
        };

        String mStamp;

        SmartStamp(String stamp) {
            mStamp = stamp;
        }

        abstract boolean isTarget(Context context, Song song, int playCount, RecordType recordType);

        public abstract void register(Context context, Song song, int playCount, RecordType recordType);

        static private void registerStamp(Context context, String stamp) {
            StampController stampController = new StampController(context);
            stampController.register(stamp);
        }
    }

    private Context mContext;

    SmartStampController(Context context) {
        this.mContext = context;
    }

    void calculateAsync(final Song song, final int playCount, final RecordType recordType) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                calculate(song, playCount, recordType);
            }
        }).start();
    }

    private void calculate(Song song, int playCount, RecordType recordType) {
        for (SmartStamp smartStamp : SmartStamp.values()) {
            if (smartStamp.isTarget(mContext, song, playCount, recordType)) {
                smartStamp.register(mContext, song, playCount, recordType);
            }
        }
    }
}