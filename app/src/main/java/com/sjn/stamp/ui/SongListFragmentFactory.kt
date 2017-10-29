package com.sjn.stamp.ui

import com.sjn.stamp.media.provider.ProviderType
import com.sjn.stamp.ui.fragment.media_list.MyStampListFragment
import com.sjn.stamp.ui.fragment.media_list.SongListFragment


object SongListFragmentFactory {
    fun create(mediaId: String): SongListFragment {
        val fragment = resolveFragment(mediaId)
        fragment.mediaId = mediaId
        return fragment
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