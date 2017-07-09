package com.sjn.stamp.db;

import android.content.res.Resources;

import com.sjn.stamp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
    Map<Song, Integer> mSongCountMap;

    @Override
    public String tweet(Resources resources) {
        if (mArtist == null) {
            return "";
        }
        return resources.getString(R.string.tweet_ranked, mPlayCount, mArtist.getName());
    }

    public Song mostPlayedSong() {
        if (mSongCountMap == null || mSongCountMap.isEmpty()) {
            return null;
        }
        List<RankedSong> rankedSongList = new ArrayList<>();
        for (Map.Entry<Song, Integer> entry : mSongCountMap.entrySet()) {
            rankedSongList.add(new RankedSong(entry.getValue(), entry.getKey()));
        }
        Collections.sort(rankedSongList, new Comparator<RankedSong>() {
            @Override
            public int compare(RankedSong t1, RankedSong t2) {
                return t2.getPlayCount() - t1.getPlayCount();
            }
        });
        return rankedSongList.get(0).getSong();
    }
}
