package com.sjn.stamp.model.constant

enum class RecordType(val databaseValue: String) {
    PLAY("play"),
    SKIP("skip"),
    START("start"),
    COMPLETE("complete");

    val text: String
        get() = this.toString()

    companion object {
        fun of(databaseValue: String): RecordType? = RecordType.values().firstOrNull { it.databaseValue == databaseValue }
    }

}