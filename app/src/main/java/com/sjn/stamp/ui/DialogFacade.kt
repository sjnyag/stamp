package com.sjn.stamp.ui

import android.content.Context
import android.content.DialogInterface
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sjn.stamp.R
import net.yslibrary.licenseadapter.LicenseAdapter
import net.yslibrary.licenseadapter.LicenseEntry
import net.yslibrary.licenseadapter.Licenses
import net.yslibrary.licenseadapter.Licenses.LICENSE_APACHE_V2
import java.util.*

object DialogFacade {
    fun createConfirmDialog(context: Context, content: String, callback: (Any, Any) -> Unit, dismissListener: DialogInterface.OnDismissListener): MaterialDialog.Builder {
        return MaterialDialog.Builder(context)
                .title(R.string.dialog_confirm_title)
                .content(content)
                .positiveText(R.string.dialog_ok)
                .negativeText(R.string.dialog_cancel)
                .onAny(callback)
                .dismissListener(dismissListener)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey)
                .theme(Theme.DARK)
    }

    fun createConfirmDialog(context: Context, content: Int, callback: (Any, Any) -> Unit, dismissListener: DialogInterface.OnDismissListener): MaterialDialog.Builder {
        return MaterialDialog.Builder(context)
                .title(R.string.dialog_confirm_title)
                .content(content)
                .positiveText(R.string.dialog_ok)
                .negativeText(R.string.dialog_cancel)
                .onAny(callback)
                .dismissListener(dismissListener)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey)
                .theme(Theme.DARK)
    }

    fun createPermissionNecessaryDialog(context: Context, callback: (Any, Any) -> Unit): MaterialDialog.Builder {
        return MaterialDialog.Builder(context)
                .title(R.string.dialog_permission_necessary_title)
                .content(R.string.dialog_permission_necessary_content)
                .positiveText(R.string.dialog_ok)
                .onAny(callback)
                .cancelable(false)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey)
                .theme(Theme.DARK)
    }

    fun createRestartDialog(context: Context, callback: (Any, Any) -> Unit): MaterialDialog.Builder {
        return MaterialDialog.Builder(context)
                .title(R.string.dialog_restart_title)
                .content(R.string.dialog_restart_content)
                .positiveText(R.string.dialog_ok)
                .onAny(callback)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey)
                .theme(Theme.DARK)
    }

    fun createRemoveStampSongDialog(context: Context, stamp: String, callback: MaterialDialog.SingleButtonCallback): MaterialDialog.Builder {
        return MaterialDialog.Builder(context)
                .title(R.string.dialog_delete_stamp_title)
                .content(R.string.dialog_delete_stamp_content, stamp)
                .positiveText(R.string.dialog_delete)
                .negativeText(R.string.dialog_cancel)
                .onAny(callback)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey)
                .theme(Theme.DARK)
    }

    fun createLetsPlayMusicDialog(context: Context): MaterialDialog.Builder {
        return MaterialDialog.Builder(context)
                .title(R.string.dialog_lets_play_title)
                .positiveText(R.string.dialog_ok)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey)
                .theme(Theme.DARK)
    }

    fun createRemoveStampSongDialog(context: Context, song: String, stamp: String, callback: MaterialDialog.SingleButtonCallback, dismissListener: DialogInterface.OnDismissListener): MaterialDialog.Builder {
        return MaterialDialog.Builder(context)
                .title(R.string.dialog_remove_stamp_title)
                .content(R.string.dialog_remove_stamp_content, song, stamp)
                .positiveText(R.string.dialog_delete)
                .negativeText(R.string.dialog_cancel)
                .onAny(callback)
                .dismissListener(dismissListener)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey)
                .theme(Theme.DARK)
    }

    fun createHistoryDeleteDialog(context: Context, history: String, callback: MaterialDialog.SingleButtonCallback, dismissListener: DialogInterface.OnDismissListener): MaterialDialog.Builder {
        return MaterialDialog.Builder(context)
                .title(R.string.dialog_delete_history_title)
                .content(R.string.dialog_delete_history_content, history)
                .positiveText(R.string.dialog_delete)
                .negativeText(R.string.dialog_cancel)
                .onAny(callback)
                .dismissListener(dismissListener)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey)
                .theme(Theme.DARK)
    }

    fun createRetrieveMediaDialog(context: Context, callback: MaterialDialog.SingleButtonCallback, dismissListener: DialogInterface.OnDismissListener): MaterialDialog.Builder {
        return MaterialDialog.Builder(context)
                .title(R.string.dialog_reload_music_title)
                .content(R.string.dialog_reload_music_content)
                .positiveText(R.string.dialog_reload)
                .negativeText(R.string.dialog_cancel)
                .onAny(callback)
                .dismissListener(dismissListener)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey)
                .theme(Theme.DARK)
    }

    fun createLicenceDialog(context: Context): MaterialDialog.Builder {
        val dataSet = ArrayList<LicenseEntry>()
        dataSet.add(Licenses.noContent("Android SDK", "Google Inc.", "https://developer.android.com/sdk/terms.html"))
        //TODO
        //        dataSet.add(Licenses.noLink("Google Play Services", "Google Inc.", GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(context)));
        dataSet.add(Licenses.noContent("Android Support Libraries", "Google Inc.", "https://developer.android.com/sdk/terms.html"))
        dataSet.add(Licenses.noContent("Firebase", "Google Inc.", "https://firebase.google.com/terms/"))
        dataSet.add(Licenses.fromGitHub("google/guava", LICENSE_APACHE_V2))
        dataSet.add(Licenses.fromGitHubMIT("gowong/material-sheet-fab"))
        dataSet.add(Licenses.fromGitHubMIT("afollestad/material-dialogs"))
        dataSet.add(Licenses.fromGitHubApacheV2("vajro/MaterialDesignLibrary"))
        dataSet.add(Licenses.fromGitHubApacheV2("sjnyag/AnimationWrapLayout"))
        dataSet.add(Licenses.fromGitHubApacheV2("sjnyag/ForceAnimateAppBarLayout"))
        dataSet.add(Licenses.fromGitHubApacheV2("TakuSemba/Spotlight"))
        dataSet.add(Licenses.fromGitHubApacheV2("jakewharton/DiskLruCache"))
        dataSet.add(Licenses.fromGitHubApacheV2("mikepenz/MaterialDrawer"))
        dataSet.add(Licenses.fromGitHubBSD("NanoHttpd/nanohttpd"))
        dataSet.add(Licenses.fromGitHubApacheV2("umano/AndroidSlidingUpPanel"))
        dataSet.add(Licenses.fromGitHubApacheV2("square/picasso"))
        dataSet.add(Licenses.fromGitHubApacheV2("davideas/FlexibleAdapter"))
        dataSet.add(Licenses.fromGitHubApacheV2("dlew/joda-time-android"))
        dataSet.add(Licenses.fromGitHubApacheV2("yshrsmz/LicenseAdapter"))
        dataSet.add(Licenses.fromGitHubApacheV2("realm/realm-java"))
        val adapter = LicenseAdapter(dataSet)
        val list = RecyclerView(context, null)
        list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        list.adapter = adapter
        Licenses.load(dataSet)

        return MaterialDialog.Builder(context)
                .title(R.string.licence)
                .customView(list, true)
                .positiveText(R.string.dialog_ok)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey)
                .theme(Theme.DARK)
    }
}
