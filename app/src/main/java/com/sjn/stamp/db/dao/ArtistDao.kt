package com.sjn.stamp.db.dao

import com.sjn.stamp.db.Artist

import io.realm.Realm

object ArtistDao : BaseDao() {

    @Suppress("unused")
    fun findOrCreate(realm: Realm, rawArtist: Artist): Artist {
        var artist: Artist? = realm.where(Artist::class.java).equalTo("name", rawArtist.name).findFirst()
        if (artist == null) {
            rawArtist.id = getAutoIncrementId(realm, Artist::class.java)
            artist = realm.copyToRealm(rawArtist)
        }
        return artist!!
    }

    fun newStandalone(): Artist = Artist()

}