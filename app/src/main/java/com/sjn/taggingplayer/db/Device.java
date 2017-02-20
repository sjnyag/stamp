package com.sjn.taggingplayer.db;

import android.os.Build;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
@AllArgsConstructor(suppressConstructorProperties = true)
@NoArgsConstructor
@Getter
@Setter
public class Device extends RealmObject {
    private static final String OS = "android";

    @PrimaryKey
    public long mId;
    public String mModel;
    public String mOs;

    public void configure() {
        setOs(OS);
        setModel(Build.MODEL);
    }
}
