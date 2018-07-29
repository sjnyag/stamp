package com.sjn.stamp.ui

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.sjn.stamp.R
import com.sjn.stamp.ui.custom.PeriodSelectLayout
import com.sjn.stamp.ui.custom.StampRegisterLayout
import net.yslibrary.licenseadapter.LicenseAdapter
import net.yslibrary.licenseadapter.LicenseEntry
import net.yslibrary.licenseadapter.Licenses
import net.yslibrary.licenseadapter.Licenses.LICENSE_APACHE_V2
import java.util.*

@Suppress("unused")
object DialogFacade {

    fun createRankingPeriodSelectDialog(context: Context, period: PeriodSelectLayout.Period, updateRankingPeriod: (PeriodSelectLayout) -> Unit): AlertDialog =
            PeriodSelectLayout(context, period).run {
                AlertDialog.Builder(context)
                        .setTitle(R.string.dialog_period_select)
                        .setView(this)
                        .setPositiveButton(R.string.dialog_ok) { _, _ -> updateRankingPeriod(this) }
                        .show()
            }

    fun createRegisterStampDialog(context: Context): AlertDialog =
            AlertDialog.Builder(context).apply {
                setTitle(R.string.dialog_stamp_register)
                setView(StampRegisterLayout(context, null))
                setPositiveButton(R.string.dialog_close) { _, _ -> }
            }.show()


    fun createConfirmDialog(context: Context, content: String, onPositive: (Any, Any) -> Unit): AlertDialog.Builder =
            AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_confirm_title)
                    .setMessage(content)
                    .setPositiveButton(R.string.dialog_ok, onPositive)
                    .setNegativeButton(R.string.dialog_cancel) { _, _ -> }
                    .setOnDismissListener { }

    fun createConfirmDialog(context: Context, content: String, onPositive: (Any, Any) -> Unit, onNegative: (Any, Any) -> Unit, onDismiss: DialogInterface.OnDismissListener): AlertDialog.Builder =
            AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_confirm_title)
                    .setMessage(content)
                    .setPositiveButton(R.string.dialog_ok, onPositive)
                    .setNegativeButton(R.string.dialog_cancel, onNegative)
                    .setOnDismissListener(onDismiss)

    fun createConfirmDialog(context: Context, content: Int, onPositive: (Any, Any) -> Unit): AlertDialog.Builder =
            AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_confirm_title)
                    .setMessage(content)
                    .setPositiveButton(R.string.dialog_ok, onPositive)
                    .setNegativeButton(R.string.dialog_cancel) { _, _ -> }
                    .setOnDismissListener { }

    fun createConfirmDialog(context: Context, content: Int, onPositive: (Any, Any) -> Unit, onNegative: (Any, Any) -> Unit, onDismiss: DialogInterface.OnDismissListener): AlertDialog.Builder =
            AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_confirm_title)
                    .setMessage(content)
                    .setPositiveButton(R.string.dialog_ok, onPositive)
                    .setNegativeButton(R.string.dialog_cancel, onNegative)
                    .setOnDismissListener(onDismiss)

    fun createPermissionNecessaryDialog(context: Context, onPositive: (Any, Any) -> Unit): AlertDialog.Builder =
            AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_permission_necessary_title)
                    .setMessage(R.string.dialog_permission_necessary_content)
                    .setPositiveButton(R.string.dialog_ok, onPositive)
                    .setCancelable(false)

    fun createRestartDialog(context: Context, onPositive: (Any, Any) -> Unit): AlertDialog.Builder =
            AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_restart_title)
                    .setMessage(R.string.dialog_restart_content)
                    .setPositiveButton(R.string.dialog_ok, onPositive)

    fun createRemoveStampSongDialog(context: Context, stamp: String, onPositive: (Any, Any) -> Unit): AlertDialog.Builder {
        return AlertDialog.Builder(context)
                .setTitle(R.string.dialog_delete_stamp_title)
                .setMessage(context.resources.getString(R.string.dialog_delete_stamp_content, stamp))
                .setPositiveButton(R.string.dialog_delete, onPositive)
                .setNegativeButton(R.string.dialog_cancel) { _, _ -> }
    }

    fun createLetsPlayMusicDialog(context: Context): AlertDialog.Builder {
        return AlertDialog.Builder(context)
                .setTitle(R.string.dialog_lets_play_title)
                .setPositiveButton(R.string.dialog_ok) { _, _ -> }
    }

    fun createRemoveStampSongDialog(context: Context, song: String, stamp: String, onPositive: (Any, Any) -> Unit, dismissListener: DialogInterface.OnDismissListener): AlertDialog.Builder {
        return AlertDialog.Builder(context)
                .setTitle(R.string.dialog_remove_stamp_title)
                .setMessage(context.resources.getString(R.string.dialog_remove_stamp_content, song, stamp))
                .setPositiveButton(R.string.dialog_delete, onPositive)
                .setNegativeButton(R.string.dialog_cancel) { _, _ -> }
                .setOnDismissListener(dismissListener)
    }

    fun createHistoryDeleteDialog(context: Context, history: String, onPositive: (Any, Any) -> Unit, dismissListener: DialogInterface.OnDismissListener): AlertDialog.Builder {
        return AlertDialog.Builder(context)
                .setTitle(R.string.dialog_delete_history_title)
                .setMessage(context.resources.getString(R.string.dialog_delete_history_content, history))
                .setPositiveButton(R.string.dialog_delete, onPositive)
                .setNegativeButton(R.string.dialog_cancel) { _, _ -> }
                .setOnDismissListener(dismissListener)
    }

    fun createRetrieveMediaDialog(context: Context, onPositive: (Any, Any) -> Unit, onNegative: (Any, Any) -> Unit, onDismiss: DialogInterface.OnDismissListener): AlertDialog.Builder {
        return AlertDialog.Builder(context)
                .setTitle(R.string.dialog_reload_music_title)
                .setMessage(R.string.dialog_reload_music_content)
                .setPositiveButton(R.string.dialog_reload, onPositive)
                .setNegativeButton(R.string.dialog_cancel, onNegative)
                .setOnDismissListener(onDismiss)
    }

    fun createLicenceDialog(context: Context): AlertDialog.Builder {
        val dataSet = ArrayList<LicenseEntry>()
        dataSet.add(Licenses.noContent("Android SDK", "Google Inc.", "https://developer.android.com/sdk/terms.html"))

        dataSet.add(Licenses.noContent("Android Support Libraries", "Google Inc.", "https://developer.android.com/sdk/terms.html"))

        dataSet.add(Licenses.fromGitHub("google/ExoPlayer", LICENSE_APACHE_V2))

        dataSet.add(Licenses.noContent("Firebase", "Google Inc.", "https://firebase.google.com/terms/"))

        dataSet.add(Licenses.fromGitHub("google/guava", LICENSE_APACHE_V2))
        dataSet.add(Licenses.fromGitHubMIT("gowong/material-sheet-fab"))
        dataSet.add(Licenses.fromGitHubApacheV2("sjnyag/AnimationWrapLayout"))
        dataSet.add(Licenses.fromGitHubApacheV2("sjnyag/ForceAnimateAppBarLayout"))
        dataSet.add(Licenses.fromGitHubApacheV2("TakuSemba/Spotlight"))
        dataSet.add(Licenses.fromGitHubApacheV2("vajro/MaterialDesignLibrary"))
        dataSet.add(Licenses.fromGitHubApacheV2("garretyoder/Colorful"))
        dataSet.add(Licenses.fromGitHubApacheV2("jakewharton/DiskLruCache"))
        dataSet.add(Licenses.fromGitHubApacheV2("mikepenz/MaterialDrawer"))
        dataSet.add(Licenses.fromGitHubBSD("NanoHttpd/nanohttpd"))
        dataSet.add(Licenses.fromGitHubApacheV2("qhutch/ElevationImageView"))
        dataSet.add(Licenses.fromGitHubApacheV2("umano/AndroidSlidingUpPanel"))
        dataSet.add(Licenses.fromGitHubApacheV2("davideas/FlexibleAdapter"))
        dataSet.add(Licenses.fromGitHubApacheV2("dlew/joda-time-android"))
        dataSet.add(Licenses.fromGitHubApacheV2("yshrsmz/LicenseAdapter"))
        dataSet.add(Licenses.fromGitHubApacheV2("realm/realm-java"))
        val adapter = LicenseAdapter(dataSet)
        val list = RecyclerView(context, null)
        list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        list.adapter = adapter
        Licenses.load(dataSet)

        return AlertDialog.Builder(context)
                .setTitle(R.string.licence)
                .setView(list)
                .setPositiveButton(R.string.dialog_ok) { _, _ -> }
    }
}
