package com.sjn.stamp.media.provider;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.media.MediaMetadataCompat;

import com.sjn.stamp.R;
import com.sjn.stamp.constant.CategoryType;
import com.sjn.stamp.media.provider.multiple.GenreListProvider;
import com.sjn.stamp.media.provider.multiple.PlaylistProvider;
import com.sjn.stamp.media.provider.single.TopSongProvider;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.media.provider.multiple.AlbumListProvider;
import com.sjn.stamp.media.provider.multiple.ArtistListProvider;
import com.sjn.stamp.media.provider.multiple.StampListProvider;
import com.sjn.stamp.media.provider.single.AllProvider;
import com.sjn.stamp.media.provider.single.NewProvider;
import com.sjn.stamp.media.provider.single.QueueProvider;


public enum ProviderType {
    ARTIST(MediaMetadataCompat.METADATA_KEY_ARTIST, MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST, R.string.no_items) {
        @Override
        public ListProvider newProvider(Context context) {
            return new ArtistListProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return CategoryType.ARTIST;
        }
    },
    ALL(null, MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL, R.string.no_items) {
        @Override
        public ListProvider newProvider(Context context) {
            return new AllProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return null;
        }
    },
    GENRE(MediaMetadataCompat.METADATA_KEY_GENRE, MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE, R.string.no_items) {
        @Override
        public ListProvider newProvider(Context context) {
            return new GenreListProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return CategoryType.GENRE;
        }
    },
    STAMP(MediaMetadataCompat.METADATA_KEY_GENRE, MediaIDHelper.MEDIA_ID_MUSICS_BY_STAMP, R.string.no_items) {
        @Override
        public ListProvider newProvider(Context context) {
            return new StampListProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return null;
        }
    },
    PLAYLIST(MediaMetadataCompat.METADATA_KEY_GENRE, MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST, R.string.empty_message_playlist) {
        @Override
        public ListProvider newProvider(Context context) {
            return new PlaylistProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return null;
        }
    },
    ALBUM(MediaMetadataCompat.METADATA_KEY_ALBUM, MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM, R.string.no_items) {
        @Override
        public ListProvider newProvider(Context context) {
            return new AlbumListProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return CategoryType.ALBUM;
        }
    },
    QUEUE(MediaMetadataCompat.METADATA_KEY_GENRE, MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE, R.string.empty_message_queue) {
        @Override
        public ListProvider newProvider(Context context) {
            return new QueueProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return null;
        }
    },
    NEW(MediaMetadataCompat.METADATA_KEY_GENRE, MediaIDHelper.MEDIA_ID_MUSICS_BY_NEW, R.string.empty_message_new) {
        @Override
        public ListProvider newProvider(Context context) {
            return new NewProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return null;
        }
    },
    MOST_PLAYED(MediaMetadataCompat.METADATA_KEY_GENRE, MediaIDHelper.MEDIA_ID_MUSICS_BY_MOST_PLAYED, R.string.empty_message_most_played) {
        @Override
        public ListProvider newProvider(Context context) {
            return new TopSongProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return null;
        }
    },;
    final String mMediaKey;
    final String mKeyId;
    final int mEmptyMessageResourceId;

    ProviderType(String mediaKey, String keyId, int emptyMessageResourceId) {
        mMediaKey = mediaKey;
        mKeyId = keyId;
        mEmptyMessageResourceId = emptyMessageResourceId;
    }

    public abstract ListProvider newProvider(Context context);

    public abstract CategoryType getCategoryType();

    public String getEmptyMessage(Resources resources){
        return resources.getString(mEmptyMessageResourceId);
    }

    public static ProviderType of(String value) {
        if (value == null) {
            return null;
        }
        for (ProviderType providerType : ProviderType.values()) {
            if (value.startsWith(providerType.mKeyId)) return providerType;
        }
        return null;
    }
}
