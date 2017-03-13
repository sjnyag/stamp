package com.sjn.taggingplayer.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.controller.TagController;
import com.sjn.taggingplayer.ui.custom.ToggleTextView;
import com.sjn.taggingplayer.ui.custom.WrapLayout;
import com.sjn.taggingplayer.ui.observer.TagEditStateObserver;
import com.sjn.taggingplayer.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

public class TagEditFragment extends Fragment {

    private static final String TAG = LogHelper.makeLogTag(TagEditFragment.class);
    private WrapLayout mTagListLayout;
    private Button mTagRegister;
    private EditText mTagInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tag_edit, container, false);
        mTagListLayout = (WrapLayout) rootView.findViewById(R.id.tag_list_layout);
        mTagRegister = (Button) rootView.findViewById(R.id.tag_register);
        mTagInput = (EditText) rootView.findViewById(R.id.tag_input);
        mTagRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context == null) {
                    return;
                }
                String tag = mTagInput.getText().toString();
                TagController tagController = new TagController(context);
                tagController.register(tag);
                drawTagList(context, tagController.findAll());
                TagEditStateObserver.getInstance().notifyAllTagChange(tag);
                mTagInput.setText("");
            }
        });
        drawTagList(getContext(), new TagController(getContext()).findAll());
        return rootView;
    }

    private void drawTagList(Context context, List<String> tagList) {
        mTagListLayout.removeAllViews();
        for (String tagName : tagList) {
            ToggleTextView text = (ToggleTextView) LayoutInflater.from(context).inflate(R.layout.text_view_select_tag, null);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notifySelectedTagListChange();
                }
            });
            text.setText(tagName);
            mTagListLayout.addView(text);
        }
    }

    private void notifySelectedTagListChange() {
        List<String> tagList = new ArrayList<>();
        for (int i = 0; i < mTagListLayout.getChildCount(); i++) {
            ToggleTextView toggleTextView = ((ToggleTextView) mTagListLayout.getChildAt(i));
            if (toggleTextView.isBooleanValue()) {
                tagList.add(toggleTextView.getText().toString());
            }
        }
        TagEditStateObserver.getInstance().notifySelectedTagListChange(tagList);
    }
}