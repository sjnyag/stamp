package com.sjn.stamp.db;

import android.content.res.Resources;
import android.media.MediaMetadata;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.R;
import com.sjn.stamp.db.dao.ArtistDao;
import com.sjn.stamp.media.source.MusicProviderSource;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
@AllArgsConstructor(suppressConstructorProperties = true)
@NoArgsConstructor
@Getter
@Setter
public class Song extends RealmObject implements Shareable {

    @PrimaryKey
    public long mId;
    @Index
    public String mMediaId;
    public String mTrackSource;
    @Index
    public String mAlbum;
    public Long mDuration;
    public String mGenre;
    public String mAlbumArtUri;
    @Index
    public String mTitle;
    public Long mTrackNumber;
    public Long mNumTracks;
    public String mDateAdded;
    public RealmList<SongStamp> mSongStampList;
    public Artist mArtist;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;

        Song song = (Song) o;

        if (mArtist != null ? !mArtist.getName().equals(song.mArtist.getName()) : song.mArtist != null)
            return false;
        return mTitle != null ? mTitle.equals(song.mTitle) : song.mTitle == null;

    }

    @Override
    public int hashCode() {
        int result = mArtist != null ? mArtist.getName().hashCode() : 0;
        result = 31 * result + (mTitle != null ? mTitle.hashCode() : 0);
        return result;
    }

    public void parseMetadata(MediaMetadataCompat metadata) {
        setMediaId(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
        //noinspection ResourceType
        setTrackSource(metadata.getString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE));
        setAlbum(getStringIfExists(metadata, MediaMetadata.METADATA_KEY_ALBUM));
        String artistName = getStringIfExists(metadata, MediaMetadata.METADATA_KEY_ARTIST);
        Artist artist = ArtistDao.getInstance().newStandalone();
        artist.setName(artistName);
        artist.setAlbumArtUri(getStringIfExists(metadata, MediaMetadata.METADATA_KEY_ALBUM_ART_URI));
        setArtist(artist);
        setDuration(getLongIfExists(metadata, MediaMetadata.METADATA_KEY_DURATION));
        setGenre(getStringIfExists(metadata, MediaMetadata.METADATA_KEY_GENRE));
        setAlbumArtUri(getStringIfExists(metadata, MediaMetadata.METADATA_KEY_ALBUM_ART_URI));
        setTitle(getStringIfExists(metadata, MediaMetadata.METADATA_KEY_TITLE));
        setTrackNumber(getLongIfExists(metadata, MediaMetadata.METADATA_KEY_TRACK_NUMBER));
        setNumTracks(getLongIfExists(metadata, MediaMetadata.METADATA_KEY_NUM_TRACKS));
        setDateAdded(getStringIfExists(metadata, MediaMetadata.METADATA_KEY_DATE));
    }

    public MediaMetadataCompat.Builder mediaMetadataCompatBuilder() {
        //noinspection ResourceType
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mMediaId)
                .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, mTrackSource)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mAlbum)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mArtist.getName())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mDuration)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, mGenre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, mAlbumArtUri)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mTitle)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, mTrackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, mTrackNumber)
                .putString(MediaMetadataCompat.METADATA_KEY_DATE, mDateAdded);
    }

    public MediaMetadataCompat buildMediaMetadataCompat() {
        return mediaMetadataCompatBuilder().build();
    }

    String getStringIfExists(MediaMetadataCompat metadata, String key) {
        if (metadata.containsKey(key)) {
            return metadata.getString(key);
        }
        return null;
    }

    Long getLongIfExists(MediaMetadataCompat metadata, String key) {
        if (metadata.containsKey(key)) {
            return metadata.getLong(key);
        }
        return null;
    }

    @Override
    public String share(Resources resources) {
        return resources.getString(R.string.share_song, mTitle, mArtist.getName());
    }
}