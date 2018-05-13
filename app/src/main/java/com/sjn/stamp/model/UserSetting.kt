package com.sjn.stamp.model

import com.sjn.stamp.utils.MediaIDHelper
import io.realm.RealmObject

@Suppress("unused")
open class UserSetting(
        var isAutoLogin: Boolean = false,
        var queueIdentifyMediaId: String = MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL,
        var lastMusicId: String = "",
        var stopOnAudioLostFocus: Boolean = false,
        var showAlbumArtOnLockScreen: Boolean = false,
        var autoPlayOnHeadsetConnected: Boolean = false,
        var alertSpeaker: Boolean = false,
        var newSongDays: Int = 30,
        var mostPlayedSongSize: Int = 30
) : RealmObject()
