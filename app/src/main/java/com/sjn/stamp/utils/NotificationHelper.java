package com.sjn.stamp.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.NotificationCompat;

import com.sjn.stamp.R;

import java.util.Date;

import static com.sjn.stamp.utils.TimeHelper.getDateDiff;

public class NotificationHelper {

    public static void sendNotification(Context context, String title, int playCount, Date recordedAt) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (playCount == 1) {
            nm.notify(1, new NotificationCompat.Builder(context)
                    .setColor(ResourceHelper.getThemeColor(context, R.attr.colorPrimary, Color.DKGRAY))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentTitle(title + "を初めて再生しました！！")
                    .build());
            return;
        }
        nm.notify(1, new NotificationCompat.Builder(context)
                .setColor(ResourceHelper.getThemeColor(context, R.attr.colorPrimary, Color.DKGRAY))
                .setSmallIcon(R.drawable.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(title + "を" + playCount + "回再生しました！！")
                .setContentText("最初に聞いてから" + TimeHelper.getDateDiff(recordedAt) + "です。")
                .build());
    }
}
