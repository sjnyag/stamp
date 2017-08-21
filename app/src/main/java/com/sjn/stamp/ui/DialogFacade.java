package com.sjn.stamp.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.common.GoogleApiAvailability;
import com.sjn.stamp.R;

import net.yslibrary.licenseadapter.LicenseAdapter;
import net.yslibrary.licenseadapter.LicenseEntry;
import net.yslibrary.licenseadapter.Licenses;

import java.util.ArrayList;
import java.util.List;

import static net.yslibrary.licenseadapter.Licenses.LICENSE_APACHE_V2;

public class DialogFacade {
    public static MaterialDialog.Builder createConfirmDialog(final Context context, int content, MaterialDialog.SingleButtonCallback callback, final MaterialDialog.OnDismissListener dismissListener) {
        return new MaterialDialog.Builder(context)
                .title(R.string.dialog_confirm_title)
                .content(content)
                .positiveText(R.string.dialog_ok)
                .negativeText(R.string.dialog_cancel)
                .onAny(callback)
                .dismissListener(dismissListener)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .theme(Theme.DARK);
    }

    public static MaterialDialog.Builder createPermissionNecessaryDialog(final Context context, MaterialDialog.SingleButtonCallback callback) {
        return new MaterialDialog.Builder(context)
                .title(R.string.dialog_permission_necessary_title)
                .content(R.string.dialog_permission_necessary_content)
                .positiveText(R.string.dialog_ok)
                .onAny(callback)
                .cancelable(false)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .theme(Theme.DARK);
    }

    public static MaterialDialog.Builder createRestartDialog(final Context context, MaterialDialog.SingleButtonCallback callback) {
        return new MaterialDialog.Builder(context)
                .title(R.string.dialog_restart_title)
                .content(R.string.dialog_restart_content)
                .positiveText(R.string.dialog_ok)
                .onAny(callback)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .theme(Theme.DARK);
    }

    public static MaterialDialog.Builder createStampDeleteDialog(final Context context, String stamp, MaterialDialog.SingleButtonCallback callback) {
        return new MaterialDialog.Builder(context)
                .title(R.string.dialog_delete_stamp_title)
                .content(R.string.dialog_delete_stamp_content, stamp)
                .positiveText(R.string.dialog_delete)
                .negativeText(R.string.dialog_cancel)
                .onAny(callback)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .theme(Theme.DARK);
    }

    public static MaterialDialog.Builder createLetsPlayMusicDialog(final Context context) {
        return new MaterialDialog.Builder(context)
                .title(R.string.dialog_lets_play_title)
                .positiveText(R.string.dialog_ok)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .theme(Theme.DARK);
    }

    public static MaterialDialog.Builder createHistoryDeleteDialog(final Context context, String history, MaterialDialog.SingleButtonCallback callback, DialogInterface.OnDismissListener dismissListener) {
        return new MaterialDialog.Builder(context)
                .title(R.string.dialog_delete_history_title)
                .content(R.string.dialog_delete_history_content, history)
                .positiveText(R.string.dialog_delete)
                .negativeText(R.string.dialog_cancel)
                .onAny(callback)
                .dismissListener(dismissListener)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .theme(Theme.DARK);
    }

    public static MaterialDialog.Builder createRetrieveMediaDialog(final Context context, final MaterialDialog.SingleButtonCallback callback, final MaterialDialog.OnDismissListener dismissListener) {
        return new MaterialDialog.Builder(context)
                .title(R.string.dialog_reload_music_title)
                .content(R.string.dialog_reload_music_content)
                .positiveText(R.string.dialog_reload)
                .negativeText(R.string.dialog_cancel)
                .onAny(callback)
                .dismissListener(dismissListener)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .theme(Theme.DARK);
    }

    public static MaterialDialog.Builder createLicenceDialog(final Context context) {
        List<LicenseEntry> dataSet = new ArrayList<>();
        dataSet.add(Licenses.noContent("Android SDK", "Google Inc.", "https://developer.android.com/sdk/terms.html"));
        dataSet.add(Licenses.noLink("Google Play Services", "Google Inc.", GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(context)));
        dataSet.add(Licenses.noContent("Android Support Libraries", "Google Inc.", "https://developer.android.com/sdk/terms.html"));
        dataSet.add(Licenses.noContent("Firebase", "Google Inc.", "https://firebase.google.com/terms/"));
        dataSet.add(Licenses.fromGitHub("google/guava", LICENSE_APACHE_V2));
        dataSet.add(Licenses.fromGitHubMIT("gowong/material-sheet-fab"));
        dataSet.add(Licenses.fromGitHubMIT("afollestad/material-dialogs"));
        dataSet.add(Licenses.fromGitHubApacheV2("vajro/MaterialDesignLibrary"));
        dataSet.add(Licenses.fromGitHubApacheV2("sjnyag/AnimationWrapLayout"));
        dataSet.add(Licenses.fromGitHubApacheV2("TakuSemba/Spotlight"));
        dataSet.add(Licenses.fromGitHubApacheV2("jakewharton/DiskLruCache"));
        dataSet.add(Licenses.fromGitHubApacheV2("mikepenz/MaterialDrawer"));
        dataSet.add(Licenses.fromGitHubBSD("NanoHttpd/nanohttpd"));
        dataSet.add(Licenses.fromGitHubApacheV2("umano/AndroidSlidingUpPanel"));
        dataSet.add(Licenses.fromGitHubApacheV2("square/picasso"));
        dataSet.add(Licenses.fromGitHubApacheV2("davideas/FlexibleAdapter"));
        dataSet.add(Licenses.fromGitHubApacheV2("dlew/joda-time-android"));
        dataSet.add(Licenses.fromGitHubApacheV2("yshrsmz/LicenseAdapter"));
        dataSet.add(Licenses.fromGitHubApacheV2("realm/realm-java"));
        LicenseAdapter adapter = new LicenseAdapter(dataSet);
        RecyclerView list = new RecyclerView(context, null);
        list.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        list.setAdapter(adapter);
        Licenses.load(dataSet);

        return new MaterialDialog.Builder(context)
                .title(R.string.licence)
                .customView(list, true)
                .positiveText(R.string.dialog_ok)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .theme(Theme.DARK);
    }
}
