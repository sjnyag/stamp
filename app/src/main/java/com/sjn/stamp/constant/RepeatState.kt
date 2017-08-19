package com.sjn.stamp.constant

enum class RepeatState(val no: Int) : QueueState {
    ALL(0),
    ONE(1),
    NONE(2);

    fun toggle(): RepeatState {
        val no = (this.no + 1) % RepeatState.values().size
        return RepeatState.of(no)
    }

    companion object {

        fun of(no: Int): RepeatState = RepeatState.values().firstOrNull { it.no == no } ?: NONE

        fun getDefault(): RepeatState = RepeatState.NONE
    }
}
