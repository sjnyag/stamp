package com.sjn.stamp.db

import android.content.res.Resources
import com.sjn.stamp.R

class RankedSong(
        var playCount: Int = 0,
        var song: Song? = null
) : Shareable {
    override fun share(resources: Resources): String {
        if (song == null) {
            return ""
        }
        return resources.getString(R.string.share_ranked, playCount, song!!.share(resources))
    }
}
