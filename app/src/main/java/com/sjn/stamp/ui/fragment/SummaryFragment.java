package com.sjn.stamp.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sjnyag.AnimationWrapLayout;
import com.sjn.stamp.R;
import com.sjn.stamp.utils.LogHelper;

public class SummaryFragment extends Fragment {

    private static final String TAG = LogHelper.makeLogTag(SummaryFragment.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary, container, false);
        return rootView;
    }
}