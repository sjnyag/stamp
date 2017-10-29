package com.sjn.stamp.utils;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DataSourceHelper {
    public static boolean setMediaPlayerDataSource(Context context,
                                                   MediaPlayer mp, String fileInfo) throws IOException {

        if (fileInfo.startsWith("content://")) {
            Uri uri = Uri.parse(fileInfo);
            mp.setDataSource(context, uri);
            return true;
        }

        try {
            if (CompatibleHelper.hasHoneycomb())
                try {
                    setMediaPlayerDataSourcePreHoneyComb(context, mp, fileInfo);
                } catch (Exception e) {
                    setMediaPlayerDataSourcePostHoneyComb(context, mp, fileInfo);
                }
            else {
                setMediaPlayerDataSourcePostHoneyComb(context, mp, fileInfo);
            }
        } catch (Exception e) {
            try {
                setMediaPlayerDataSourceUsingFileDescriptor(context, mp, fileInfo);
            } catch (Exception ee) {
                try {
                    String uri = getRingtoneUriFromPath(context, fileInfo);
                    mp.reset();
                    mp.setDataSource(uri);
                } catch (Exception eee) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void setMediaPlayerDataSourcePreHoneyComb(Context context, MediaPlayer mp, String fileInfo) throws Exception {
        mp.reset();
        mp.setDataSource(fileInfo);
    }

    private static void setMediaPlayerDataSourcePostHoneyComb(Context context, MediaPlayer mp, String fileInfo) throws Exception {
        mp.reset();
        mp.setDataSource(context, Uri.parse(Uri.encode(fileInfo)));
    }

    private static void setMediaPlayerDataSourceUsingFileDescriptor(Context context, MediaPlayer mp, String fileInfo) throws Exception {
        File file = new File(fileInfo);
        FileInputStream inputStream = new FileInputStream(file);
        mp.reset();
        mp.setDataSource(inputStream.getFD());
        inputStream.close();
    }

    private static String getRingtoneUriFromPath(Context context, String path) {
        Uri ringtoneUri = MediaStore.Audio.Media.getContentUriForPath(path);
        Cursor ringtoneCursor = context.getContentResolver().query(
                ringtoneUri, null,
                MediaStore.Audio.Media.DATA + "='" + path + "'", null, null);
        ringtoneCursor.moveToFirst();

        long id = ringtoneCursor.getLong(ringtoneCursor
                .getColumnIndex(MediaStore.Audio.Media._ID));
        ringtoneCursor.close();

        if (!ringtoneUri.toString().endsWith(String.valueOf(id))) {
            return ringtoneUri + "/" + id;
        }
        return ringtoneUri.toString();
    }

    private static String getRingtonePathFromContentUri(Context context,
                                                        Uri contentUri) {
        String[] projection = {MediaStore.Audio.Media.DATA};
        Cursor ringtoneCursor = context.getContentResolver().query(contentUri,
                projection, null, null, null);
        ringtoneCursor.moveToFirst();

        String path = ringtoneCursor.getString(ringtoneCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

        ringtoneCursor.close();
        return path;
    }
}
