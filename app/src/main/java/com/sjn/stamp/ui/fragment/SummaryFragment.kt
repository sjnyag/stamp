package com.sjn.stamp.ui.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sjn.stamp.R
import com.sjn.stamp.utils.LogHelper

class SummaryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(SummaryFragment::class.java)
    }
}