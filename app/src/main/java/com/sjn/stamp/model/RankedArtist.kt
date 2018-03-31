package com.sjn.stamp.model

import android.content.res.Resources
import com.sjn.stamp.R
import java.util.*

open class RankedArtist(
        var playCount: Int,
        var artist: Artist,
        private var songCountMap: Map<Song, Int> = emptyMap()
) : Shareable {

    override fun share(resources: Resources): String = resources.getString(R.string.share_ranked, playCount, artist.name)

    fun mostPlayedSong(): Song? = orderedSongList()[0].song

    fun orderedSongList(): List<RankedSong> {
        val rankedSongList = ArrayList<RankedSong>()
        if (songCountMap.isEmpty()) {
            return rankedSongList
        }
        for ((key, value) in songCountMap) {
            rankedSongList.add(RankedSong(value, key))
        }
        rankedSongList.sortWith(Comparator { t1, t2 -> t2.playCount - t1.playCount })
        return rankedSongList
    }
}
