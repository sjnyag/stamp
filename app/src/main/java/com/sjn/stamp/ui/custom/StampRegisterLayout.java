package com.sjn.stamp.ui.custom;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.sjn.stamp.R;
import com.sjn.stamp.controller.StampController;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.utils.LogHelper;

public class StampRegisterLayout extends RelativeLayout {

    private static final String TAG = LogHelper.makeLogTag(StampRegisterLayout.class);

    public StampRegisterLayout(Context context) {
        super(context);
    }

    public StampRegisterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        View root = LayoutInflater.from(context).inflate(R.layout.layout_stamp_register, this);
        final EditText stampInput = (EditText) root.findViewById(R.id.stamp_input);
        final Button stampRegisterButton = (Button) root.findViewById(R.id.stamp_register);
        stampRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context == null) {
                    return;
                }
                String stampName = stampInput.getText().toString();
                if (new StampController(context).register(stampName)) {
                    StampEditStateObserver.getInstance().notifyAllStampChange(stampName);
                    stampInput.setText("");
                }
            }
        });
    }

    public StampRegisterLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


}