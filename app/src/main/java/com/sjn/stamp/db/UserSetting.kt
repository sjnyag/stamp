package com.sjn.stamp.db

import com.sjn.stamp.constant.RepeatState
import com.sjn.stamp.constant.ShuffleState
import com.sjn.stamp.utils.MediaIDHelper
import io.realm.RealmObject

open class UserSetting(
        var isAutoLogin: Boolean = false,
        var repeatState: Int = RepeatState.getDefault().no,
        var shuffleState: Int = ShuffleState.getDefault().no,
        var queueIdentifyMediaId: String = MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL,
        var lastMusicId: String? = null,
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

    fun fetchRepeatState(): RepeatState? {
        return RepeatState.of(repeatState)
    }

    fun applyShuffleState(shuffleState: ShuffleState) {
        this.shuffleState = shuffleState.no
    }

    fun fetchShuffleStateValue(): ShuffleState? {
        return ShuffleState.of(shuffleState)
    }

}
