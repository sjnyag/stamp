package com.sjn.stamp.constant

enum class RecordType(val value: String) {
    PLAY("play"),
    SKIP("skip"),
    START("start"),
    COMPLETE("complete");

    val text: String
        get() = this.toString()

    companion object {

        fun of(value: String): RecordType? {
            return RecordType.values().firstOrNull { it.value == value }
        }
    }

}