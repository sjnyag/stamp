package com.sjn.stamp.controller;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.constant.CategoryType;
import com.sjn.stamp.db.CategoryStamp;
import com.sjn.stamp.db.Song;
import com.sjn.stamp.db.SongStamp;
import com.sjn.stamp.db.dao.CategoryStampDao;
import com.sjn.stamp.db.dao.SongDao;
import com.sjn.stamp.db.dao.SongStampDao;
import com.sjn.stamp.media.provider.ProviderType;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.MediaRetrieveHelper;
import com.sjn.stamp.utils.RealmHelper;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class SongController {

    private static final String TAG = LogHelper.makeLogTag(SongController.class);
    private Context mContext;
    private CategoryStampDao mCategoryStampDao;
    private SongDao mSongDao;
    private SongStampDao mSongStampDao;

    public SongController(Context context) {
        mContext = context;
        mSongDao = SongDao.getInstance();
        mSongStampDao = SongStampDao.getInstance();
        mCategoryStampDao = CategoryStampDao.getInstance();
    }

    public void registerStampList(List<String> stampNameList, String mediaId) {
        if (MediaIDHelper.isTrack(mediaId)) {
            registerSongStampList(
                    stampNameList,
                    MediaRetrieveHelper.findByMusicId(
                            mContext,
                            Long.valueOf(MediaIDHelper.extractMusicIDFromMediaID(mediaId)),
                            new MediaRetrieveHelper.PermissionRequiredCallback() {
                                @Override
                                public void onPermissionRequired() {
                                }
                            }
                    ));
        } else {
            String[] hierarchy = MediaIDHelper.getHierarchy(mediaId);
            if (hierarchy.length <= 1) {
                return;
            }
            registerCategoryStampList(stampNameList, ProviderType.of(hierarchy[0]).getCategoryType(), hierarchy[1]);
        }
        StampController stampController = new StampController(mContext);
        stampController.notifyStampChange();
    }

    public List<String> findStampsByMediaId(String mediaId) {
        if (MediaIDHelper.isTrack(mediaId)) {
            return findSongStampList(mediaId);
        } else {
            String[] hierarchy = MediaIDHelper.getHierarchy(mediaId);
            if (hierarchy.length <= 1) {
                return new ArrayList<>();
            }
            return findCategoryStampList(ProviderType.of(hierarchy[0]).getCategoryType(), hierarchy[1]);
        }
    }

    public List<String> findStampsByMusicId(String musicId) {
        return findSongStampListByMusicId(musicId);
    }

    public void removeStamp(String stampName, String mediaId) {
        if (MediaIDHelper.isTrack(mediaId)) {
            removeSongStamp(stampName, mediaId);
        } else {
            String[] hierarchy = MediaIDHelper.getHierarchy(mediaId);
            if (hierarchy.length <= 1) {
                return;
            }
            removeCategoryStamp(stampName, ProviderType.of(hierarchy[0]).getCategoryType(), hierarchy[1]);
        }
        StampController stampController = new StampController(mContext);
        stampController.notifyStampChange();
    }

    private void removeCategoryStamp(String stampName, CategoryType categoryType, String categoryValue) {
        Realm realm = RealmHelper.getRealmInstance();
        mCategoryStampDao.remove(realm, stampName, categoryType, categoryValue);
        realm.close();
    }

    private void removeSongStamp(String stampName, String mediaId) {
        //TODO: cache
        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
        Realm realm = RealmHelper.getRealmInstance();
        Song song = mSongDao.findByMusicId(realm, musicId);
        if (song != null) {
            for (SongStamp songStamp : song.getSongStampList()) {
                if (songStamp.getName().equals(stampName)) {
                    realm.beginTransaction();
                    songStamp.getSongList().remove(song);
                    song.getSongStampList().remove(songStamp);
                    realm.commitTransaction();
                    break;
                }
            }
        }
        realm.close();
    }

    private List<String> findCategoryStampList(CategoryType categoryType, String categoryValue) {
        List<String> stampList = new ArrayList<>();
        Realm realm = RealmHelper.getRealmInstance();
        for (CategoryStamp categoryStamp : mCategoryStampDao.findCategoryStampList(realm, categoryType, categoryValue)) {
            stampList.add(categoryStamp.getName());
        }
        realm.close();
        return stampList;
    }

    private List<String> findSongStampList(String mediaId) {
        //TODO: cache
        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
        return findSongStampListByMusicId(musicId);
    }

    private List<String> findSongStampListByMusicId(String musicId) {
        List<String> stampList = new ArrayList<>();
        Realm realm = RealmHelper.getRealmInstance();
        Song song = mSongDao.findByMusicId(realm, musicId);
        if (song != null) {
            for (SongStamp songStamp : song.getSongStampList()) {
                stampList.add(songStamp.getName());
            }
        }
        realm.close();
        return stampList;
    }

    private void registerCategoryStampList(List<String> stampNameList, CategoryType categoryType, String categoryValue) {
        if (categoryType == null) {
            return;
        }
        Realm realm = RealmHelper.getRealmInstance();
        for (String stampName : stampNameList) {
            mCategoryStampDao.save(realm, stampName, categoryType, categoryValue);
        }
        realm.close();
    }

    private void registerSongStampList(List<String> stampNameList, MediaMetadataCompat track) {
        if (track == null) {
            return;
        }
        Song song = SongDao.getInstance().newStandalone();
        song.parseMetadata(track);
        Realm realm = RealmHelper.getRealmInstance();
        for (String stampName : stampNameList) {
            SongStamp songStamp = mSongStampDao.newStandalone();
            songStamp.setName(stampName);
            songStamp.setSystem(false);
            mSongStampDao.saveOrAdd(realm, songStamp, song);
        }
        realm.close();
    }

}
