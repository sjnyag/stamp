package com.sjn.taggingplayer.controller;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.taggingplayer.constant.RecordType;
import com.sjn.taggingplayer.db.Artist;
import com.sjn.taggingplayer.db.DailySongHistory;
import com.sjn.taggingplayer.db.Device;
import com.sjn.taggingplayer.db.RankedArtist;
import com.sjn.taggingplayer.db.RankedSong;
import com.sjn.taggingplayer.db.Song;
import com.sjn.taggingplayer.db.SongHistory;
import com.sjn.taggingplayer.db.TotalSongHistory;
import com.sjn.taggingplayer.db.dao.DailySongHistoryDao;
import com.sjn.taggingplayer.db.dao.DeviceDao;
import com.sjn.taggingplayer.db.dao.SongDao;
import com.sjn.taggingplayer.db.dao.SongHistoryDao;
import com.sjn.taggingplayer.db.dao.TotalSongHistoryDao;
import com.sjn.taggingplayer.media.provider.ListProvider;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.RealmHelper;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

public class SongHistoryController {

    private static final String TAG = LogHelper.makeLogTag(SongHistoryController.class);

    private Context mContext;
    private DeviceDao mDeviceDao;
    private SongDao mSongDao;
    private SongHistoryDao mSongHistoryDao;
    private DailySongHistoryDao mDailySongHistoryDao;
    private TotalSongHistoryDao mTotalSongHistoryDao;

    public SongHistoryController(Context context) {
        mContext = context;
        mDeviceDao = DeviceDao.getInstance();
        mSongDao = SongDao.getInstance();
        mSongHistoryDao = SongHistoryDao.getInstance();
        mDailySongHistoryDao = DailySongHistoryDao.getInstance();
        mTotalSongHistoryDao = TotalSongHistoryDao.getInstance();
    }

    public void onPlay(MediaMetadataCompat track, DateTime dateTime) {
        LogHelper.i(TAG, "insertPLAY ", track.getDescription().getTitle());
        registerHistory(track, RecordType.PLAY, dateTime);
    }

    public void onSkip(MediaMetadataCompat track, DateTime dateTime) {
        LogHelper.i(TAG, "insertSKIP ", track.getDescription().getTitle());
        registerHistory(track, RecordType.SKIP, dateTime);
    }

    public void onStart(MediaMetadataCompat track, DateTime dateTime) {
        LogHelper.i(TAG, "insertSTART ", track.getDescription().getTitle());
        registerHistory(track, RecordType.START, dateTime);
    }

    public void onComplete(MediaMetadataCompat track, DateTime dateTime) {
        LogHelper.i(TAG, "insertComplete ", track.getDescription().getTitle());
        registerHistory(track, RecordType.COMPLETE, dateTime);
    }

    private void registerHistory(MediaMetadataCompat track, RecordType recordType, DateTime dateTime) {
        SongHistory songHistory = createSongHistory(createSong(track), createDevice(), recordType, dateTime);
        Realm realm = RealmHelper.getRealmInstance(mContext);
        mDailySongHistoryDao.saveOrIncrement(realm, createDailySongHistory(songHistory));
        mTotalSongHistoryDao.saveOrIncrement(realm, createTotalSongHistory(songHistory));
        realm.close();
    }

    private TotalSongHistory createTotalSongHistory(SongHistory songHistory) {
        TotalSongHistory totalSongHistory = mTotalSongHistoryDao.newStandalone();
        totalSongHistory.parseSongQueue(songHistory);
        return totalSongHistory;
    }

    private DailySongHistory createDailySongHistory(SongHistory songHistory) {
        DailySongHistory dailySongHistory = mDailySongHistoryDao.newStandalone();
        dailySongHistory.parseSongQueue(songHistory);
        return dailySongHistory;
    }

    private Device createDevice() {
        Device device = mDeviceDao.newStandalone();
        device.configure();
        return device;
    }

    private Song createSong(MediaMetadataCompat track) {
        Song song = mSongDao.newStandalone();
        song.parseMetadata(track);
        return song;
    }

    private SongHistory createSongHistory(Song song, Device device, RecordType recordType, DateTime dateTime) {
        SongHistory songHistory = mSongHistoryDao.newStandalone();
        songHistory.setValues(song, recordType, device, dateTime);
        return songHistory;
    }

    public List<MediaMetadataCompat> getTopSongList() {
        Realm realm = RealmHelper.getRealmInstance(mContext);
        List<MediaMetadataCompat> trackList = new ArrayList<>();
        List<TotalSongHistory> historyList = mTotalSongHistoryDao.getOrderedList(realm);
        for (TotalSongHistory totalSongHistory : historyList) {
            if (totalSongHistory.getPlayCount() == 0) {
                break;
            }
            //noinspection ResourceType
            trackList.add(
                    totalSongHistory.getSong().mediaMetadataCompatBuilder()
                            .putLong(ListProvider.CUSTOM_METADATA_TRACK_PREFIX, totalSongHistory.getPlayCount())
                            .build()
            );
        }
        realm.close();
        return trackList;
    }

    public List<RankedSong> getRankedSongList() {
        Realm realm = RealmHelper.getRealmInstance(mContext);
        List<RankedSong> rankedSongList = new ArrayList<>();
        List<TotalSongHistory> historyList = mTotalSongHistoryDao.getOrderedList(realm);
        for (TotalSongHistory totalSongHistory : historyList) {
            if (totalSongHistory.getPlayCount() == 0) {
                break;
            }
            rankedSongList.add(new RankedSong(totalSongHistory.getPlayCount(), realm.copyFromRealm(totalSongHistory.getSong())));
        }
        realm.close();
        return rankedSongList;
    }

    public List<RankedArtist> getRankedArtistList() {
        Realm realm = RealmHelper.getRealmInstance(mContext);
        List<TotalSongHistory> historyList = mTotalSongHistoryDao.getOrderedList(realm);
        Map<String, Integer> artistMap = new HashMap<>();
        for (TotalSongHistory totalSongHistory : historyList) {
            if (totalSongHistory.getPlayCount() == 0) {
                break;
            }
            String artist = totalSongHistory.getSong().getArtist();
            int count = artistMap.containsKey(artist) ? artistMap.get(artist) + totalSongHistory.getPlayCount() : totalSongHistory.getPlayCount();
            artistMap.put(artist, count);
        }
        realm.close();
        List<RankedArtist> rankedArtistList = new ArrayList<>();
        for (Map.Entry<String, Integer> e : artistMap.entrySet()) {
            rankedArtistList.add(new RankedArtist(e.getValue(), new Artist(e.getKey(), "")));
        }
        Collections.sort(rankedArtistList, new Comparator<RankedArtist>() {
            @Override
            public int compare(RankedArtist t1, RankedArtist t2) {
                return t2.getPlayCount() - t1.getPlayCount();
            }
        });
        return rankedArtistList;
    }

}
