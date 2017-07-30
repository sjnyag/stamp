package com.sjn.stamp.media.provider;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

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
    ARTIST(MediaMetadataCompat.METADATA_KEY_ARTIST, MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST) {
        @Override
        public ListProvider newProvider(Context context) {
            return new ArtistListProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return CategoryType.ARTIST;
        }
    },
    ALL(null, MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL) {
        @Override
        public ListProvider newProvider(Context context) {
            return new AllProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return null;
        }
    },
    GENRE(MediaMetadataCompat.METADATA_KEY_GENRE, MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE) {
        @Override
        public ListProvider newProvider(Context context) {
            return new GenreListProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return CategoryType.GENRE;
        }
    },
    STAMP(MediaMetadataCompat.METADATA_KEY_GENRE, MediaIDHelper.MEDIA_ID_MUSICS_BY_STAMP) {
        @Override
        public ListProvider newProvider(Context context) {
            return new StampListProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return null;
        }
    },
    PLAYLIST(MediaMetadataCompat.METADATA_KEY_GENRE, MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST) {
        @Override
        public ListProvider newProvider(Context context) {
            return new PlaylistProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return null;
        }
    },
    ALBUM(MediaMetadataCompat.METADATA_KEY_ALBUM, MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM) {
        @Override
        public ListProvider newProvider(Context context) {
            return new AlbumListProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return CategoryType.ALBUM;
        }
    },
    QUEUE(MediaMetadataCompat.METADATA_KEY_GENRE, MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE) {
        @Override
        public ListProvider newProvider(Context context) {
            return new QueueProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return null;
        }
    },
    NEW(MediaMetadataCompat.METADATA_KEY_GENRE, MediaIDHelper.MEDIA_ID_MUSICS_BY_NEW) {
        @Override
        public ListProvider newProvider(Context context) {
            return new NewProvider(context);
        }

        @Override
        public CategoryType getCategoryType() {
            return null;
        }
    },
    TOP_SONG(MediaMetadataCompat.METADATA_KEY_GENRE, MediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_SONG) {
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

    ProviderType(String mediaKey, String keyId) {
        mMediaKey = mediaKey;
        mKeyId = keyId;
    }

    public abstract ListProvider newProvider(Context context);

    public abstract CategoryType getCategoryType();

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
