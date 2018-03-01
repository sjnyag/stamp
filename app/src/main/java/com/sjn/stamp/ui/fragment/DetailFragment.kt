package com.sjn.stamp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sjn.stamp.ui.activity.DrawerActivity
import com.sjn.stamp.ui.fragment.media.SongListFragment
import com.sjn.stamp.utils.AlbumArtHelper
import com.sjn.stamp.utils.LogHelper

class DetailFragment : SongListFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            super.onCreateView(inflater, container, savedInstanceState)?.also {
                arguments?.also {
                    //                    it.getString("TITLE")?.let { activity?.title = it }
//                    view.findViewById<TextView>(R.id.textView)?.also { textView ->
//                        if (CompatibleHelper.hasLollipop()) textView.transitionName = it.getString("TRANS_TITLE")
//                        textView.text = it.getString("TITLE")
//                    }
                    if (activity is DrawerActivity) {
                        (activity as DrawerActivity).run {
                            updateAppbar(it.getString("IMAGE_TEXT"), { activity, imageView ->
                                AlbumArtHelper.loadAlbumArt(activity, imageView, it.getParcelable("IMAGE_BITMAP"), it.getString("IMAGE_TYPE"), it.getString("IMAGE_URL"), it.getString("IMAGE_TEXT"))
                            })
                        }

                    }
                }
            }

    companion object {

        private val TAG = LogHelper.makeLogTag(DetailFragment::class.java)
    }
}