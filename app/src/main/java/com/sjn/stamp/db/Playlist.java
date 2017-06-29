package com.sjn.stamp.db;

import android.support.v4.media.MediaMetadataCompat;

import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
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
public class Playlist extends RealmObject {

    @PrimaryKey
    public long mId;
    public String mName;
    public Boolean mActive;
    public Date mCreatedAt;
    public Date mUpdatedAt;
    public RealmList<PlaylistSong> mSongs;


    public static Playlist create(String name, List<MediaMetadataCompat> mediaList) {
        Playlist playlist = new Playlist();
        playlist.setSongs(createSong(mediaList));
        playlist.setName(name);
        return playlist;
    }

    private static RealmList<PlaylistSong> createSong(List<MediaMetadataCompat> mediaList) {
        RealmList<PlaylistSong> playlistSongList = new RealmList<>();
        for (MediaMetadataCompat media : mediaList) {
            PlaylistSong playlistSong = new PlaylistSong();
            playlistSong.setArtist(media.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            playlistSong.setTitle(media.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            playlistSongList.add(playlistSong);
        }
        return playlistSongList;
    }
}
