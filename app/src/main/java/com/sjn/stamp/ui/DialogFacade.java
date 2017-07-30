package com.sjn.stamp.ui;

import android.content.Context;
import android.content.DialogInterface;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.sjn.stamp.R;

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
}
