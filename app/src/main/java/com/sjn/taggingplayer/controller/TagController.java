package com.sjn.taggingplayer.controller;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.taggingplayer.constant.CategoryType;
import com.sjn.taggingplayer.db.CategoryTag;
import com.sjn.taggingplayer.db.Song;
import com.sjn.taggingplayer.db.SongTag;
import com.sjn.taggingplayer.db.dao.CategoryTagDao;
import com.sjn.taggingplayer.db.dao.SongTagDao;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.RealmHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.realm.Realm;

public class TagController {

    private static final String TAG = LogHelper.makeLogTag(TagController.class);
    private Context mContext;
    private SongTagDao mSongTagDao;
    private CategoryTagDao mCategoryTagDao;
    private List<Listener> mListenerSet = new ArrayList<>();

    public void addListener(Listener listener) {
        mListenerSet.add(listener);
    }

    public void removeListener(Listener listener) {
        mListenerSet.remove(listener);
    }

    public void remove(String tag) {
        Realm realm = RealmHelper.getRealmInstance();
        mSongTagDao.remove(realm, tag);
        mCategoryTagDao.remove(realm, tag);
        realm.close();
        notifyTagChange();
    }

    public interface Listener {
        void onTagChange();
    }

    public void notifyTagChange() {
        for (Listener listener : mListenerSet) {
            listener.onTagChange();
        }
    }

    public TagController(Context context) {
        mSongTagDao = SongTagDao.getInstance();
        mCategoryTagDao = CategoryTagDao.getInstance();
        mContext = context;
    }

    public List<String> findAll() {
        List<String> tagList = new ArrayList<>();
        Realm realm = RealmHelper.getRealmInstance();
        for (SongTag songTag : mSongTagDao.findAll(realm)) {
            tagList.add(songTag.getName());
        }
        realm.close();
        return tagList;
    }

    public boolean register(String name) {
        Realm realm = RealmHelper.getRealmInstance();
        boolean result = mSongTagDao.save(realm, name);
        realm.close();
        notifyTagChange();
        return result;
    }

    public ConcurrentMap<String, List<MediaMetadataCompat>> getAllSongList(final ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        ConcurrentMap<String, ConcurrentMap<String, MediaMetadataCompat>> songTagMap = new ConcurrentHashMap<>();
        Realm realm = RealmHelper.getRealmInstance();
        for (SongTag songTag : mSongTagDao.findAll(realm)) {
            put(songTagMap, songTag.getName(), createTrackMap(songTag));
        }
        ConcurrentMap<String, ConcurrentMap<CategoryType, List<String>>> tagQueryMap = new ConcurrentHashMap<>();
        for (CategoryTag categoryTag : mCategoryTagDao.findAll(realm)) {
            put(tagQueryMap, categoryTag);
        }
        realm.close();

        ConcurrentMap<String, ConcurrentMap<String, MediaMetadataCompat>> categoryTagMap = searchMusic(musicListById, tagQueryMap);
        merge(songTagMap, categoryTagMap);

        ConcurrentMap<String, List<MediaMetadataCompat>> tagSongMap = new ConcurrentHashMap<>();
        for (Map.Entry<String, ConcurrentMap<String, MediaMetadataCompat>> entry : songTagMap.entrySet()) {
            tagSongMap.put(entry.getKey(), new ArrayList<>(entry.getValue().values()));
        }
        return tagSongMap;
    }

    private void merge(ConcurrentMap<String, ConcurrentMap<String, MediaMetadataCompat>> songTagMap, ConcurrentMap<String, ConcurrentMap<String, MediaMetadataCompat>> categoryTagMap) {
        for (String tag : categoryTagMap.keySet()) {
            if (songTagMap.containsKey(tag)) {
                songTagMap.get(tag).putAll(categoryTagMap.get(tag));
            } else {
                songTagMap.put(tag, categoryTagMap.get(tag));
            }
        }
    }

    private ConcurrentMap<String, MediaMetadataCompat> createTrackMap(SongTag songTag) {
        ConcurrentMap<String, MediaMetadataCompat> trackMap = new ConcurrentHashMap<>();
        for (Song song : songTag.getSongList()) {
            trackMap.put(song.getMediaId(), song.buildMediaMetadataCompat());
        }
        return trackMap;
    }

    private ConcurrentMap<String, ConcurrentMap<String, MediaMetadataCompat>> searchMusic(final ConcurrentMap<String, MediaMetadataCompat> musicListById, ConcurrentMap<String, ConcurrentMap<CategoryType, List<String>>> queryMap) {
        ConcurrentMap<String, ConcurrentMap<String, MediaMetadataCompat>> result = new ConcurrentHashMap<>();
        if (musicListById == null || queryMap == null) {
            return result;
        }
        for (MediaMetadataCompat track : musicListById.values()) {
            for (Map.Entry<String, ConcurrentMap<CategoryType, List<String>>> entry1 : queryMap.entrySet()) {
                for (Map.Entry<CategoryType, List<String>> entry : entry1.getValue().entrySet()) {
                    if (entry.getValue().contains(track.getString(entry.getKey().getKey()).toLowerCase(Locale.US))) {
                        put(result, entry1.getKey(), track);
                    }
                }
            }
        }
        return result;
    }

    private void put(ConcurrentMap<String, ConcurrentMap<String, MediaMetadataCompat>> tagMap, String tagName, MediaMetadataCompat track) {
        if (tagMap == null) {
            return;
        }
        if (tagMap.containsKey(tagName) && !tagMap.get(tagName).isEmpty()) {
            tagMap.get(tagName).put(track.getDescription().getMediaId(), track);
        } else {
            ConcurrentMap<String, MediaMetadataCompat> trackMap = new ConcurrentHashMap<>();
            trackMap.put(track.getDescription().getMediaId(), track);
            tagMap.put(tagName, trackMap);
        }
    }

    private void put(ConcurrentMap<String, ConcurrentMap<String, MediaMetadataCompat>> tagMap, String tagName, ConcurrentMap<String, MediaMetadataCompat> trackMap) {
        if (tagMap == null || trackMap == null) {
            return;
        }
        if (tagMap.containsKey(tagName)) {
            tagMap.get(tagName).putAll(trackMap);
        } else {
            tagMap.put(tagName, trackMap);
        }
    }

    private void put(ConcurrentMap<String, ConcurrentMap<CategoryType, List<String>>> tagQueryMap, CategoryTag categoryTag) {
        if (tagQueryMap == null || categoryTag == null) {
            return;
        }
        if (tagQueryMap.containsKey(categoryTag.getName())) {
            putQuery(tagQueryMap.get(categoryTag.getName()), categoryTag);
        } else {
            ConcurrentMap<CategoryType, List<String>> queryMap = new ConcurrentHashMap<>();
            putQuery(queryMap, categoryTag);
            tagQueryMap.put(categoryTag.getName(), queryMap);
        }
    }

    private void putQuery(ConcurrentMap<CategoryType, List<String>> queryMap, CategoryTag categoryTag) {
        if (queryMap == null || categoryTag == null) {
            return;
        }
        CategoryType categoryType = CategoryType.of(categoryTag.getType());
        if (categoryType == null) {
            return;
        }
        String query = categoryTag.getValue().toLowerCase(Locale.US);
        if (queryMap.containsKey(categoryType) && !queryMap.get(categoryType).isEmpty()) {
            queryMap.get(categoryType).add(query);
        } else {
            queryMap.put(categoryType, new ArrayList<>(Collections.singletonList(query)));
        }
    }
}
