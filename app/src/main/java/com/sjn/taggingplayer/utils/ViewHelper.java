package com.sjn.taggingplayer.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.ui.custom.TextDrawable;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import eu.davidea.flipview.FlipView;

public class ViewHelper {
    private static int colorAccent = -1;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static int getColorAccent(Context context) {
        if (colorAccent < 0) {
            int accentAttr = eu.davidea.flexibleadapter.utils.Utils.hasLollipop() ? android.R.attr.colorAccent : R.attr.colorAccent;
            TypedArray androidAttr = context.getTheme().obtainStyledAttributes(new int[]{accentAttr});
            colorAccent = androidAttr.getColor(0, 0xFF009688); //Default: material_deep_teal_500
            androidAttr.recycle();
        }
        return colorAccent;
    }

    public static float dpToPx(Context context, float dp) {
        return Math.round(dp * getDisplayMetrics(context).density);
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    public static void setFragmentTitle(Activity activity, String title) {
        if (activity == null || !(activity instanceof AppCompatActivity)) {
            return;
        }
        ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }


    public static void setFragmentTitle(Activity activity, int title) {
        if (activity != null) {
            setFragmentTitle(activity, activity.getResources().getString(title));
        }
    }

    public static int getRankingColor(Context context, int position) {
        return ContextCompat.getColor(context, getRankingColorResourceId(position));
    }

    private static int getRankingColorResourceId(int position) {
        switch (position) {
            case 0:
                return R.color.color_1;
            case 1:
                return R.color.color_2;
            case 2:
                return R.color.color_3;
            case 3:
                return R.color.color_4;
            case 4:
                return R.color.color_5;
            case 5:
                return R.color.color_6;
            case 6:
                return R.color.color_7;
            default:
                return R.color.colorPrimaryDark;
        }
    }

    public static void updateAlbumArt(final Activity activity, final FlipView view, final String artUrl, final String text) {
        updateAlbumArt(activity, view, artUrl, text, 192, 192);
    }

    public static void updateAlbumArt(final Activity activity, final ImageView view, final String artUrl, final String text) {
        updateAlbumArt(activity, view, artUrl, text, 128, 128);
    }


    public static void updateAlbumArt(final Activity activity, final FlipView flipView, final String artUrl, final String text, final int targetWidth, final int targetHeight) {
        flipView.getFrontImageView().setTag(artUrl);
        if (artUrl == null || artUrl.isEmpty()) {
            //view.setImageDrawable(ContextCompat.getDrawable(activity, R.mipmap.ic_launcher));
            flipView.getChildAt(0).setBackground(createTextDrawable(text));
            return;
        }
        Picasso.with(activity).load(artUrl).resize(targetWidth, targetHeight).into(flipView.getFrontImageView(), new Callback() {
            @Override
            public void onSuccess() {
                if (!artUrl.equals(flipView.getFrontImageView().getTag())) {
                    updateAlbumArt(activity, flipView.getFrontImageView(), (String) flipView.getFrontImageView().getTag(), text);
                }
            }

            @Override
            public void onError() {
                flipView.getChildAt(0).setBackground(createTextDrawable(text));
                //view.setImageDrawable(ContextCompat.getDrawable(activity, R.mipmap.ic_launcher));
            }
        });
    }


    public static void updateAlbumArt(final Activity activity, final ImageView view, final String artUrl, final String text, final int targetWidth, final int targetHeight) {
        view.setTag(artUrl);
        if (artUrl == null || artUrl.isEmpty()) {
            //view.setImageDrawable(ContextCompat.getDrawable(activity, R.mipmap.ic_launcher));
            view.setImageDrawable(createTextDrawable(text));
            return;
        }
        Picasso.with(activity).load(artUrl).resize(targetWidth, targetHeight).into(view, new Callback() {
            @Override
            public void onSuccess() {
                if (!artUrl.equals(view.getTag())) {
                    updateAlbumArt(activity, view, (String) view.getTag(), text);
                }
            }

            @Override
            public void onError() {
                view.setImageDrawable(createTextDrawable(text));
                //view.setImageDrawable(ContextCompat.getDrawable(activity, R.mipmap.ic_launcher));
            }
        });
    }

    public static void setDrawableLayerColor(Activity activity, View view, int color, int drawableId, int layerId) {
        LayerDrawable shape = (LayerDrawable) ContextCompat.getDrawable(activity, drawableId);
        shape.findDrawableByLayerId(layerId).mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        view.setBackground(shape);
    }

    public static TextDrawable createTextDrawable(String text) {
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        String s = text == null || text.isEmpty() ? "" : String.valueOf(text.charAt(0));
        return TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.DEFAULT)
                .bold()
                .toUpperCase()
                .endConfig()
                .rect()
                .build(s, generator.getColor(text));
    }

    public static Bitmap toBitmap(Drawable drawable, int width, int height) {

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            return bitmapDrawable.getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return bitmap;
    }

    public static class ColorGenerator {

        public static ColorGenerator DEFAULT;

        public static ColorGenerator MATERIAL;

        static {
            DEFAULT = create(Arrays.asList(
                    0xfff16364,
                    0xfff58559,
                    0xfff9a43e,
                    0xffe4c62e,
                    0xff67bf74,
                    0xff59a2be,
                    0xff2093cd,
                    0xffad62a7,
                    0xff805781
            ));
            MATERIAL = create(Arrays.asList(
                    0xffe57373,
                    0xfff06292,
                    0xffba68c8,
                    0xff9575cd,
                    0xff7986cb,
                    0xff64b5f6,
                    0xff4fc3f7,
                    0xff4dd0e1,
                    0xff4db6ac,
                    0xff81c784,
                    0xffaed581,
                    0xffff8a65,
                    0xffd4e157,
                    0xffffd54f,
                    0xffffb74d,
                    0xffa1887f,
                    0xff90a4ae
            ));
        }

        private final List<Integer> mColors;
        private final Random mRandom;

        public static ColorGenerator create(List<Integer> colorList) {
            return new ColorGenerator(colorList);
        }

        private ColorGenerator(List<Integer> colorList) {
            mColors = colorList;
            mRandom = new Random(System.currentTimeMillis());
        }

        public int getRandomColor() {
            return mColors.get(mRandom.nextInt(mColors.size()));
        }

        public int getColor(Object key) {
            if(key == null){
                return mColors.get(0);
            }
            return mColors.get(Math.abs(key.hashCode()) % mColors.size());
        }
    }

}
