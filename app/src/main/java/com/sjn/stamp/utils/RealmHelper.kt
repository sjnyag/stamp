package com.sjn.stamp.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.sjn.stamp.R
import com.sjn.stamp.model.migration.Migration
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.internal.IOException
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

object RealmHelper {

    private val EXPORT_REALM_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    private const val EXPORT_REALM_FILE_NAME = "stamp_backup.realm"
    private const val IMPORT_REALM_FILE_NAME = "default.realm"
    private const val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val realmInstance: Realm
        get() = Realm.getDefaultInstance()

    fun init(context: Context) {
        Realm.init(context)
        val config = buildConfig()
        try {
            Realm.migrateRealm(config, Migration())
        } catch (ignored: FileNotFoundException) {
            // If the Realm file doesn't exist, just ignore.
        }
        Realm.setDefaultConfiguration(config)
    }

    private fun buildConfig(): RealmConfiguration = RealmConfiguration.Builder().schemaVersion(Migration.VERSION.toLong()).build()

    fun exportBackUp(activity: Activity) {
        // First check if we have storage permissions
        checkStoragePermissions(activity)
        try {
            EXPORT_REALM_PATH.mkdirs()
            // create a backup file
            val exportRealmFile = File(EXPORT_REALM_PATH, EXPORT_REALM_FILE_NAME)
            // if backup file already exists, delete it
            exportRealmFile.delete()
            // copy current realm to backup file
            realmInstance.use {
                it.writeCopyTo(exportRealmFile)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Toast.makeText(activity.applicationContext, activity.resources.getString(R.string.message_file_exported, EXPORT_REALM_PATH.toString() + "/" + EXPORT_REALM_FILE_NAME), Toast.LENGTH_LONG).show()
    }

    fun importBackUp(activity: Activity, uri: Uri) {
        checkStoragePermissions(activity)
        //copyBundledRealmFile(activity, EXPORT_REALM_PATH + "/" + EXPORT_REALM_FILE_NAME, IMPORT_REALM_FILE_NAME);
        copyBundledRealmFile(activity, uri, IMPORT_REALM_FILE_NAME)
        init(activity)
    }

    private fun copyBundledRealmFile(activity: Activity, uri: Uri, outFileName: String): String? {
        try {
            return File(activity.applicationContext.filesDir, outFileName).apply {
                FileOutputStream(this).use { output ->
                    activity.contentResolver.openInputStream(uri).use { input ->
                        val buf = ByteArray(1024)
                        var bytesRead: Int
                        while (true) {
                            bytesRead = input.read(buf)
                            if (bytesRead <= 0) {
                                break
                            }
                            output.write(buf, 0, bytesRead)
                        }
                    }
                }
            }.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: java.io.IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun copyBundledRealmFile(activity: Activity, oldFilePath: String, outFileName: String): String? {
        try {
            return File(activity.applicationContext.filesDir, outFileName).apply {
                FileOutputStream(this).use { output ->
                    FileInputStream(File(oldFilePath)).use { input ->
                        val buf = ByteArray(1024)
                        var bytesRead: Int
                        while (true) {
                            bytesRead = input.read(buf)
                            if (bytesRead <= 0) {
                                break
                            }
                            output.write(buf, 0, bytesRead)
                        }
                    }
                }
            }.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: java.io.IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun checkStoragePermissions(activity: Activity) {
        // Check if we have write permission
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
        }
    }

    private fun dbPath(realm: Realm): String = realm.path
}
