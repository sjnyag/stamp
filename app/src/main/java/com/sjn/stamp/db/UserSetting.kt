package com.sjn.stamp.db

import com.sjn.stamp.constant.RepeatState
import com.sjn.stamp.constant.ShuffleState
import io.realm.RealmObject

open class UserSetting(
        var isAutoLogin: Boolean = false,
        var repeatState: Int = 0,
        var shuffleState: Int = 0,
        var queueIdentifyMediaId: String? = null,
        var lastMusicId: String? = null
) : RealmObject() {

    fun applyRepeatState(repeatState: RepeatState) {
        this.repeatState = repeatState.no
    }

    fun fetchRepeatState(): RepeatState {
        return RepeatState.of(repeatState)
    }

    fun applyShuffleState(shuffleState: ShuffleState) {
        this.shuffleState = shuffleState.no
    }

    fun fetchShuffleStateValue(): ShuffleState {
        return ShuffleState.of(shuffleState)
    }

}
