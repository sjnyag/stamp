package com.sjn.stamp.ui

import com.sjn.stamp.media.provider.ProviderType
import com.sjn.stamp.ui.fragment.media_list.MyStampListFragment
import com.sjn.stamp.ui.fragment.media_list.SongListFragment


object SongListFragmentFactory {
    fun create(mediaId: String): SongListFragment =
            resolveFragment(mediaId).apply {
                this.mediaId = mediaId
            }

    private fun resolveFragment(mediaId: String): SongListFragment =
            when (ProviderType.of(mediaId)) {
                ProviderType.MY_STAMP -> {
                    MyStampListFragment()
                }
                else -> {
                    SongListFragment()
                }
            }
}