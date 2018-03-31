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
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils
import com.sjn.stamp.media.provider.MusicProvider
import java.util.*

/**
 * Utility class to help on queue related tasks.
 */
object QueueHelper {

    private val TAG = LogHelper.makeLogTag(QueueHelper::class.java)

    private const val RANDOM_QUEUE_SIZE = 10

    class QueueList : ArrayList<MediaSessionCompat.QueueItem>, Parcelable {

        constructor() : super()

        constructor(source: Parcel) {
            source.readTypedList(this, MediaSessionCompat.QueueItem.CREATOR)
        }

        override fun describeContents(): Int = 0

        override fun writeToParcel(parcel: Parcel, i: Int) {
            parcel.writeTypedList(this)
        }

        companion object {
            @JvmField
            @Suppress("unused")
            val CREATOR: Parcelable.Creator<QueueList> = object : Parcelable.Creator<QueueList> {
                override fun createFromParcel(source: Parcel): QueueList {
                    return QueueList(source)
                }

                override fun newArray(size: Int): Array<QueueList?> = arrayOfNulls(size)

            }
        }
    }

    fun createQueue(trackList: List<MediaMetadataCompat>?, category: String): QueueList {
        val queueList = QueueList()
        if (trackList == null) {
            return queueList
        }
        for ((count, track) in trackList.withIndex()) {
            val hierarchyAwareMediaID = MediaIDHelper.createMediaID(track.description.mediaId, category)
            queueList.add(MediaItemHelper.convertToQueueItem(track, hierarchyAwareMediaID, count.toLong()))
        }
        return queueList
    }

    fun getPlayingQueue(mediaId: String,
                        musicProvider: MusicProvider): List<MediaSessionCompat.QueueItem> {

        LogHelper.d(TAG, "getPlayingQueue mediaId: ", mediaId)
        // extract the browsing hierarchy from the media ID:
        val hierarchy = MediaIDHelper.getHierarchy(mediaId)
        LogHelper.d(TAG, "getPlayingQueue hierarchy.length: ", hierarchy.size)
        LogHelper.d(TAG, "getPlayingQueue hierarchy[0]: ", hierarchy[0])
        return if (hierarchy.size == 1) {
            if (MediaIDHelper.isDirect(hierarchy[0])) {
                convertToQueue(ArrayList(listOf(musicProvider.getMusicByMusicId(MediaIDHelper.extractMusicIDFromMediaID(mediaId)!!)!!)), hierarchy[0])
            } else convertToQueue(musicProvider.getMusicsHierarchy(hierarchy[0], null), hierarchy[0])
        } else convertToQueue(musicProvider.getMusicsHierarchy(hierarchy[0], hierarchy[1]), hierarchy[0], hierarchy[1])
/*TODO
        if (hierarchy.length != 2) {
            LogHelper.e(TAG, "Could not build a playing queue for this mediaId: ", mediaId);
            return null;
        }

        String categoryType = hierarchy[0];
        String categoryValue = hierarchy[1];
        LogHelper.d(TAG, "Creating playing queue for ", categoryType, ",  ", categoryValue);

        Iterable<MediaMetadataCompat> tracks = null;
        // This sample only supports genre and by_search category types.
        if (categoryType.equals(MEDIA_ID_MUSICS_BY_GENRE)) {
            //tracks = musicProvider.getMusicsByGenre(categoryValue);
        } else if (categoryType.equals(MEDIA_ID_MUSICS_BY_SEARCH)) {
            tracks = musicProvider.searchMusicBySongTitle(categoryValue);
        }

        if (tracks == null) {
            LogHelper.e(TAG, "Unrecognized category type: ", categoryType, " for media ", mediaId);
            return null;
        }

        return convertToQueue(tracks, hierarchy[0], hierarchy[1]);
        */
    }

    fun getPlayingQueueFromSearch(query: String,
                                  queryParams: Bundle, musicProvider: MusicProvider): List<MediaSessionCompat.QueueItem> {

        LogHelper.d(TAG, "Creating playing queue for musics from search: ", query,
                " params=", queryParams)

        val params = VoiceSearchParams(query, queryParams)

        LogHelper.d(TAG, "VoiceSearchParams: ", params)

        if (params.isAny) {
            // If isAny is true, we will play anything. This is app-dependent, and can be,
            // for example, favorite playlists, "I'm feeling lucky", most recent, etc.
            return getRandomQueue(musicProvider)
        }

        // To keep it simple for this example, we do unstructured searches on the
        // song title only. A real world application could search on other fields as well.
        var result: Iterable<MediaMetadataCompat>? = when {
            params.isAlbumFocus -> musicProvider.searchMusicByAlbum(params.album!!)
            params.isGenreFocus -> musicProvider.searchMusicByGenre(params.genre!!)
            params.isArtistFocus -> musicProvider.searchMusicByArtist(params.artist!!)
            params.isSongFocus -> musicProvider.searchMusicBySongTitle(params.song!!)
            else -> null
        }

        // If there was no results using media focus parameter, we do an unstructured query.
        // This is useful when the user is searching for something that looks like an artist
        // to Google, for example, but is not. For example, a user searching for Madonna on
        // a PodCast application wouldn't get results if we only looked at the
        // Artist (podcast author). Then, we can instead do an unstructured search.

        // If there was no results using media focus parameter, we do an unstructured query.
        // This is useful when the user is searching for something that looks like an artist
        // to Google, for example, but is not. For example, a user searching for Madonna on
        // a PodCast application wouldn't get results if we only looked at the
        // Artist (podcast author). Then, we can instead do an unstructured search.
        if (params.isUnstructured || result == null || !result.iterator().hasNext()) {
            // To keep it simple for this example, we do unstructured searches on the
            // song title only. A real world application could search on other fields as well.
            result = musicProvider.searchMusicBySongTitle(query)
        }

        return convertToQueue(result, MediaIDHelper.MEDIA_ID_MUSICS_BY_SEARCH, query)
    }


    fun getMusicIndexOnQueueByMusicId(queue: Iterable<MediaSessionCompat.QueueItem>?, musicId: String): Int =
            queue?.indexOfFirst { musicId == MediaIDHelper.extractMusicIDFromMediaID(it.description.mediaId!!) }
                    ?: -1

    fun getMusicIndexOnQueueByMediaId(queue: Iterable<MediaSessionCompat.QueueItem>?, mediaId: String?): Int =
            queue?.indexOfFirst { mediaId == it.description.mediaId } ?: -1

    fun getMusicIndexOnQueue(queue: Iterable<MediaSessionCompat.QueueItem>, queueId: Long): Int {
        return queue.indexOfFirst { queueId == it.queueId }
    }

    private fun convertToQueue(
            tracks: Iterable<MediaMetadataCompat>?, vararg categories: String): List<MediaSessionCompat.QueueItem> {
        val queue = ArrayList<MediaSessionCompat.QueueItem>()
        LogHelper.d(TAG, "convertToQueue Start")
        if (tracks == null) {
            return queue
        }
        for ((count, track) in tracks.withIndex()) {
            // We create a hierarchy-aware mediaID, so we know what the queue is about by looking
            // at the QueueItem media IDs.
            val hierarchyAwareMediaID = MediaIDHelper.createMediaID(track.description.mediaId, *categories)
            queue.add(MediaItemHelper.convertToQueueItem(track, hierarchyAwareMediaID, count.toLong()))
        }
        LogHelper.d(TAG, "convertToQueue End")
        return queue

    }

    /**
     * Create a random queue with at most [.RANDOM_QUEUE_SIZE] elements.
     *
     * @param musicProvider the provider used for fetching music.
     * @return list containing [MediaSessionCompat.QueueItem]'s
     */
    fun getRandomQueue(musicProvider: MusicProvider): List<MediaSessionCompat.QueueItem> {
        val result = ArrayList<MediaMetadataCompat>(RANDOM_QUEUE_SIZE)
        val shuffled = musicProvider.shuffledMusic
        for (metadata in shuffled) {
            if (result.size == RANDOM_QUEUE_SIZE) {
                break
            }
            result.add(metadata)
        }
        LogHelper.d(TAG, "getRandomQueue: result.size=", result.size)

        return convertToQueue(result, MediaIDHelper.MEDIA_ID_MUSICS_BY_SEARCH, "random")
    }

    fun isIndexPlayable(index: Int, queue: List<MediaSessionCompat.QueueItem>?): Boolean {
        return queue != null && index >= 0 && index < queue.size
    }

    /**
     * Determine if two queues contain identical media id's in order.
     *
     * @param list1 containing [MediaSessionCompat.QueueItem]'s
     * @param list2 containing [MediaSessionCompat.QueueItem]'s
     * @return boolean indicating whether the queue's match
     */
    fun equals(list1: List<MediaSessionCompat.QueueItem>?,
               list2: List<MediaSessionCompat.QueueItem>?): Boolean {
        if (list1 === list2) {
            return true
        }
        if (list1 == null || list2 == null) {
            return false
        }
        if (list1.size != list2.size) {
            return false
        }
        for (i in list1.indices) {
            if (list1[i].queueId != list2[i].queueId) {
                return false
            }
            if (!TextUtils.equals(list1[i].description.mediaId,
                            list2[i].description.mediaId)) {
                return false
            }
        }
        return true
    }

    fun isQueueItemPlaying(activity: Activity,
                           queueItem: MediaSessionCompat.QueueItem): Boolean {
        // Queue item is considered to be playing or paused based on both the controller's
        // current media id and the controller's active queue item id
        val controller = MediaControllerCompat.getMediaController(activity)
        if (controller != null && controller.playbackState != null) {
            val currentPlayingQueueId = controller.playbackState.activeQueueItemId
            val currentPlayingMediaId = controller.metadata.description
                    .mediaId
            val itemMusicId = MediaIDHelper.extractMusicIDFromMediaID(
                    queueItem.description.mediaId!!)
            if (queueItem.queueId == currentPlayingQueueId
                    && currentPlayingMediaId != null
                    && TextUtils.equals(currentPlayingMediaId, itemMusicId)) {
                return true
            }
        }
        return false
    }

    internal class VoiceSearchParams
    /**
     * Creates a simple object describing the search criteria from the query and extras.
     *
     * @param query  the query parameter from a voice search
     * @param extras the extras parameter from a voice search
     */
    (val query: String, extras: Bundle?) {
        var isAny: Boolean = false
        var isUnstructured: Boolean = false
        var isGenreFocus: Boolean = false
        var isArtistFocus: Boolean = false
        var isAlbumFocus: Boolean = false
        var isSongFocus: Boolean = false
        var genre: String? = null
        var artist: String? = null
        var album: String? = null
        var song: String? = null

        init {

            if (TextUtils.isEmpty(query)) {
                // A generic search like "Play music" sends an empty query
                isAny = true
            } else {
                if (extras == null) {
                    isUnstructured = true
                } else {
                    val genreKey: String = if (Build.VERSION.SDK_INT >= 21) {
                        MediaStore.EXTRA_MEDIA_GENRE
                    } else {
                        "android.intent.extra.genre"
                    }

                    val mediaFocus = extras.getString(MediaStore.EXTRA_MEDIA_FOCUS)
                    if (TextUtils.equals(mediaFocus, MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE)) {
                        // for a Genre focused search, only genre is set:
                        isGenreFocus = true
                        genre = extras.getString(genreKey)
                        if (TextUtils.isEmpty(genre)) {
                            // Because of a bug on the platform, genre is only sent as a query, not as
                            // the semantic-aware extras. This check makes it future-proof when the
                            // bug is fixed.
                            genre = query
                        }
                    } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)) {
                        // for an Artist focused search, both artist and genre are set:
                        isArtistFocus = true
                        genre = extras.getString(genreKey)
                        artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST)
                    } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)) {
                        // for an Album focused search, album, artist and genre are set:
                        isAlbumFocus = true
                        album = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM)
                        genre = extras.getString(genreKey)
                        artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST)
                    } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Media.ENTRY_CONTENT_TYPE)) {
                        // for a Song focused search, title, album, artist and genre are set:
                        isSongFocus = true
                        song = extras.getString(MediaStore.EXTRA_MEDIA_TITLE)
                        album = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM)
                        genre = extras.getString(genreKey)
                        artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST)
                    } else {
                        // If we don't know the focus, we treat it is an unstructured query:
                        isUnstructured = true
                    }
                }
            }
        }

        override fun toString(): String {
            return ("query=" + query
                    + " isAny=" + isAny
                    + " isUnstructured=" + isUnstructured
                    + " isGenreFocus=" + isGenreFocus
                    + " isArtistFocus=" + isArtistFocus
                    + " isAlbumFocus=" + isAlbumFocus
                    + " isSongFocus=" + isSongFocus
                    + " genre=" + genre
                    + " artist=" + artist
                    + " album=" + album
                    + " song=" + song)
        }

    }

}
