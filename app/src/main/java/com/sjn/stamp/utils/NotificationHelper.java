package com.sjn.stamp.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.sjn.stamp.R;
import com.sjn.stamp.model.Song;
import com.sjn.stamp.ui.activity.IntentDispatchActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class NotificationHelper {

    public static final String ACTION_CMD = "com.sjn.stamp.ACTION_CMD";
    public static final String CMD_NAME = "CMD_NAME";
    public static final String CMD_PAUSE = "CMD_PAUSE";
    public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";
    public static final String CMD_KILL = "CMD_KILL";
    public static final String CMD_SHARE = "CMD_SHARE";
    public static final String SHARE_MESSAGE = "SHARE_MESSAGE";
    public static final String HASH_TAG_LIST = "HASH_TAG_LIST";
    private static String BUNDLE_KEY_PLAYED_TEXT = "BUNDLE_KEY_PLAYED_TEXT";
    private static String BUNDLE_KEY_HASH_TAG_LIST = "BUNDLE_KEY_HASH_TAG_LIST";

    public static boolean isSendPlayedNotification(int count) {
        return count == 10 || count == 50 || (count % 100 == 0 && count >= 100);
    }

    public static void sendPlayedNotification(Context context, String title, String bitmapUrl, int playCount, Date recordedAt) {
        String contentTitle = context.getResources().getString(R.string.notification_title, title, String.valueOf(playCount));
        String contentText = context.getResources().getString(R.string.notification_text, TimeHelper.INSTANCE.getDateDiff(context, recordedAt));
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_PLAYED_TEXT, contentTitle + "\n" + contentText);
        bundle.putStringArrayList(BUNDLE_KEY_HASH_TAG_LIST, new ArrayList<>(Arrays.asList(title)));
        fetchBitmapFromURLAsync(context, bitmapUrl, new NotificationCompat.Builder(context)
                .setColor(ViewHelper.INSTANCE.getThemeColor(context, R.attr.colorPrimary, Color.DKGRAY))
                .setSmallIcon(R.mipmap.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setExtras(bundle)
        );
    }

    public static void sendPlayedNotification(Context context, Song song, String bitmapUrl, int playCount, Date recordedAt) {
        String contentTitle = context.getResources().getString(R.string.notification_title, context.getResources().getString(R.string.share_song, song.getTitle(), song.getArtist().getName()), String.valueOf(playCount));
        String contentText = context.getResources().getString(R.string.notification_text, TimeHelper.INSTANCE.getDateDiff(context, recordedAt));
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_PLAYED_TEXT, contentTitle + "\n" + contentText);
        bundle.putStringArrayList(BUNDLE_KEY_HASH_TAG_LIST, new ArrayList<>(Arrays.asList(song.getTitle(), song.getArtist().getName())));
        fetchBitmapFromURLAsync(context, bitmapUrl, new NotificationCompat.Builder(context)
                .setColor(ViewHelper.INSTANCE.getThemeColor(context, R.attr.colorPrimary, Color.DKGRAY))
                .setSmallIcon(R.mipmap.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setExtras(bundle)
        );
    }

    private static void fetchBitmapFromURLAsync(final Context context, final String bitmapUrl,
                                                final NotificationCompat.Builder builder) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                new SetNotificationBitmapAsyncTask(context, builder, bitmap).execute();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                int requestCode = builder.hashCode();
                PendingIntent shareIntent = createShareAction(context, requestCode, builder.getExtras().getString(BUNDLE_KEY_PLAYED_TEXT), builder.getExtras().getStringArrayList(BUNDLE_KEY_HASH_TAG_LIST));
                builder.setContentIntent(shareIntent);
                builder.addAction(R.drawable.ic_share, context.getResources().getString(R.string.notification_share), shareIntent);
                send(context, requestCode, builder);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        ViewHelper.INSTANCE.readBitmapAsync(context, bitmapUrl, target);
    }

    private static class SetNotificationBitmapAsyncTask extends AsyncTask<Void, Void, Void> {
        Context mContext;
        NotificationCompat.Builder mBuilder;
        Bitmap mBitmap;

        SetNotificationBitmapAsyncTask(Context context, final NotificationCompat.Builder builder, Bitmap bitmap) {
            mContext = context;
            mBuilder = builder;
            mBitmap = bitmap;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mBuilder.setLargeIcon(mBitmap);
            int requestCode = mBuilder.hashCode();
            PendingIntent shareIntent = createShareAction(mContext, requestCode, mBuilder.getExtras().getString(BUNDLE_KEY_PLAYED_TEXT), mBuilder.getExtras().getStringArrayList(BUNDLE_KEY_HASH_TAG_LIST));
            mBuilder.setContentIntent(shareIntent);
            mBuilder.addAction(R.drawable.ic_share, mContext.getResources().getString(R.string.notification_share), shareIntent);
            send(mContext, requestCode, mBuilder);
            return null;
        }
    }

    private static PendingIntent createShareAction(Context context, int requestCode, String text, ArrayList<String> hashTagList) {
        Intent i = new Intent(context, IntentDispatchActivity.class);
        i.setAction(ACTION_CMD);
        i.putExtra(CMD_NAME, CMD_SHARE);
        i.putExtra(SHARE_MESSAGE, text);
        i.putStringArrayListExtra(HASH_TAG_LIST, hashTagList);
        return PendingIntent.getActivity(context, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void send(Context context, int id, NotificationCompat.Builder builder) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }
}
