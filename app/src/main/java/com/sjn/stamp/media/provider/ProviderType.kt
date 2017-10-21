package com.sjn.stamp.media.provider

import android.content.Context
import android.content.res.Resources
import com.sjn.stamp.R
import com.sjn.stamp.constant.CategoryType
import com.sjn.stamp.media.provider.multiple.*
import com.sjn.stamp.media.provider.single.AllProvider
import com.sjn.stamp.media.provider.single.NewProvider
import com.sjn.stamp.media.provider.single.QueueProvider
import com.sjn.stamp.media.provider.single.TopSongProvider
import com.sjn.stamp.utils.MediaIDHelper

enum class ProviderType(internal val mKeyId: String, private val mEmptyMessageResourceId: Int) {
    ARTIST(MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST, R.string.no_items) {
        override fun newProvider(context: Context): ListProvider = ArtistListProvider(context)

        override val categoryType: CategoryType
            get() = CategoryType.ARTIST
    },
    ALL(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL, R.string.no_items) {
        override fun newProvider(context: Context): ListProvider = AllProvider(context)

        override val categoryType: CategoryType?
            get() = null
    },
    GENRE(MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE, R.string.no_items) {
        override fun newProvider(context: Context): ListProvider = GenreListProvider(context)

        override val categoryType: CategoryType
            get() = CategoryType.GENRE
    },
    MY_STAMP(MediaIDHelper.MEDIA_ID_MUSICS_BY_MY_STAMP, R.string.empty_message_my_stamp) {
        override fun newProvider(context: Context): ListProvider = MyStampListProvider(context)

        override val categoryType: CategoryType?
            get() = null
    },
    SMART_STAMP(MediaIDHelper.MEDIA_ID_MUSICS_BY_SMART_STAMP, R.string.empty_message_smart_stamp) {
        override fun newProvider(context: Context): ListProvider = SmartStampListProvider(context)

        override val categoryType: CategoryType?
            get() = null
    },
    PLAYLIST(MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST, R.string.empty_message_playlist) {
        override fun newProvider(context: Context): ListProvider = PlaylistProvider(context)

        override val categoryType: CategoryType?
            get() = null
    },
    ALBUM(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM, R.string.no_items) {
        override fun newProvider(context: Context): ListProvider = AlbumListProvider(context)

        override val categoryType: CategoryType
            get() = CategoryType.ALBUM
    },
    QUEUE(MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE, R.string.empty_message_queue) {
        override fun newProvider(context: Context): ListProvider = QueueProvider(context)

        override val categoryType: CategoryType?
            get() = null
    },
    NEW(MediaIDHelper.MEDIA_ID_MUSICS_BY_NEW, R.string.empty_message_new) {
        override fun newProvider(context: Context): ListProvider = NewProvider(context)

        override val categoryType: CategoryType?
            get() = null
    },
    MOST_PLAYED(MediaIDHelper.MEDIA_ID_MUSICS_BY_MOST_PLAYED, R.string.empty_message_most_played) {
        override fun newProvider(context: Context): ListProvider = TopSongProvider(context)

        override val categoryType: CategoryType?
            get() = null
    },
    RANKING(MediaIDHelper.MEDIA_ID_MUSICS_BY_RANKING, R.string.no_items) {
        override fun newProvider(context: Context): ListProvider? = null

        override val categoryType: CategoryType?
            get() = null
    };

    abstract fun newProvider(context: Context): ListProvider?

    abstract val categoryType: CategoryType?

    fun getEmptyMessage(resources: Resources): String = resources.getString(mEmptyMessageResourceId)

    companion object {

        fun of(value: String?): ProviderType? {
            if (value == null) {
                return null
            }
            return ProviderType.values().firstOrNull { value.startsWith(it.mKeyId) }
        }
    }
}
