package com.sjn.stamp.model.dao

import com.sjn.stamp.model.Artist
import io.realm.Realm

object ArtistDao : BaseDao() {

    fun findOrCreate(realm: Realm, name: String, uri: String): Artist {
        var artist: Artist? = realm.where(Artist::class.java).equalTo("name", name).findFirst()
        if (artist == null) {
            realm.beginTransaction()
            artist = realm.createObject(Artist::class.java, CategoryStampDao.getAutoIncrementId(realm, Artist::class.java))
            artist.name = name
            artist.albumArtUri = uri
            realm.commitTransaction()
            return artist
        }
        return artist
    }

    fun newStandalone(name: String, uri: String): Artist = Artist(name = name, albumArtUri = uri)

}