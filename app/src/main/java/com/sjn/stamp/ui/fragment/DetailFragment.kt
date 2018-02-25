package com.sjn.stamp.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.sjn.stamp.R
import com.sjn.stamp.utils.CompatibleHelper
import com.sjn.stamp.utils.ViewHelper

class DetailFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_detail, container, false).also { view ->
                arguments?.let {
                    it.getString("TITLE")?.let { activity?.title = it }
                    if (CompatibleHelper.hasLollipop()) {
                        view.findViewById<View>(R.id.textView).transitionName = it.getString("TRANS_TITLE")
                        view.findViewById<View>(R.id.image).transitionName = it.getString("TRANS_IMAGE")
                    }
                    view.findViewById<TextView>(R.id.textView).text = it.getString("TITLE")
                    activity?.let { activity ->
                        view.findViewById<ImageView>(R.id.image)?.let { view ->
                            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                            ViewHelper.loadAlbumArt(activity, view, it.getString("IMAGE_TYPE"), it.getString("IMAGE_URL"), it.getString("IMAGE_TEXT"))
                        }
                    }
                }
            }

}