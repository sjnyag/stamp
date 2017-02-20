package com.sjn.taggingplayer.controller;

import android.content.Context;

import com.sjn.taggingplayer.db.dao.CategoryTagDao;
import com.sjn.taggingplayer.db.dao.SongDao;
import com.sjn.taggingplayer.db.dao.SongTagDao;
import com.sjn.taggingplayer.utils.LogHelper;

public class SongController {

    private static final String TAG = LogHelper.makeLogTag(SongController.class);
    private Context mContext;
    private CategoryTagDao mCategoryTagDao;
    private SongDao mSongDao;
    private SongTagDao mSongTagDao;

    public SongController(Context context) {
        mContext = context;
        mSongDao = SongDao.getInstance();
        mSongTagDao = SongTagDao.getInstance();
        mCategoryTagDao = CategoryTagDao.getInstance();
    }
/*TODO:
    public void registerTagList(List<String> tagNameList, String mediaId) {
        if (MediaIDHelper.isTrack(mediaId)) {
            registerSongTagList(tagNameList, MediaObserver.getInstance().findTrackByMediaId(mediaId));
        } else {
            String[] hierarchy = QueueHelper.parseMediaId(mediaId);
            if (hierarchy.length <= 1) {
                return;
            }
            registerCategoryTagList(tagNameList, ProviderType.of(hierarchy[0]).getCategoryType(), hierarchy[1]);
        }
        TagController.getInstance().notifyTagChange();
    }

    public List<String> findTagsByMediaId(String mediaId) {
        if (MediaIDHelper.isTrack(mediaId)) {
            return findSongTagList(mediaId);
        } else {
            String[] hierarchy = QueueHelper.parseMediaId(mediaId);
            if (hierarchy.length <= 1) {
                return new ArrayList<>();
            }
            return findCategoryTagList(ProviderType.of(hierarchy[0]).getCategoryType(), hierarchy[1]);
        }
    }

    public void removeTag(String tagName, String mediaId) {
        if (MediaIDHelper.isTrack(mediaId)) {
            removeSongTag(tagName, mediaId);
        } else {
            String[] hierarchy = QueueHelper.parseMediaId(mediaId);
            if (hierarchy.length <= 1) {
                return;
            }
            removeCategoryTag(tagName, ProviderType.of(hierarchy[0]).getCategoryType(), hierarchy[1]);
        }
        TagController.getInstance().notifyTagChange();
    }

    private void removeCategoryTag(String tagName, CategoryType categoryType, String categoryValue) {
        Realm realm = RealmHelper.getRealmInstance(mContext);
        mCategoryTagDao.remove(realm, tagName, categoryType, categoryValue);
        realm.close();
    }

    private void removeSongTag(String tagName, String mediaId) {
        //TODO: cache
        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
        Realm realm = RealmHelper.getRealmInstance(mContext);
        Song song = mSongDao.findByMusicId(realm, musicId);
        if (song != null) {
            for (SongTag songTag : song.getSongTagList()) {
                if (songTag.getName().equals(tagName)) {
                    realm.beginTransaction();
                    songTag.getSongList().remove(song);
                    song.getSongTagList().remove(songTag);
                    realm.commitTransaction();
                    break;
                }
            }
        }
        realm.close();
    }

    private List<String> findCategoryTagList(CategoryType categoryType, String categoryValue) {
        List<String> tagList = new ArrayList<>();
        Realm realm = RealmHelper.getRealmInstance(mContext);
        for (CategoryTag categoryTag : mCategoryTagDao.findCategoryTagList(realm, categoryType, categoryValue)) {
            tagList.add(categoryTag.getName());
        }
        realm.close();
        return tagList;
    }

    private List<String> findSongTagList(String mediaId) {
        //TODO: cache
        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
        List<String> tagList = new ArrayList<>();
        Realm realm = RealmHelper.getRealmInstance(mContext);
        Song song = mSongDao.findByMusicId(realm, musicId);
        if (song != null) {
            for (SongTag songTag : song.getSongTagList()) {
                tagList.add(songTag.getName());
            }
        }
        realm.close();
        return tagList;
    }

    private void registerCategoryTagList(List<String> tagNameList, CategoryType categoryType, String categoryValue) {
        if (categoryType == null) {
            return;
        }
        Realm realm = RealmHelper.getRealmInstance(mContext);
        for (String tagName : tagNameList) {
            mCategoryTagDao.save(realm, tagName, categoryType, categoryValue);
        }
        realm.close();
    }

    private void registerSongTagList(List<String> tagNameList, MediaMetadataCompat track) {
        Song song = SongDao.getInstance().newStandalone();
        song.parseMetadata(track);
        Realm realm = RealmHelper.getRealmInstance(mContext);
        for (String tagName : tagNameList) {
            SongTag songTag = mSongTagDao.newStandalone();
            songTag.setName(tagName);
            mSongTagDao.saveOrAdd(realm, songTag, song);
        }
        realm.close();
    }
    */

}
