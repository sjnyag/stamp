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
public class RankedArtist implements Tweetable {
    int mPlayCount;
    public Artist mArtist;

    @Override
    public String tweet(Resources resources) {
        if (mArtist == null) {
            return "";
        }
        return resources.getString(R.string.tweet_ranked, mPlayCount, mArtist.getName());
    }
}
