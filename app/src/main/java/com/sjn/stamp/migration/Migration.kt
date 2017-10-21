package com.sjn.stamp.migration

import com.sjn.stamp.utils.LogHelper
import io.realm.*

class Migration : RealmMigration {

    companion object {
        private val TAG = LogHelper.makeLogTag(Migration::class.java)
        val VERSION = 5
    }

    override fun migrate(realm: DynamicRealm, version: Long, newVersion: Long) {
        var oldVersion = version

        val schema = realm.schema
        if (oldVersion == 0L) {
            migrateTo1(realm, schema)
            oldVersion++
        }
        if (oldVersion == 1L) {
            migrateTo2(schema)
            oldVersion++
        }
        if (oldVersion == 2L) {
            migrateTo3(schema)
            oldVersion++
        }
        if (oldVersion == 3L) {
            migrateTo4(schema)
            oldVersion++
        }
        if (oldVersion == 4L) {
            migrateTo5(realm, schema)
            oldVersion++
        }
        if (oldVersion != newVersion) {
            LogHelper.w(TAG, "Realm migration might be failed.")
        }
    }

    private fun migrateTo1(realm: DynamicRealm, schema: RealmSchema) {
        val artistSchema = schema.create("Artist")
                .addField("mId", Long::class.javaPrimitiveType, FieldAttribute.PRIMARY_KEY)
                .addField("mName", String::class.java, FieldAttribute.INDEXED)
                .addField("mAlbumArtUri", String::class.java)

        val songSchema = schema.get("Song")

        songSchema.addRealmObjectField("mArtist_new", artistSchema)
                .transform(object : RealmObjectSchema.Function {
                    override fun apply(obj: DynamicRealmObject) {
                        obj.set("mArtist_new", findOrCreateArtist(obj.getString("mArtist"), obj.getString("mAlbumArtUri")))
                    }

                    internal fun findOrCreateArtist(name: String, artUrl: String): DynamicRealmObject {
                        var artist: DynamicRealmObject? = realm.where("Artist").equalTo("mName", name).findFirst()
                        if (artist == null) {
                            artist = realm.createObject("Artist", getAutoIncrementId(realm, "Artist"))
                            artist!!.setString("mName", name)
                            artist.setString("mAlbumArtUri", artUrl)
                        }
                        return artist
                    }

                    internal fun getAutoIncrementId(realm: DynamicRealm, clazz: String): Int {
                        val maxId = realm.where(clazz).max("mId")
                        if (maxId != null) {
                            return maxId.toInt() + 1
                        }
                        return 1
                    }
                })
                .removeField("mArtist")
                .renameField("mArtist_new", "mArtist")
    }

    private fun migrateTo2(schema: RealmSchema) {
        schema.get("Artist")
                .renameField("mId", "id")
                .renameField("mName", "name")
                .renameField("mAlbumArtUri", "albumArtUri")
        schema.get("CategoryStamp")
                .renameField("mId", "id")
                .renameField("mName", "name")
                .renameField("mValue", "value")
                .renameField("mType", "type")
        schema.get("Device")
                .renameField("mId", "id")
                .renameField("mModel", "model")
                .renameField("mOs", "os")
        schema.get("Playlist")
                .renameField("mId", "id")
                .renameField("mName", "name")
                .renameField("mActive", "active")
                .renameField("mCreatedAt", "createdAt")
                .renameField("mUpdatedAt", "updatedAt")
                .renameField("mSongs", "songs")
        schema.get("PlaylistSong")
                .renameField("mId", "id")
                .renameField("mArtist", "artist")
                .renameField("mTitle", "title")
                .renameField("mSongId", "songId")
                .renameField("mActive", "active")
                .renameField("mCreatedAt", "createdAt")
                .renameField("mUpdatedAt", "updatedAt")
                .renameField("mPlaylist", "playlist")
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
                .renameField("mArtist", "artist")
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
                .renameField("mAltitude", "altitude")
        schema.get("SongStamp")
                .renameField("mId", "id")
                .renameField("mName", "name")
                .renameField("mIsSystem", "isSystem")
                .renameField("mSongList", "songList")
        schema.get("TotalSongHistory")
                .renameField("mId", "id")
                .renameField("mSong", "song")
                .renameField("mPlayCount", "playCount")
                .renameField("mSkipCount", "skipCount")
                .renameField("mCompleteCount", "completeCount")
        schema.get("UserSetting")
                .renameField("mIsAutoLogin", "isAutoLogin")
                .renameField("mRepeatState", "repeatState")
                .renameField("mShuffleState", "shuffleState")
                .renameField("mQueueIdentifyMediaId", "queueIdentifyMediaId")
                .renameField("mLastMusicId", "lastMusicId")
    }

    private fun migrateTo3(schema: RealmSchema) {
        schema.get("UserSetting")
                .addField("stopOnAudioLostFocus", Boolean::class.java, FieldAttribute.REQUIRED)
                .transform({ obj -> obj.setBoolean("stopOnAudioLostFocus", false) })
                .addField("showAlbumArtOnLockScreen", Boolean::class.java, FieldAttribute.REQUIRED)
                .transform({ obj -> obj.setBoolean("showAlbumArtOnLockScreen", false) })
                .addField("autoPlayOnHeadsetConnected", Boolean::class.java, FieldAttribute.REQUIRED)
                .transform({ obj -> obj.setBoolean("autoPlayOnHeadsetConnected", false) })
                .addField("alertSpeaker", Boolean::class.java, FieldAttribute.REQUIRED)
                .transform({ obj -> obj.setBoolean("alertSpeaker", false) })
                .addField("newSongDays", Int::class.java, FieldAttribute.REQUIRED)
                .transform({ obj -> obj.setInt("newSongDays", 30) })
                .addField("mostPlayedSongSize", Int::class.java, FieldAttribute.REQUIRED)
                .transform({ obj -> obj.setInt("mostPlayedSongSize", 30) })
    }

    private fun migrateTo4(schema: RealmSchema) {
        schema.get("CategoryStamp")
                .addField("isSystem", Boolean::class.java, FieldAttribute.REQUIRED)
                .transform({ obj -> obj.setBoolean("isSystem", false) })
    }

    private fun migrateTo5(realm: DynamicRealm, schema: RealmSchema) {
        schema.get("Song")
                .addRealmObjectField("totalSongHistory", schema.get("TotalSongHistory"))
                .transform(object : RealmObjectSchema.Function {
                    override fun apply(obj: DynamicRealmObject) {
                        obj.set("totalSongHistory", findOrCreateArtist(obj))
                    }

                    internal fun findOrCreateArtist(obj: DynamicRealmObject): DynamicRealmObject {
                        var totalSongHistory: DynamicRealmObject? = realm.where("TotalSongHistory").equalTo("song.id", obj.getLong("id")).findFirst()
                        if (totalSongHistory == null) {
                            totalSongHistory = realm.createObject("TotalSongHistory", getAutoIncrementId(realm, "TotalSongHistory"))
                            totalSongHistory!!.set("song", obj)
                            totalSongHistory.setInt("playCount", 0)
                            totalSongHistory.setInt("playCount", 0)
                            totalSongHistory.setInt("playCount", 0)
                        }
                        return totalSongHistory
                    }

                    internal fun getAutoIncrementId(realm: DynamicRealm, clazz: String): Int {
                        val maxId = realm.where(clazz).max("id")
                        if (maxId != null) {
                            return maxId.toInt() + 1
                        }
                        return 1
                    }
                })
        schema.get("TotalSongHistory")
                .removeField("song")

    }
}