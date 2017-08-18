package com.sjn.stamp.db

import android.content.res.Resources
import com.sjn.stamp.R

class RankedSong(
        var playCount: Int,
        var song: Song
) : Shareable {
    override fun share(resources: Resources): String = resources.getString(R.string.share_ranked, playCount, song.share(resources))
}
