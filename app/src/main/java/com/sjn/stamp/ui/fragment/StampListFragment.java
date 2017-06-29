package com.sjn.stamp.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sjnyag.AnimationWrapLayout;
import com.sjn.stamp.ui.custom.ToggleTextView;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.R;
import com.sjn.stamp.utils.LogHelper;

import java.util.List;

public class StampListFragment extends Fragment implements StampEditStateObserver.Listener {

    private static final String TAG = LogHelper.makeLogTag(StampListFragment.class);
    private AnimationWrapLayout mStampListLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stamp_list, container, false);
        mStampListLayout = (AnimationWrapLayout) rootView.findViewById(R.id.selected_stamp_list_layout);
        return rootView;
    }

    @Override
    public void onSelectedStampChange(List<String> selectedStampList) {
        Context context = getContext();
        if (mStampListLayout != null && context != null) {
            mStampListLayout.removeAllViews();
            for (String stampName : selectedStampList) {
                ToggleTextView text = (ToggleTextView) LayoutInflater.from(context).inflate(R.layout.text_view_select_stamp, null);
                text.setText(stampName);
                mStampListLayout.addView(text);
            }
        }
    }

    @Override
    public void onNewStampCreated(String stamp) {

    }

    @Override
    public void onStateChange(StampEditStateObserver.State state) {

    }

    @Override
    public void onStart() {
        super.onStart();
        StampEditStateObserver.getInstance().addListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        StampEditStateObserver.getInstance().removeListener(this);
    }

}