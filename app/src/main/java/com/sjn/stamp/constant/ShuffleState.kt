package com.sjn.stamp.constant

enum class ShuffleState(val no: Int) : QueueState {
    SHUFFLE(0),
    NONE(1);

    fun toggle(): ShuffleState? {
        val no = (this.no + 1) % ShuffleState.values().size
        return ShuffleState.of(no)
    }

    companion object {

        fun of(no: Int): ShuffleState? = ShuffleState.values().firstOrNull { it.no == no }

        fun getDefault(): ShuffleState = NONE
    }
}