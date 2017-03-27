package com.sjn.taggingplayer.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.sjn.taggingplayer.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class TweetHelper {
    public static void tweet(Context context, String text) {
        if (context == null) {
            return;
        }
        String tweet = "";
        try {
            tweet = "https://twitter.com/intent/tweet?text="
                    + URLEncoder.encode(text + "\n", "UTF-8")
                    + URLEncoder.encode(context.getResources().getString(R.string.hash_tag) + "\n", "UTF-8")
                    + "&url="
                    + URLEncoder.encode(context.getResources().getString(R.string.base_url) + "\n", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweet));
        context.startActivity(intent);
    }
}
