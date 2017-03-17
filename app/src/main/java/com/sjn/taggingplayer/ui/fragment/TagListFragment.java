package com.sjn.taggingplayer.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sjnyag.AnimationWrapLayout;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.ui.custom.ToggleTextView;
import com.sjn.taggingplayer.ui.observer.TagEditStateObserver;
import com.sjn.taggingplayer.utils.LogHelper;

import java.util.List;

public class TagListFragment extends Fragment implements TagEditStateObserver.Listener {

    private static final String TAG = LogHelper.makeLogTag(TagListFragment.class);
    private AnimationWrapLayout mTagListLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tag_list, container, false);
        mTagListLayout = (AnimationWrapLayout) rootView.findViewById(R.id.selected_tag_list_layout);
        return rootView;
    }

    @Override
    public void onSelectedTagChange(List<String> selectedTagList) {
        Context context = getContext();
        if (mTagListLayout != null && context != null) {
            mTagListLayout.removeAllViews();
            for (String tagName : selectedTagList) {
                ToggleTextView text = (ToggleTextView) LayoutInflater.from(context).inflate(R.layout.text_view_select_tag, null);
                text.setText(tagName);
                mTagListLayout.addView(text);
            }
        }
    }

    @Override
    public void onNetTagCreated(String tag) {

    }

    @Override
    public void onStart() {
        super.onStart();
        TagEditStateObserver.getInstance().addListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        TagEditStateObserver.getInstance().removeListener(this);
    }

}