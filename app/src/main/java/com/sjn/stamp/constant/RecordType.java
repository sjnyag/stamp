package com.sjn.stamp.constant;

public enum RecordType {
    PLAY("play"),
    SKIP("skip"),
    START("start"),
    COMPLETE("complete");

    final public String mValue;

    public String getValue() {
        return mValue;
    }

    RecordType(String value) {
        mValue = value;
    }

    public static RecordType of(String value) {
        for (RecordType recordType : RecordType.values()) {
            if (recordType.getValue().equals(value)) return recordType;
        }
        return null;
    }

    public String getText() {
        return this.toString();
    }

}