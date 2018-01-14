package com.sjn.stamp.media.source

import android.support.v4.media.MediaMetadataCompat

interface MusicProviderSource {

    operator fun iterator(): Iterator<MediaMetadataCompat>
}
