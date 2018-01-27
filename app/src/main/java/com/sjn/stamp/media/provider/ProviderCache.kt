package com.sjn.stamp.media.provider

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.media.MediaMetadataCompat
import com.sjn.stamp.utils.ParcelDiskCache
import java.io.IOException
import java.util.*

internal object ProviderCache {

    private const val SUB_DIR = "/media_provider"
    private const val CACHE_KEY = "media_map_cache"

    fun readCache(context: Context): HashMap<String, MediaMetadataCompat> {
        var mediaMapCache: MediaMapCache? = null
        try {
            val cache = ParcelDiskCache.open(context, MediaMapCache::class.java.classLoader, SUB_DIR, (1024 * 1024 * 10).toLong())
            mediaMapCache = cache[CACHE_KEY] as MediaMapCache
            cache.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return if (mediaMapCache == null) {
            HashMap()
        } else mediaMapCache.mMap
    }

    private data class MediaMapCache internal constructor(internal val mMap: HashMap<String, MediaMetadataCompat>) : Parcelable {

        override fun describeContents(): Int = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.run {
                writeInt(mMap.size)
                for ((key, value) in mMap) {
                    writeString(key)
                    writeParcelable(value, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
                }
            }

        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<MediaMapCache> = object : Parcelable.Creator<MediaMapCache> {
                override fun createFromParcel(source: Parcel): MediaMapCache {
                    val map = HashMap<String, MediaMetadataCompat>()
                    val size = source.readInt()
                    for (i in 0 until size) {
                        val key = source.readString()
                        val value = source.readParcelable<MediaMetadataCompat>(MediaMetadataCompat::class.java.classLoader)
                        map[key] = value
                    }
                    return MediaMapCache(map)
                }

                override fun newArray(size: Int): Array<MediaMapCache?> = arrayOfNulls(size)
            }
        }

    }


    fun saveCache(context: Context, map: HashMap<String, MediaMetadataCompat>) {
        val mediaMapCache = MediaMapCache(map)
        try {
            val cache = ParcelDiskCache.open(context, MediaMapCache::class.java.classLoader, SUB_DIR, (1024 * 1024 * 10).toLong())
            cache[CACHE_KEY] = mediaMapCache
            cache.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


}
