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


class DetailFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_detail, container, false).also { view ->
                arguments?.let {
                    it.getString("TITLE")?.let { activity?.title = it }
                    if (CompatibleHelper.hasLollipop()) {
                        view.findViewById<View>(R.id.textView).transitionName = it.getString("TRANS_TITLE")
                        view.findViewById<View>(R.id.listImage).transitionName = it.getString("TRANS_IMAGE")
                    }
                    view.findViewById<TextView>(R.id.textView).text = it.getString("TITLE")
                    view.findViewById<ImageView>(R.id.listImage).setImageBitmap(it.getParcelable("IMAGE"))
                }
            }

}