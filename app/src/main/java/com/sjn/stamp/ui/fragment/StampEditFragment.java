package com.sjn.stamp.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.gc.materialdesign.views.ButtonFloatSmall;
import com.github.sjnyag.AnimationWrapLayout;
import com.sjn.stamp.R;
import com.sjn.stamp.controller.StampController;
import com.sjn.stamp.ui.DialogFacade;
import com.sjn.stamp.ui.custom.StampRegisterLayout;
import com.sjn.stamp.ui.custom.ToggleTextView;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

public class StampEditFragment extends Fragment implements StampEditStateObserver.Listener {

    private static final String TAG = LogHelper.makeLogTag(StampEditFragment.class);
    private AnimationWrapLayout mStampListLayout;
    private ButtonFloatSmall mRegisterButton;
    private TextView mEmptyString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stamp_edit, container, false);
        mStampListLayout = (AnimationWrapLayout) rootView.findViewById(R.id.stamp_list_layout);
        mEmptyString = (TextView) rootView.findViewById(R.id.stamp_list_empty);

        final StampRegisterLayout stampRegisterLayout = new StampRegisterLayout(getActivity(), null);
        mRegisterButton = (ButtonFloatSmall) LayoutInflater.from(getContext()).inflate(R.layout.add_stamp_button, null);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getContext())
                        .title(R.string.dialog_stamp_register)
                        .customView(stampRegisterLayout, true)
                        .positiveText(R.string.dialog_close)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            }
                        })
                        .contentColorRes(android.R.color.white)
                        .backgroundColorRes(R.color.material_blue_grey_800)
                        .theme(Theme.DARK)
                        .show();
            }
        });
        final Button okButton = (Button) rootView.findViewById(R.id.button_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StampEditStateObserver.getInstance().notifyStateChange(StampEditStateObserver.State.STAMPING);
            }
        });
        drawStampList(getContext(), new StampController().findAll());
        return rootView;
    }

    private void drawStampList(Context context, List<String> stampList) {
        mStampListLayout.removeAllViews();
        mStampListLayout.addView(mRegisterButton);
        if (stampList == null || stampList.isEmpty()) {
            mEmptyString.setVisibility(View.VISIBLE);
            return;
        }
        mEmptyString.setVisibility(View.GONE);
        for (String stampName : stampList) {
            mStampListLayout.addView(inflateStampView(context, stampName));
        }
    }

    private void updateEmptyString(List<String> stampList) {
        if (stampList == null || stampList.isEmpty()) {
            mEmptyString.setVisibility(View.VISIBLE);
            return;
        }
        mEmptyString.setVisibility(View.GONE);
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
                                StampController stampController = new StampController();
                                stampController.remove(stamp);
                                mStampListLayout.removeViewWithAnimation(view);
                                updateEmptyString(new StampController().findAll());
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
            View view = mStampListLayout.getChildAt(i);
            if (view instanceof ToggleTextView) {
                ToggleTextView toggleTextView = ((ToggleTextView) view);
                if (toggleTextView.isBooleanValue()) {
                    stampList.add(toggleTextView.getText().toString());
                }
            }
        }
        StampEditStateObserver.getInstance().notifySelectedStampListChange(stampList);
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

    @Override
    public void onSelectedStampChange(List<String> selectedStampList) {

    }

    @Override
    public void onNewStampCreated(String stamp) {
        mStampListLayout.addViewWithAnimation(inflateStampView(getContext(), stamp), 1);
        updateEmptyString(new StampController().findAll());
    }

    @Override
    public void onStampStateChange(StampEditStateObserver.State state) {

    }
}