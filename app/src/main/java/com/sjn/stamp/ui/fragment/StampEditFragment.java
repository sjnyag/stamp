package com.sjn.stamp.ui.fragment;


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
import com.sjn.stamp.controller.StampController;
import com.sjn.stamp.ui.DialogFacade;
import com.sjn.stamp.ui.custom.ToggleTextView;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.R;
import com.sjn.stamp.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

public class StampEditFragment extends Fragment {

    private static final String TAG = LogHelper.makeLogTag(StampEditFragment.class);
    private AnimationWrapLayout mStampListLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stamp_edit, container, false);
        mStampListLayout = (AnimationWrapLayout) rootView.findViewById(R.id.stamp_list_layout);
        final Button stampRegisterButton = (Button) rootView.findViewById(R.id.stamp_register);
        final EditText stampInput = (EditText) rootView.findViewById(R.id.stamp_input);
        stampRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context == null) {
                    return;
                }
                String stampName = stampInput.getText().toString();
                if (new StampController(context).register(stampName)) {
                    mStampListLayout.addViewWithAnimation(inflateStampView(context, stampName), 0);
                    StampEditStateObserver.getInstance().notifyAllStampChange(stampName);
                    stampInput.setText("");
                }
            }
        });
        drawStampList(getContext(), new StampController(getContext()).findAll());
        return rootView;
    }

    private void drawStampList(Context context, List<String> stampList) {
        mStampListLayout.removeAllViews();
        for (String stampName : stampList) {
            mStampListLayout.addView(inflateStampView(context, stampName));
        }
    }

    private ToggleTextView inflateStampView(Context context, String stampName) {
        ToggleTextView stampView = (ToggleTextView) LayoutInflater.from(context).inflate(R.layout.text_view_select_stamp, null);
        stampView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifySelectedStampListChange();
            }
        });
        stampView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                final Context context = getContext();
                final String stamp = ((ToggleTextView) view).getText().toString();
                if (context == null || stamp.isEmpty()) {
                    return false;
                }
                DialogFacade.createStampDeleteDialog(context, stamp, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case NEGATIVE:
                                return;
                            case POSITIVE:
                                StampController stampController = new StampController(context);
                                stampController.remove(stamp);
                                mStampListLayout.removeViewWithAnimation(view);
                                break;
                        }
                    }
                }).show();
                return true;
            }
        });
        stampView.setText(stampName);
        return stampView;
    }

    private void notifySelectedStampListChange() {
        List<String> stampList = new ArrayList<>();
        for (int i = 0; i < mStampListLayout.getChildCount(); i++) {
            ToggleTextView toggleTextView = ((ToggleTextView) mStampListLayout.getChildAt(i));
            if (toggleTextView.isBooleanValue()) {
                stampList.add(toggleTextView.getText().toString());
            }
        }
        StampEditStateObserver.getInstance().notifySelectedStampListChange(stampList);
    }
}