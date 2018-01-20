package com.sjn.stamp.media

import com.sjn.stamp.controller.UserSettingController
import com.sjn.stamp.model.constant.RepeatState
import com.sjn.stamp.model.constant.ShuffleState
import java.util.*

object CustomController {

    private val shuffleStateListenerSet = ArrayList<ShuffleStateListener>()
    private val repeatStateListenerSet = ArrayList<RepeatStateListener>()

    var shuffleState: ShuffleState = UserSettingController().shuffleState
        private set(state) {
            field = state
            for (shuffleStateListener in shuffleStateListenerSet) {
                shuffleStateListener.onShuffleStateChanged(state)
            }
            val userSettingController = UserSettingController()
            userSettingController.shuffleState = state
        }

    var repeatState: RepeatState = UserSettingController().repeatState
        private set(state) {
            field = state
            for (repeatStateListener in repeatStateListenerSet) {
                repeatStateListener.onRepeatStateChanged(state)
            }
            UserSettingController().repeatState = state
        }

    interface RepeatStateListener {
        fun onRepeatStateChanged(state: RepeatState)
    }

    interface ShuffleStateListener {
        fun onShuffleStateChanged(state: ShuffleState)
    }

    fun addShuffleStateListenerSet(listener: ShuffleStateListener) =
            shuffleStateListenerSet.add(listener)

    fun removeShuffleStateListenerSet(listener: ShuffleStateListener) =
            shuffleStateListenerSet.add(listener)

    fun addRepeatStateListenerSet(listener: RepeatStateListener) =
            repeatStateListenerSet.add(listener)

    fun removeRepeatStateListenerSet(listener: RepeatStateListener) =
            repeatStateListenerSet.add(listener)

    fun toggleRepeatState() {
        repeatState = repeatState.toggle()
    }

    fun toggleShuffleState() {
        shuffleState = shuffleState.toggle()
    }

}