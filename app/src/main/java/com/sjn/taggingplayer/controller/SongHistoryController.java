package com.sjn.taggingplayer.controller;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.taggingplayer.constant.RecordType;
import com.sjn.taggingplayer.db.Artist;
import com.sjn.taggingplayer.db.Device;
import com.sjn.taggingplayer.db.RankedArtist;
import com.sjn.taggingplayer.db.RankedSong;
import com.sjn.taggingplayer.db.Song;
import com.sjn.taggingplayer.db.SongHistory;
import com.sjn.taggingplayer.db.TotalSongHistory;
import com.sjn.taggingplayer.db.dao.DeviceDao;
import com.sjn.taggingplayer.db.dao.SongDao;
import com.sjn.taggingplayer.db.dao.SongHistoryDao;
import com.sjn.taggingplayer.db.dao.TotalSongHistoryDao;
import com.sjn.taggingplayer.media.provider.ListProvider;
import com.sjn.taggingplayer.ui.custom.TermSelectLayout;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.RealmHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
    private TotalSongHistoryDao mTotalSongHistoryDao;

    public SongHistoryController(Context context) {
        mContext = context;
        mDeviceDao = DeviceDao.getInstance();
        mSongDao = SongDao.getInstance();
        mSongHistoryDao = SongHistoryDao.getInstance();
        mTotalSongHistoryDao = TotalSongHistoryDao.getInstance();
    }

    public void onPlay(MediaMetadataCompat track, Date date) {
        LogHelper.i(TAG, "insertPLAY ", track.getDescription().getTitle());
        registerHistory(track, RecordType.PLAY, date);
    }

    public void onSkip(MediaMetadataCompat track, Date date) {
        LogHelper.i(TAG, "insertSKIP ", track.getDescription().getTitle());
        registerHistory(track, RecordType.SKIP, date);
    }

    public void onStart(MediaMetadataCompat track, Date date) {
        LogHelper.i(TAG, "insertSTART ", track.getDescription().getTitle());
        registerHistory(track, RecordType.START, date);
    }

    public void onComplete(MediaMetadataCompat track, Date date) {
        LogHelper.i(TAG, "insertComplete ", track.getDescription().getTitle());
        registerHistory(track, RecordType.COMPLETE, date);
    }

    private void registerHistory(MediaMetadataCompat track, RecordType recordType, Date date) {
        Song song = createSong(track);
        Realm realm = RealmHelper.getRealmInstance();
        int playCount = mTotalSongHistoryDao.saveOrIncrement(realm, createTotalSongHistory(song, recordType));
        mSongHistoryDao.save(realm, createSongHistory(song, createDevice(), recordType, date, playCount));
        realm.close();
    }

    private TotalSongHistory createTotalSongHistory(Song song, RecordType recordType) {
        TotalSongHistory totalSongHistory = mTotalSongHistoryDao.newStandalone();
        totalSongHistory.parseSongQueue(song, recordType);
        return totalSongHistory;
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

    private SongHistory createSongHistory(Song song, Device device, RecordType recordType, Date date, int count) {
        SongHistory songHistory = mSongHistoryDao.newStandalone();
        songHistory.setValues(song, recordType, device, date, count);
        return songHistory;
    }

    public List<MediaMetadataCompat> getTopSongList() {
        Realm realm = RealmHelper.getRealmInstance();
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

    public List<SongHistory> getManagedTimeline(Realm realm) {
        return mSongHistoryDao.timeline(realm, RecordType.PLAY.getValue());
    }

    public List<RankedSong> getRankedSongList(TermSelectLayout.Term term) {
        return getRankedSongList(
                term.from() == null ? null : term.from().toDateTimeAtStartOfDay().toDate(),
                term.to() == null ? null : term.to().toDateTimeAtStartOfDay().plusDays(1).toDate());
    }

    public List<RankedArtist> getRankedArtistList(TermSelectLayout.Term term) {
        return getRankedArtistList(
                term.from() == null ? null : term.from().toDateTimeAtStartOfDay().toDate(),
                term.to() == null ? null : term.to().toDateTimeAtStartOfDay().plusDays(1).toDate());
    }

    public List<RankedSong> getRankedSongList(Date from, Date to) {
        Realm realm = RealmHelper.getRealmInstance();
        List<SongHistory> historyList = mSongHistoryDao.where(realm, from, to, RecordType.PLAY.getValue());
        Map<Song, Integer> songMap = new HashMap<>();
        for (SongHistory songHistory : historyList) {
            Song song = realm.copyFromRealm(songHistory.getSong());
            int count = songMap.containsKey(song) ? songMap.get(song) + 1 : 1;
            songMap.put(song, count);
        }
        realm.close();
        List<RankedSong> rankedSongList = new ArrayList<>();
        for (Map.Entry<Song, Integer> e : songMap.entrySet()) {
            rankedSongList.add(new RankedSong(e.getValue(), e.getKey()));
        }
        Collections.sort(rankedSongList, new Comparator<RankedSong>() {
            @Override
            public int compare(RankedSong t1, RankedSong t2) {
                return t2.getPlayCount() - t1.getPlayCount();
            }
        });
        return rankedSongList;
    }

    public List<RankedArtist> getRankedArtistList(Date from, Date to) {
        Realm realm = RealmHelper.getRealmInstance();
        List<SongHistory> historyList = mSongHistoryDao.where(realm, from, to, RecordType.PLAY.getValue());
        Map<String, Integer> artistMap = new HashMap<>();
        for (SongHistory songHistory : historyList) {
            String artist = songHistory.getSong().getArtist();
            int count = artistMap.containsKey(artist) ? artistMap.get(artist) + 1 : 1;
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
