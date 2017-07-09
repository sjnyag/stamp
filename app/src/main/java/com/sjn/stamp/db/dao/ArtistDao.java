package com.sjn.stamp.db.dao;

import com.sjn.stamp.db.Artist;

import io.realm.Realm;

public class ArtistDao extends BaseDao {

    private static ArtistDao sInstance;

    public static ArtistDao getInstance() {
        if (sInstance == null) {
            sInstance = new ArtistDao();
        }
        return sInstance;
    }

    public Artist findById(Realm realm, long id) {
        return realm.where(Artist.class).equalTo("mId", id).findFirst();
    }

    public Artist findOrCreate(Realm realm, Artist rawArtist) {
        Artist artist = realm.where(Artist.class).equalTo("mName", rawArtist.getName()).findFirst();
        if (artist == null) {
            rawArtist.setId(getAutoIncrementId(realm, Artist.class));
            artist = realm.copyToRealm(rawArtist);
        }
        return artist;
    }

    public Artist newStandalone() {
        return new Artist();
    }
}