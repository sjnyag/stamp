package com.sjn.taggingplayer.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.utils.LogHelper;

public class WrapLayout extends ViewGroup {
    private static final String TAG = LogHelper.makeLogTag(WrapLayout.class);
    private int mMarginWidth;
    private int mMarginHeight;

    private void readAttr(Context context, AttributeSet attrs) {
        TypedArray parameters = context.obtainStyledAttributes(attrs, R.styleable.WrapLayout);
        mMarginWidth = parameters.getLayoutDimension(R.styleable.WrapLayout_wrapLayoutMarginWidth, 4);
        mMarginHeight = parameters.getLayoutDimension(R.styleable.WrapLayout_wrapLayoutMarginHeight, 4);
    }

    public WrapLayout(Context context) {
        super(context);
    }

    public WrapLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttr(context, attrs);
    }

    public WrapLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        readAttr(context, attrs);
    }

    @Override
    public WrapLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new WrapLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = View.resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = View.resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        int childWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.UNSPECIFIED);
        int childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).measure(childWidthSpec, childHeightSpec);
        }

        int count = this.getChildCount();
        int rowHeight = 0;
        int currentRowWidth = 0;
        int top = 0;

        for (int i = 0; i < count; i++) {
            View child = this.getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                WrapLayout.LayoutParams lp = (WrapLayout.LayoutParams) child.getLayoutParams();

                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                int childTotalWidth = childWidth + lp.rightMargin + lp.leftMargin + mMarginWidth;
                int childTotalHeight = childHeight + lp.topMargin + lp.bottomMargin + mMarginHeight;

                if (width < currentRowWidth + childTotalWidth) {
                    top += rowHeight < childTotalHeight ? childTotalHeight : rowHeight;
                    rowHeight = 0;
                    currentRowWidth = 0;
                }
                height = top + childHeight + lp.bottomMargin;
                LogHelper.i(TAG, "i=", i, ", ", "height", height);
                rowHeight = rowHeight < childTotalHeight ? childTotalHeight : rowHeight;
                currentRowWidth += childTotalWidth;
            }
        }
        LogHelper.i(TAG, "totalHeight=", height);
        this.setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int count = this.getChildCount();
        int rowHeight = 0;
        int currentRowWidth = 0;
        int rowMaxWidth = this.getWidth();
        int top = 0;

        for (int i = 0; i < count; i++) {
            View child = this.getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                WrapLayout.LayoutParams lp = (WrapLayout.LayoutParams) child.getLayoutParams();

                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                int childTotalWidth = childWidth + lp.rightMargin + lp.leftMargin + mMarginWidth;
                int childTotalHeight = childHeight + lp.topMargin + lp.bottomMargin + mMarginHeight;

                if (rowMaxWidth < currentRowWidth + childTotalWidth) {
                    top += rowHeight < childTotalHeight ? childTotalHeight : rowHeight;
                    rowHeight = 0;
                    currentRowWidth = 0;
                }

                child.layout(
                        currentRowWidth + lp.leftMargin + mMarginWidth,
                        top + lp.topMargin,
                        currentRowWidth + childWidth + lp.rightMargin + mMarginWidth,
                        top + childHeight + lp.bottomMargin);

                rowHeight = rowHeight < childTotalHeight ? childTotalHeight : rowHeight;
                currentRowWidth += childTotalWidth;
            }
        }
    }

    @Override
    protected WrapLayout.LayoutParams generateDefaultLayoutParams() {
        return new WrapLayout.LayoutParams(WrapLayout.LayoutParams.WRAP_CONTENT, WrapLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof WrapLayout.LayoutParams;
    }

    @Override
    protected WrapLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new WrapLayout.LayoutParams(p);
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context context, AttributeSet attr) {
            super(context, attr);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }
    }
}