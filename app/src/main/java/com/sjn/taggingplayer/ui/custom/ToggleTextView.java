package com.sjn.taggingplayer.ui.custom;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.sjn.taggingplayer.R;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


public class ToggleTextView extends TextView {

    @Accessors(prefix = "m")
    @Getter
    @Setter
    private boolean mBooleanValue = false;

    public ToggleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        changeViewBackground(mBooleanValue);
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBooleanValue = !mBooleanValue;
                changeViewBackground(mBooleanValue);
            }
        });
    }

    private void changeViewBackground(boolean booleanValue) {
        if (Build.VERSION.SDK_INT >= 16) {
            if (booleanValue) {
                this.setBackground(getResources().getDrawable(R.drawable.toggle_on_text));
            } else {
                this.setBackground(getResources().getDrawable(R.drawable.toggle_off_text));
            }
        } else {
            if (booleanValue) {
                this.setBackgroundDrawable(getResources().getDrawable(R.drawable.toggle_on_text));
            } else {
                this.setBackgroundDrawable(getResources().getDrawable(R.drawable.toggle_off_text));
            }
        }
    }

}