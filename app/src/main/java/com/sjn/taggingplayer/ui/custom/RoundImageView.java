package com.sjn.taggingplayer.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.sjn.taggingplayer.R;

public class RoundImageView extends AppCompatImageView {

    Paint mMaskedPaint;
    Paint mCopyPaint;
    Drawable mMaskDrawable;

    public RoundImageView(Context context) {
        this(context, null);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mMaskedPaint = new Paint();
        mMaskedPaint.setXfermode(new PorterDuffXfermode(
                PorterDuff.Mode.SRC_ATOP));

        mCopyPaint = new Paint();
        mMaskDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.round_mask, null);
    }

    Rect mBounds;
    RectF mBoundsF;

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBounds = new Rect(0, 0, w, h);
        mBoundsF = new RectF(mBounds);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int sc = canvas.saveLayer(mBoundsF, mCopyPaint,
                Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                        | Canvas.FULL_COLOR_LAYER_SAVE_FLAG);

        mMaskDrawable.setBounds(mBounds);
        mMaskDrawable.draw(canvas);

        canvas.saveLayer(mBoundsF, mMaskedPaint, 0);

        super.onDraw(canvas);

        canvas.restoreToCount(sc);
    }
}