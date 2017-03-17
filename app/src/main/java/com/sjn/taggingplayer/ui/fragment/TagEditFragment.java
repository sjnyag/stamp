package com.sjn.taggingplayer.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.sjnyag.AnimationWrapLayout;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.controller.TagController;
import com.sjn.taggingplayer.ui.DialogFacade;
import com.sjn.taggingplayer.ui.custom.ToggleTextView;
import com.sjn.taggingplayer.ui.observer.TagEditStateObserver;
import com.sjn.taggingplayer.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

public class TagEditFragment extends Fragment {

    private static final String TAG = LogHelper.makeLogTag(TagEditFragment.class);
    private AnimationWrapLayout mTagListLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tag_edit, container, false);
        mTagListLayout = (AnimationWrapLayout) rootView.findViewById(R.id.tag_list_layout);
        final Button tagRegisterButton = (Button) rootView.findViewById(R.id.tag_register);
        final EditText tagInput = (EditText) rootView.findViewById(R.id.tag_input);
        tagRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context == null) {
                    return;
                }
                String tagName = tagInput.getText().toString();
                if (new TagController(context).register(tagName)) {
                    mTagListLayout.addViewWithAnimation(inflateTagView(context, tagName), 0);
                    TagEditStateObserver.getInstance().notifyAllTagChange(tagName);
                    tagInput.setText("");
                }
            }
        });
        drawTagList(getContext(), new TagController(getContext()).findAll());
        return rootView;
    }

    private void drawTagList(Context context, List<String> tagList) {
        mTagListLayout.removeAllViews();
        for (String tagName : tagList) {
            mTagListLayout.addView(inflateTagView(context, tagName));
        }
    }

    private ToggleTextView inflateTagView(Context context, String tagName) {
        ToggleTextView tagView = (ToggleTextView) LayoutInflater.from(context).inflate(R.layout.text_view_select_tag, null);
        tagView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifySelectedTagListChange();
            }
        });
        tagView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                final Context context = getContext();
                final String tag = ((ToggleTextView) view).getText().toString();
                if (context == null || tag.isEmpty()) {
                    return false;
                }
                DialogFacade.createTagDeleteDialog(context, tag, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case NEGATIVE:
                                return;
                            case POSITIVE:
                                TagController tagController = new TagController(context);
                                tagController.remove(tag);
                                mTagListLayout.removeViewWithAnimation(view);
                                break;
                        }
                    }
                }).show();
                return true;
            }
        });
        tagView.setText(tagName);
        return tagView;
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