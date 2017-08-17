package com.sjn.stamp.controller;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.constant.CategoryType;
import com.sjn.stamp.db.CategoryStamp;
import com.sjn.stamp.db.Song;
import com.sjn.stamp.db.SongStamp;
import com.sjn.stamp.db.dao.CategoryStampDao;
import com.sjn.stamp.db.dao.SongStampDao;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.RealmHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.realm.Realm;

public class StampController {
    @SuppressWarnings("unused")
    private static final String TAG = LogHelper.makeLogTag(StampController.class);
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private Context mContext;
    private List<Listener> mListenerSet = new ArrayList<>();

    public void addListener(Listener listener) {
        mListenerSet.add(listener);
    }

    public void removeListener(Listener listener) {
        mListenerSet.remove(listener);
    }

    public void remove(String stamp) {
        Realm realm = RealmHelper.getRealmInstance();
        SongStampDao.INSTANCE.remove(realm, stamp);
        CategoryStampDao.INSTANCE.remove(realm, stamp);
        realm.close();
        notifyStampChange();
    }

    public interface Listener {
        void onStampChange();
    }

    void notifyStampChange() {
        for (Listener listener : mListenerSet) {
            listener.onStampChange();
        }
    }

    public StampController(Context context) {
        mContext = context;
    }

    public List<String> findAll() {
        List<String> stampList = new ArrayList<>();
        Realm realm = RealmHelper.getRealmInstance();
        for (SongStamp songStamp : SongStampDao.INSTANCE.findAll(realm)) {
            stampList.add(songStamp.getName());
        }
        realm.close();
        return stampList;
    }

    public boolean register(String name) {
        Realm realm = RealmHelper.getRealmInstance();
        boolean result = SongStampDao.INSTANCE.save(realm, name);
        realm.close();
        notifyStampChange();
        return result;
    }

    public Map<String, List<MediaMetadataCompat>> getAllSongList(final Map<String, MediaMetadataCompat> musicListById) {
        Map<String, Map<String, MediaMetadataCompat>> songStampMap = new ConcurrentHashMap<>();
        Realm realm = RealmHelper.getRealmInstance();
        for (SongStamp songStamp : SongStampDao.INSTANCE.findAll(realm)) {
            put(songStampMap, songStamp.getName(), createTrackMap(songStamp));
        }
        Map<String, Map<CategoryType, List<String>>> stampQueryMap = new ConcurrentHashMap<>();
        for (CategoryStamp categoryStamp : CategoryStampDao.INSTANCE.findAll(realm)) {
            put(stampQueryMap, categoryStamp);
        }
        realm.close();

        Map<String, Map<String, MediaMetadataCompat>> categoryStampMap = searchMusic(musicListById, stampQueryMap);
        merge(songStampMap, categoryStampMap);

        Map<String, List<MediaMetadataCompat>> stampSongMap = new ConcurrentHashMap<>();
        for (Map.Entry<String, Map<String, MediaMetadataCompat>> entry : songStampMap.entrySet()) {
            stampSongMap.put(entry.getKey(), new ArrayList<>(entry.getValue().values()));
        }
        return stampSongMap;
    }

    private void merge(Map<String, Map<String, MediaMetadataCompat>> songStampMap, Map<String, Map<String, MediaMetadataCompat>> categoryStampMap) {
        for (String stamp : categoryStampMap.keySet()) {
            if (songStampMap.containsKey(stamp)) {
                songStampMap.get(stamp).putAll(categoryStampMap.get(stamp));
            } else {
                songStampMap.put(stamp, categoryStampMap.get(stamp));
            }
        }
    }

    private Map<String, MediaMetadataCompat> createTrackMap(SongStamp songStamp) {
        Map<String, MediaMetadataCompat> trackMap = new ConcurrentHashMap<>();
        if (songStamp.getSongList() == null) {
            return trackMap;
        }
        for (Song song : songStamp.getSongList()) {
            trackMap.put(song.getMediaId(), song.buildMediaMetadataCompat());
        }
        return trackMap;
    }

    private Map<String, Map<String, MediaMetadataCompat>> searchMusic(final Map<String, MediaMetadataCompat> musicListById, Map<String, Map<CategoryType, List<String>>> queryMap) {
        Map<String, Map<String, MediaMetadataCompat>> result = new ConcurrentHashMap<>();
        if (musicListById == null || queryMap == null) {
            return result;
        }
        for (MediaMetadataCompat track : musicListById.values()) {
            for (Map.Entry<String, Map<CategoryType, List<String>>> entry1 : queryMap.entrySet()) {
                for (Map.Entry<CategoryType, List<String>> entry : entry1.getValue().entrySet()) {
                    if (entry.getValue().contains(track.getString(entry.getKey().getKey()).toLowerCase(Locale.getDefault()))) {
                        put(result, entry1.getKey(), track);
                    }
                }
            }
        }
        return result;
    }

    private void put(Map<String, Map<String, MediaMetadataCompat>> stampMap, String stampName, MediaMetadataCompat track) {
        if (stampMap == null) {
            return;
        }
        if (stampMap.containsKey(stampName) && !stampMap.get(stampName).isEmpty()) {
            stampMap.get(stampName).put(track.getDescription().getMediaId(), track);
        } else {
            Map<String, MediaMetadataCompat> trackMap = new ConcurrentHashMap<>();
            trackMap.put(track.getDescription().getMediaId(), track);
            stampMap.put(stampName, trackMap);
        }
    }

    private void put(Map<String, Map<String, MediaMetadataCompat>> stampMap, String stampName, Map<String, MediaMetadataCompat> trackMap) {
        if (stampMap == null || trackMap == null) {
            return;
        }
        if (stampMap.containsKey(stampName)) {
            stampMap.get(stampName).putAll(trackMap);
        } else {
            stampMap.put(stampName, trackMap);
        }
    }

    private void put(Map<String, Map<CategoryType, List<String>>> stampQueryMap, CategoryStamp categoryStamp) {
        if (stampQueryMap == null || categoryStamp == null) {
            return;
        }
        if (stampQueryMap.containsKey(categoryStamp.getName())) {
            putQuery(stampQueryMap.get(categoryStamp.getName()), categoryStamp);
        } else {
            Map<CategoryType, List<String>> queryMap = new ConcurrentHashMap<>();
            putQuery(queryMap, categoryStamp);
            stampQueryMap.put(categoryStamp.getName(), queryMap);
        }
    }

    private void putQuery(Map<CategoryType, List<String>> queryMap, CategoryStamp categoryStamp) {
        if (queryMap == null || categoryStamp == null) {
            return;
        }
        CategoryType categoryType = CategoryType.Companion.of(categoryStamp.getType());
        if (categoryType == null || categoryStamp.getValue() == null) {
            return;
        }
        String query = categoryStamp.getValue().toLowerCase(Locale.getDefault());
        if (queryMap.containsKey(categoryType) && !queryMap.get(categoryType).isEmpty()) {
            queryMap.get(categoryType).add(query);
        } else {
            queryMap.put(categoryType, new ArrayList<>(Collections.singletonList(query)));
        }
    }
}
