package com.sjn.stamp.ui.custom;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;

import java.util.Locale;

public class TextDrawable extends ShapeDrawable {

    private final Paint textPaint;
    private final Paint borderPaint;
    private static final float SHADE_FACTOR = 0.9f;
    private final String text;
    private final int color;
    private final RectShape shape;
    private final int height;
    private final int width;
    private final int fontSize;
    private final float radius;
    private final int borderThickness;

    private TextDrawable(TextDrawable.Builder builder) {
        super(builder.shape);

        // shape properties
        shape = builder.shape;
        height = builder.height;
        width = builder.width;
        radius = builder.radius;

        // text and color
        text = builder.toUpperCase ? builder.text.toUpperCase(Locale.getDefault()) : builder.text;
        color = builder.color;

        // text paint settings
        fontSize = builder.fontSize;
        textPaint = new Paint();
        textPaint.setColor(builder.textColor);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(builder.isBold);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(builder.font);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStrokeWidth(builder.borderThickness);

        // border paint settings
        borderThickness = builder.borderThickness;
        borderPaint = new Paint();
        borderPaint.setColor(getDarkerShade(color));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderThickness);

        // drawable paint color
        Paint paint = getPaint();
        paint.setColor(color);

    }

    private int getDarkerShade(int color) {
        return Color.rgb((int) (SHADE_FACTOR * Color.red(color)),
                (int) (SHADE_FACTOR * Color.green(color)),
                (int) (SHADE_FACTOR * Color.blue(color)));
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Rect r = getBounds();


        // draw border
        if (borderThickness > 0) {
            drawBorder(canvas);
        }

        int count = canvas.save();
        canvas.rotate(20);
        canvas.translate(10, -10);

        // draw text
        int width = this.width < 0 ? r.width() : this.width;
        int height = this.height < 0 ? r.height() : this.height;
        int fontSize = this.fontSize < 0 ? ((int) (Math.min(width, height) * 1.5)) : this.fontSize;
        textPaint.setTextSize(fontSize);
        canvas.drawText(text, width / 2, height / 2 - ((textPaint.descent() + textPaint.ascent()) / 2), textPaint);

        canvas.restoreToCount(count);

    }

    private void drawBorder(Canvas canvas) {
        RectF rect = new RectF(getBounds());
        rect.inset(borderThickness / 2, borderThickness / 2);

        if (shape instanceof OvalShape) {
            canvas.drawOval(rect, borderPaint);
        } else if (shape instanceof RoundRectShape) {
            canvas.drawRoundRect(rect, radius, radius, borderPaint);
        } else {
            canvas.drawRect(rect, borderPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        textPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        textPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    public static TextDrawable.IShapeBuilder builder() {
        return new TextDrawable.Builder();
    }

    public static class Builder implements TextDrawable.IConfigBuilder, TextDrawable.IShapeBuilder, TextDrawable.IBuilder {

        private String text;

        private int color;

        private int borderThickness;

        private int width;

        private int height;

        private Typeface font;

        private RectShape shape;

        public int textColor;

        private int fontSize;

        private boolean isBold;

        private boolean toUpperCase;

        public float radius;

        private Builder() {
            text = "";
            color = Color.GRAY;
            textColor = Color.parseColor("#55FFFFFF");
            borderThickness = 0;
            width = -1;
            height = -1;
            shape = new RectShape();
            font = Typeface.create("sans-serif-light", Typeface.NORMAL);
            fontSize = -1;
            isBold = false;
            toUpperCase = false;
        }

        public TextDrawable.IConfigBuilder width(int width) {
            this.width = width;
            return this;
        }

        public TextDrawable.IConfigBuilder height(int height) {
            this.height = height;
            return this;
        }

        public TextDrawable.IConfigBuilder textColor(int color) {
            this.textColor = color;
            return this;
        }

        public TextDrawable.IConfigBuilder withBorder(int thickness) {
            this.borderThickness = thickness;
            return this;
        }

        public TextDrawable.IConfigBuilder useFont(Typeface font) {
            this.font = font;
            return this;
        }

        public TextDrawable.IConfigBuilder fontSize(int size) {
            this.fontSize = size;
            return this;
        }

        public TextDrawable.IConfigBuilder bold() {
            this.isBold = true;
            return this;
        }

        public TextDrawable.IConfigBuilder toUpperCase() {
            this.toUpperCase = true;
            return this;
        }

        @Override
        public TextDrawable.IConfigBuilder beginConfig() {
            return this;
        }

        @Override
        public TextDrawable.IShapeBuilder endConfig() {
            return this;
        }

        @Override
        public TextDrawable.IBuilder rect() {
            this.shape = new RectShape();
            return this;
        }

        @Override
        public TextDrawable.IBuilder round() {
            this.shape = new OvalShape();
            return this;
        }

        @Override
        public TextDrawable.IBuilder roundRect(int radius) {
            this.radius = radius;
            float[] radii = {radius, radius, radius, radius, radius, radius, radius, radius};
            this.shape = new RoundRectShape(radii, null, null);
            return this;
        }

        @Override
        public TextDrawable buildRect(String text, int color) {
            rect();
            return build(text, color);
        }

        @Override
        public TextDrawable buildRoundRect(String text, int color, int radius) {
            roundRect(radius);
            return build(text, color);
        }

        @Override
        public TextDrawable buildRound(String text, int color) {
            round();
            return build(text, color);
        }

        @Override
        public TextDrawable build(String text, int color) {
            this.color = color;
            this.text = text;
            return new TextDrawable(this);
        }
    }

    public interface IConfigBuilder {
        public TextDrawable.IConfigBuilder width(int width);

        public TextDrawable.IConfigBuilder height(int height);

        public TextDrawable.IConfigBuilder textColor(int color);

        public TextDrawable.IConfigBuilder withBorder(int thickness);

        public TextDrawable.IConfigBuilder useFont(Typeface font);

        public TextDrawable.IConfigBuilder fontSize(int size);

        public TextDrawable.IConfigBuilder bold();

        public TextDrawable.IConfigBuilder toUpperCase();

        public TextDrawable.IShapeBuilder endConfig();
    }

    public static interface IBuilder {

        public TextDrawable build(String text, int color);
    }

    public static interface IShapeBuilder {

        public TextDrawable.IConfigBuilder beginConfig();

        public TextDrawable.IBuilder rect();

        public TextDrawable.IBuilder round();

        public TextDrawable.IBuilder roundRect(int radius);

        public TextDrawable buildRect(String text, int color);

        public TextDrawable buildRoundRect(String text, int color, int radius);

        public TextDrawable buildRound(String text, int color);
    }
}