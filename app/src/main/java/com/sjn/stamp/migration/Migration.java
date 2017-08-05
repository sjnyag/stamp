package com.sjn.stamp.migration;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class Migration implements RealmMigration {
    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();
        if (oldVersion == 0) {
            RealmObjectSchema artistSchema = schema.create("Artist")
                    .addField("mId", long.class, FieldAttribute.PRIMARY_KEY)
                    .addField("mName", String.class, FieldAttribute.INDEXED)
                    .addField("mAlbumArtUri", String.class);

            RealmObjectSchema songSchema = schema.get("Song");

            songSchema.addRealmObjectField("mArtist_new", artistSchema)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("mArtist_new", findOrCreateArtist(obj.getString("mArtist"), obj.getString("mAlbumArtUri")));
                        }

                        DynamicRealmObject findOrCreateArtist(String name, String artUrl) {
                            DynamicRealmObject artist = realm.where("Artist").equalTo("mName", name).findFirst();
                            if (artist == null) {
                                artist = realm.createObject("Artist", getAutoIncrementId(realm, "Artist"));
                                artist.setString("mName", name);
                                artist.setString("mAlbumArtUri", artUrl);
                            }
                            return artist;
                        }

                        Integer getAutoIncrementId(DynamicRealm realm, String clazz) {
                            Number maxId = realm.where(clazz).max("mId");
                            if (maxId != null) {
                                return maxId.intValue() + 1;
                            }
                            return 1;
                        }
                    })
                    .removeField("mArtist")
                    .renameField("mArtist_new", "mArtist");
            oldVersion++;
        }
        if (oldVersion == 1) {
            schema.get("Artist")
                    .renameField("mId", "id")
                    .renameField("mName", "name")
                    .renameField("mAlbumArtUri", "albumArtUri");
            schema.get("CategoryStamp")
                    .renameField("mId", "id")
                    .renameField("mName", "name")
                    .renameField("mValue", "value")
                    .renameField("mType", "type");
            schema.get("Device")
                    .renameField("mId", "id")
                    .renameField("mModel", "model")
                    .renameField("mOs", "os");
            schema.get("Playlist")
                    .renameField("mId", "id")
                    .renameField("mName", "name")
                    .renameField("mActive", "active")
                    .renameField("mCreatedAt", "createdAt")
                    .renameField("mUpdatedAt", "updatedAt")
                    .renameField("mSongs", "songs");
            schema.get("PlaylistSong")
                    .renameField("mId", "id")
                    .renameField("mArtist", "artist")
                    .renameField("mTitle", "title")
                    .renameField("mSongId", "songId")
                    .renameField("mActive", "active")
                    .renameField("mCreatedAt", "createdAt")
                    .renameField("mUpdatedAt", "updatedAt")
                    .renameField("mPlaylist", "playlist");
            schema.get("Song")
                    .renameField("mId", "id")
                    .renameField("mMediaId", "mediaId")
                    .renameField("mTrackSource", "trackSource")
                    .renameField("mAlbum", "album")
                    .renameField("mDuration", "duration")
                    .renameField("mGenre", "genre")
                    .renameField("mAlbumArtUri", "albumArtUri")
                    .renameField("mTitle", "title")
                    .renameField("mTrackNumber", "trackNumber")
                    .renameField("mNumTracks", "numTracks")
                    .renameField("mDateAdded", "dateAdded")
                    .renameField("mSongStampList", "songStampList")
                    .renameField("mArtist", "artist");
            schema.get("SongHistory")
                    .renameField("mId", "id")
                    .renameField("mSong", "song")
                    .renameField("mRecordedAt", "recordedAt")
                    .renameField("mRecordType", "recordType")
                    .renameField("mDevice", "device")
                    .renameField("mCount", "count")
                    .renameField("mLatitude", "latitude")
                    .renameField("mLongitude", "longitude")
                    .renameField("mAccuracy", "accuracy")
                    .renameField("mAltitude", "altitude");
            schema.get("SongStamp")
                    .renameField("mId", "id")
                    .renameField("mName", "name")
                    .renameField("mIsSystem", "isSystem")
                    .renameField("mSongList", "songList");
            schema.get("TotalSongHistory")
                    .renameField("mId", "id")
                    .renameField("mSong", "song")
                    .renameField("mPlayCount", "playCount")
                    .renameField("mSkipCount", "skipCount")
                    .renameField("mCompleteCount", "completeCount");
            schema.get("UserSetting")
                    .renameField("mIsAutoLogin", "isAutoLogin")
                    .renameField("mRepeatState", "repeatState")
                    .renameField("mShuffleState", "shuffleState")
                    .renameField("mQueueIdentifyMediaId", "queueIdentifyMediaId")
                    .renameField("mLastMusicId", "lastMusicId");
            oldVersion++;
        }
    }
}