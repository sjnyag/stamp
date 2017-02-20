package com.sjn.taggingplayer.utils;

import com.sjn.taggingplayer.db.Artist;
import com.sjn.taggingplayer.db.DailySongHistory;
import com.sjn.taggingplayer.db.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AggregateHelper {

    public static List<String> sortAndSublist(List<DailySongHistory> dailySongHistoryList, int threshold) {
        List<String> songList = new ArrayList<>();
        int i = 1;
        for (Map.Entry<Song, Integer> entry : getSortedSongMap(dailySongHistoryList).entrySet()) {
            songList.add(i + "位 " + entry.getValue() + "回 " + entry.getKey().getTitle() + "/" + entry.getKey().getArtist());
            i++;
            if (i > threshold) {
                break;
            }
        }
        return songList;
    }

    public static Artist getMostPlayedArtist(List<DailySongHistory> dailySongHistoryList) {
        Map<String, Integer> artistMap = getSortedArtistMap(dailySongHistoryList);
        if (artistMap != null && !artistMap.isEmpty()) {
            return createArtist(artistMap.entrySet().iterator().next().getKey(), dailySongHistoryList);
        }
        return null;
    }

    public static Song getMostPlayedSong(List<DailySongHistory> dailySongHistoryList) {
        Map<Song, Integer> songMap = getSortedSongMap(dailySongHistoryList);
        if (songMap != null && !songMap.isEmpty()) {
            return songMap.entrySet().iterator().next().getKey();
        }
        return null;
    }

    private static Artist createArtist(String name, List<DailySongHistory> dailySongHistoryList) {
        return new Artist(name, findArtistArt(dailySongHistoryList, name));
    }

    private static String findArtistArt(List<DailySongHistory> dailySongHistoryList, String artist) {
        if (artist == null) {
            return null;
        }
        for (Map.Entry<Song, Integer> entry : getSortedSongMap(dailySongHistoryList).entrySet()) {
            if (artist.equals(entry.getKey().getArtist())) {
                return entry.getKey().getAlbumArtUri();
            }
        }
        return null;
    }

    private static Map<String, Integer> getSortedArtistMap(List<DailySongHistory> dailySongHistoryList) {
        Map<String, Integer> artistMap = new HashMap<>();
        for (DailySongHistory dailySongHistory : dailySongHistoryList) {
            String artist = dailySongHistory.getSong().getArtist();
            if (artistMap.containsKey(artist)) {
                artistMap.put(artist, artistMap.get(artist) + dailySongHistory.getPlayCount());
            } else {
                artistMap.put(artist, dailySongHistory.getPlayCount());
            }
        }
        return JavaHelper.sortByValue(artistMap, true);
    }

    private static Map<Song, Integer> getSortedSongMap(List<DailySongHistory> dailySongHistoryList) {
        Map<Song, Integer> songMap = new HashMap<>();
        for (DailySongHistory dailySongHistory : dailySongHistoryList) {
            Song song = dailySongHistory.getSong();
            if (songMap.containsKey(song)) {
                songMap.put(song, songMap.get(song) + dailySongHistory.getPlayCount());
            } else {
                songMap.put(song, dailySongHistory.getPlayCount());
            }
        }
        return JavaHelper.sortByValue(songMap, true);
    }
}
