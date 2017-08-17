package com.sjn.stamp.db

import android.content.res.Resources
import com.sjn.stamp.R
import java.util.*

open class RankedArtist(
        var playCount: Int = 0,
        var artist: Artist? = null,
        private var songCountMap: Map<Song, Int>? = null
) : Shareable {

    override fun share(resources: Resources): String {
        if (artist == null) {
            return ""
        }
        return resources.getString(R.string.share_ranked, playCount, artist!!.name)
    }

    fun mostPlayedSong(): Song? {
        if (songCountMap == null || songCountMap!!.isEmpty()) {
            return null
        }
        val rankedSongList = ArrayList<RankedSong>()
        for ((key, value) in songCountMap!!) {
            rankedSongList.add(RankedSong(value, key))
        }
        Collections.sort(rankedSongList) { t1, t2 -> t2.playCount - t1.playCount }
        return rankedSongList[0].song
    }
}
