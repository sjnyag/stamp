/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sjn.stamp.utils

import android.app.Activity
import android.support.v4.media.session.MediaControllerCompat
import android.text.TextUtils
import com.sjn.stamp.media.provider.ProviderType
import com.sjn.stamp.model.constant.CategoryType
import java.util.*

/**
 * Utility class to help on queue related tasks.
 */
object MediaIDHelper {

    // Media IDs used on browseable items of MediaBrowser
    const val MEDIA_ID_EMPTY_ROOT = "__EMPTY_ROOT__"
    const val MEDIA_ID_ROOT = "__ROOT__"
    const val MEDIA_ID_MUSICS_BY_GENRE = "__BY_GENRE__"
    const val MEDIA_ID_MUSICS_BY_SEARCH = "__BY_SEARCH__"
    const val MEDIA_ID_MUSICS_BY_ARTIST = "__BY_ARTIST__"
    const val MEDIA_ID_MUSICS_BY_ALBUM = "__BY_ALBUM__"
    const val MEDIA_ID_MUSICS_BY_ALL = "__BY_ALL__"
    const val MEDIA_ID_MUSICS_BY_QUEUE = "__BY_QUEUE__"
    const val MEDIA_ID_MUSICS_BY_MY_STAMP = "__BY_MY_STAMP__"
    const val MEDIA_ID_MUSICS_BY_SMART_STAMP = "__BY_SMART_STAMP__"
    const val MEDIA_ID_MUSICS_BY_PLAYLIST = "__BY_PLAYLIST__"
    const val MEDIA_ID_MUSICS_BY_PLAYLIST_LIST = "__BY_PLAYLIST__"
    const val MEDIA_ID_MUSICS_BY_NEW = "__BY_NEW__"
    const val MEDIA_ID_MUSICS_BY_MOST_PLAYED = "__BY_MOST_PLAYED__"
    const val MEDIA_ID_MUSICS_BY_DIRECT = "__BY_DIRECT__"
    const val MEDIA_ID_MUSICS_BY_TIMELINE = "__BY_TIMELINE__"
    const val MEDIA_ID_MUSICS_BY_RANKING = "__BY_RANKING__"

    private const val CATEGORY_SEPARATOR = '/'
    private const val LEAF_SEPARATOR = '|'

    /**
     * Create a String value that represents a playable or a browsable media.
     *
     *
     * Encode the media browseable categories, if any, and the unique music ID, if any,
     * into a single String mediaID.
     *
     *
     * MediaIDs are of the form <categoryType>/<categoryValue>|<musicUniqueId>, to make it easy
     * to find the category (like genre) that a music was selected from, so we
     * can correctly build the playing queue. This is specially useful when
     * one music can appear in more than one list, like "by genre -> genre_1"
     * and "by artist -> artist_1".
     *
     * @param musicID    Unique music ID for playable items, or null for browseable items.
     * @param categories hierarchy of categories representing this item's browsing parents
     * @return a hierarchy-aware media ID
    </musicUniqueId></categoryValue></categoryType> */
    fun createMediaID(musicID: String?, vararg categories: String): String =
            StringBuilder().apply {
                for (i in categories.indices) {
                    //if (!isValidCategory(categories[i])) {
                    //    throw new IllegalArgumentException("Invalid category: " + categories[i]);
                    //}
                    append(escape(categories[i]))
                    if (i < categories.size - 1) {
                        append(CATEGORY_SEPARATOR)
                    }
                }
                musicID?.let {
                    append(LEAF_SEPARATOR).append(escape(it))
                }
            }.toString()

    fun createDirectMediaId(musicID: String): String = createMediaID(musicID, MEDIA_ID_MUSICS_BY_DIRECT)

    private fun isValidCategory(category: String?): Boolean =
            category == null || category.indexOf(CATEGORY_SEPARATOR) < 0 && category.indexOf(LEAF_SEPARATOR) < 0


    fun resolveMusicId(musicIdOrMediaId: String): String? =
            if (MediaIDHelper.isTrack(musicIdOrMediaId)) MediaIDHelper.extractMusicIDFromMediaID(musicIdOrMediaId) else musicIdOrMediaId


    /**
     * Extracts unique musicID from the mediaID. mediaID is, by this sample's convention, a
     * concatenation of category (eg "by_genre"), categoryValue (eg "Classical") and unique
     * musicID. This is necessary so we know where the user selected the music from, when the music
     * exists in more than one music list, and thus we are able to correctly build the playing queue.
     *
     * @param mediaID that contains the musicID
     * @return musicID
     */
    fun extractMusicIDFromMediaID(mediaID: String?): String? {
        if (mediaID == null) {
            return null
        }
        val pos = mediaID.indexOf(LEAF_SEPARATOR)
        return if (pos >= 0) {
            mediaID.substring(pos + 1)
        } else null
    }

    /**
     * Extracts category and categoryValue from the mediaID. mediaID is, by this sample's
     * convention, a concatenation of category (eg "by_genre"), categoryValue (eg "Classical") and
     * mediaID. This is necessary so we know where the user selected the music from, when the music
     * exists in more than one music list, and thus we are able to correctly build the playing queue.
     *
     * @param mediaID that contains a category and categoryValue.
     */
    fun getHierarchy(mediaID: String): Array<String> {
        var result = mediaID
        val pos = mediaID.indexOf(LEAF_SEPARATOR)
        if (pos >= 0) {
            result = mediaID.substring(0, pos)
        }
        return result.split(CATEGORY_SEPARATOR.toString().toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    fun extractBrowseCategoryValueFromMediaID(mediaID: String): String? {
        val hierarchy = getHierarchy(mediaID)
        return if (hierarchy.size == 2) {
            hierarchy[1]
        } else null
    }

    fun isBrowseable(mediaID: String): Boolean {
        return mediaID.indexOf(LEAF_SEPARATOR) < 0
    }

    fun getParentMediaID(mediaID: String): String {
        val hierarchy = getHierarchy(mediaID)
        if (!isBrowseable(mediaID)) {
            return createMediaID(null, *hierarchy)
        }
        if (hierarchy.size <= 1) {
            return MEDIA_ID_ROOT
        }
        return createMediaID(null, *Arrays.copyOf(hierarchy, hierarchy.size - 1))
    }

    fun isMediaItemPlaying(activity: Activity, mediaId: String): Boolean {
        // Media item is considered to be playing or paused based on the controller's current media id
        MediaControllerCompat.getMediaController(activity)?.let { controller ->
            controller.metadata?.let { metadata ->
                metadata.description.mediaId?.let {
                    return TextUtils.equals(it, MediaIDHelper.extractMusicIDFromMediaID(mediaId))
                }
            }
        }
        return false
    }

    fun isTrack(mediaID: String): Boolean = !isBrowseable(mediaID)

    fun unescape(musicID: String): String {
        return musicID.replace("／".toRegex(), "/").replace("｜".toRegex(), "|")
    }

    fun escape(musicID: String): String {
        return musicID.replace("/".toRegex(), "／").replace("\\|".toRegex(), "｜")
    }

    fun getCategoryType(mediaId: String): CategoryType? = getProviderType(mediaId)?.categoryType

    fun getProviderType(mediaId: String): ProviderType? {
        val hierarchy = getHierarchy(mediaId)
        return if (hierarchy.isEmpty()) {
            null
        } else ProviderType.of(hierarchy[0])
    }

    fun isDirect(categoryType: String): Boolean = MEDIA_ID_MUSICS_BY_DIRECT == categoryType

}
