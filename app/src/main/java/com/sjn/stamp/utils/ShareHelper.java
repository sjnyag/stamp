package com.sjn.stamp.utils;

import android.content.Context;
import android.content.Intent;

import com.sjn.stamp.R;


public class ShareHelper {
    public static void share(Context context, String text) {
        if (context == null || text == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, text + "\n"
                        + context.getResources().getString(R.string.hash_tag) + "\n"
                        + context.getResources().getString(R.string.base_url));
        context.startActivity(intent);
    }
}
