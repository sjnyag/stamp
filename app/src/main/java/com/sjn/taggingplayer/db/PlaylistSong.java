package com.sjn.taggingplayer.db;

import java.util.Date;

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
public class PlaylistSong extends RealmObject {

    @PrimaryKey
    public long mId;
    public String mArtist;
    public String mTitle;
    public String mSongId;
    public Boolean mActive;
    public Date mCreatedAt;
    public Date mUpdatedAt;
    public Playlist mPlaylist;
}
