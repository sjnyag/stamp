package com.sjn.stamp.db;

import android.content.res.Resources;

import com.sjn.stamp.R;

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
public class RankedSong implements Shareable {
    int mPlayCount;
    public Song mSong;

    @Override
    public String share(Resources resources) {
        if (mSong == null) {
            return "";
        }
        return resources.getString(R.string.share_ranked, mPlayCount, mSong.share(resources));
    }
}
