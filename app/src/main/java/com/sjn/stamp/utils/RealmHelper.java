package com.sjn.stamp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.sjn.stamp.R;
import com.sjn.stamp.migration.Migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.internal.IOException;

public class RealmHelper {
    private static final int VERSION = 1;

    private static final String TAG = LogHelper.makeLogTag(RealmHelper.class);
    private static final File EXPORT_REALM_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static final String EXPORT_REALM_FILE_NAME = "stamp_backup.realm";
    private static final String IMPORT_REALM_FILE_NAME = "default.realm";


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public static void init(Context context) {
        Realm.init(context);
        RealmConfiguration config = buildConfig();
        try {
            Realm.migrateRealm(config, new Migration());
        } catch (FileNotFoundException ignored) {
            // If the Realm file doesn't exist, just ignore.
        }
        Realm.setDefaultConfiguration(config);
    }

    public static Realm getRealmInstance() {
        return Realm.getDefaultInstance();
    }

    public static RealmConfiguration buildConfig() {
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder();
        if (com.sjn.stamp.BuildConfig.BUILD_TYPE.equals("debug")) {
            //builder.deleteRealmIfMigrationNeeded();
        }
        builder.schemaVersion(VERSION);
        return builder.build();
    }

    public static void exportBackUp(Activity activity) {
        Realm realm = getRealmInstance();
        // First check if we have storage permissions
        checkStoragePermissions(activity);
        File exportRealmFile;

        try {
            EXPORT_REALM_PATH.mkdirs();

            // create a backup file
            exportRealmFile = new File(EXPORT_REALM_PATH, EXPORT_REALM_FILE_NAME);

            // if backup file already exists, delete it
            exportRealmFile.delete();

            // copy current realm to backup file
            realm.writeCopyTo(exportRealmFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        String msg = activity.getResources().getString(R.string.message_file_exported, EXPORT_REALM_PATH + "/" + EXPORT_REALM_FILE_NAME);
        Toast.makeText(activity.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        realm.close();

    }

    public static void importBackUp(final Activity activity) {
        checkStoragePermissions(activity);
        //Restore
        String restoreFilePath = EXPORT_REALM_PATH + "/" + EXPORT_REALM_FILE_NAME;
        copyBundledRealmFile(activity, restoreFilePath, IMPORT_REALM_FILE_NAME);
        init(activity);
    }

    private static String copyBundledRealmFile(Activity activity, String oldFilePath, String outFileName) {
        try {
            File file = new File(activity.getApplicationContext().getFilesDir(), outFileName);

            FileOutputStream outputStream = new FileOutputStream(file);

            FileInputStream inputStream = new FileInputStream(new File(oldFilePath));

            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            return file.getAbsolutePath();
        } catch (IOException | java.io.IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void checkStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private static String dbPath(Realm realm) {
        return realm.getPath();
    }
}
