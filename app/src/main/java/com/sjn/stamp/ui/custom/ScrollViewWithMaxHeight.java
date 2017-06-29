package com.sjn.stamp.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.sjn.stamp.R;

public class ScrollViewWithMaxHeight extends ScrollView {

    public static int WITHOUT_MAX_HEIGHT_VALUE = -1;

    private int maxHeight = WITHOUT_MAX_HEIGHT_VALUE;

    public ScrollViewWithMaxHeight(Context context) {
        super(context);
    }

    public ScrollViewWithMaxHeight(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ScrollViewWithMaxHeight,
                0, 0);

        try {
            setMaxHeight(a.getInt(R.styleable.ScrollViewWithMaxHeight_maxHeight, 0));
        } finally {
            a.recycle();
        }
    }

    public ScrollViewWithMaxHeight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
            if (maxHeight != WITHOUT_MAX_HEIGHT_VALUE
                    && heightSize > maxHeight) {
                heightSize = maxHeight;
            }
            if (0 <= heightSize && heightSize <= 1073741823) {
                heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(heightSize, View.MeasureSpec.AT_MOST);
            }
            getLayoutParams().height = heightSize;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }
}