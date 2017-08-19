package com.sjn.stamp.db

import com.sjn.stamp.constant.RepeatState
import com.sjn.stamp.constant.ShuffleState
import com.sjn.stamp.utils.MediaIDHelper
import io.realm.RealmObject

@Suppress("unused")
open class UserSetting(
        var isAutoLogin: Boolean = false,
        private var repeatState: Int = RepeatState.getDefault().no,
        private var shuffleState: Int = ShuffleState.getDefault().no,
        var queueIdentifyMediaId: String = MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL,
        var lastMusicId: String = "",
        var stopOnAudioLostFocus: Boolean = false,
        var showAlbumArtOnLockScreen: Boolean = false,
        var autoPlayOnHeadsetConnected: Boolean = false,
        var alertSpeaker: Boolean = false,
        var newSongDays: Int = 30,
        var mostPlayedSongSize: Int = 30
) : RealmObject() {

    fun applyRepeatState(repeatState: RepeatState) {
        this.repeatState = repeatState.no
    }

    fun fetchRepeatState(): RepeatState? = RepeatState.of(repeatState)

    fun applyShuffleState(shuffleState: ShuffleState) {
        this.shuffleState = shuffleState.no
    }

    fun fetchShuffleStateValue(): ShuffleState? = ShuffleState.of(shuffleState)

}
