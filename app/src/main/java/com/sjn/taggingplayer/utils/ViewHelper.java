package com.sjn.taggingplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.sjn.taggingplayer.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ViewHelper {

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

    public static void updateAlbumArt(final Activity activity, final ImageView view, final String artUrl) {
        view.setTag(artUrl);
        if (artUrl == null || artUrl.isEmpty()) {
            view.setImageDrawable(ContextCompat.getDrawable(activity, R.mipmap.ic_launcher));
            return;
        }
        Picasso.with(activity).load(artUrl).resize(128, 128).into(view, new Callback() {
            @Override
            public void onSuccess() {
                if (!artUrl.equals(view.getTag())) {
                    updateAlbumArt(activity, view, (String) view.getTag());
                }
            }

            @Override
            public void onError() {
                view.setImageDrawable(ContextCompat.getDrawable(activity, R.mipmap.ic_launcher));
            }
        });
    }

    public static void setDrawableLayerColor(Activity activity, View view, int color, int drawableId, int layerId) {
        LayerDrawable shape = (LayerDrawable) ContextCompat.getDrawable(activity, drawableId);
        shape.findDrawableByLayerId(layerId).mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        view.setBackground(shape);
    }
}
