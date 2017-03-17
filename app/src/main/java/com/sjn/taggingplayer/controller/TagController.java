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
import java.util.List;
import java.util.Locale;
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
        Realm realm = RealmHelper.getRealmInstance(mContext);
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
        Realm realm = RealmHelper.getRealmInstance(mContext);
        for (SongTag songTag : mSongTagDao.findAll(realm)) {
            tagList.add(songTag.getName());
        }
        realm.close();
        return tagList;
    }

    public boolean register(String name) {
        Realm realm = RealmHelper.getRealmInstance(mContext);
        boolean result = mSongTagDao.save(realm, name);
        realm.close();
        notifyTagChange();
        return result;
    }

    public ConcurrentMap<String, List<MediaMetadataCompat>> getAllSongList(final ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        ConcurrentMap<String, List<MediaMetadataCompat>> playlistMap = new ConcurrentHashMap<>();
        Realm realm = RealmHelper.getRealmInstance(mContext);
        for (SongTag songTag : mSongTagDao.findAll(realm)) {
            List<MediaMetadataCompat> trackList = findTrackListBySongTag(realm, songTag, musicListById);
            if (trackList != null && !trackList.isEmpty()) {
                playlistMap.put(songTag.getName(), trackList);
            }
        }
        for (CategoryTag categoryTag : mCategoryTagDao.findAllTagGroupByName(realm)) {
            if (!playlistMap.containsKey(categoryTag.getName())) {
                List<MediaMetadataCompat> trackList = findTrackListByCategoryTagName(realm, categoryTag.getName(), musicListById);
                if (trackList != null && !trackList.isEmpty()) {
                    playlistMap.put(categoryTag.getName(), trackList);
                }
            }
        }
        realm.close();
        return playlistMap;
    }

    private List<MediaMetadataCompat> findTrackListBySongTag(Realm realm, SongTag songTag, final ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        ConcurrentMap<String, MediaMetadataCompat> trackMap = new ConcurrentHashMap<>();
        for (Song song : songTag.getSongList()) {
            song.buildMediaMetadataCompat();
            trackMap.put(song.getMediaId(), song.buildMediaMetadataCompat());
        }
        for (MediaMetadataCompat track : findTrackListByCategoryTagName(realm, songTag.getName(), musicListById)) {
            trackMap.put(track.getDescription().getMediaId(), track);
        }
        return new ArrayList<>(trackMap.values());
    }

    private List<MediaMetadataCompat> findTrackListByCategoryTagName(Realm realm, String tagName, final ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        List<MediaMetadataCompat> trackList = new ArrayList<>();
        for (CategoryTag categoryTag : mCategoryTagDao.findCategoryTagList(realm, tagName)) {
            CategoryType categoryType = CategoryType.of(categoryTag.getType());
            if (categoryType != null) {
                List<MediaMetadataCompat> tempTrackList = searchMusic(musicListById, categoryType.getKey(), categoryTag.getValue());
                if (tempTrackList != null && !tempTrackList.isEmpty()) {
                    trackList.addAll(tempTrackList);
                }
            }
        }
        return trackList;
    }

    private List<MediaMetadataCompat> searchMusic(final ConcurrentMap<String, MediaMetadataCompat> musicListById, String metadataField, String query) {
        ArrayList<MediaMetadataCompat> result = new ArrayList<>();
        if (musicListById == null) {
            return result;
        }
        query = query.toLowerCase(Locale.US);
        for (MediaMetadataCompat track : musicListById.values()) {
            if (track.getString(metadataField).toLowerCase(Locale.US).contains(query)) {
                result.add(track);
            }
        }
        return result;
    }
}
