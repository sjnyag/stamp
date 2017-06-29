package com.sjn.stamp.ui;

import android.content.Context;
import android.content.DialogInterface;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.sjn.stamp.R;

public class DialogFacade {
    public static MaterialDialog.Builder createStampDeleteDialog(final Context context, String stamp, MaterialDialog.SingleButtonCallback callback) {
        return new MaterialDialog.Builder(context)
                .title("タグの削除")
                .content("タグ「" + stamp + "」を削除しますか？")
                .positiveText("削除する")
                .negativeText("キャンセル")
                .onAny(callback)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .theme(Theme.DARK);
    }

    public static MaterialDialog.Builder createLetsPlayMusicDialog(final Context context) {
        return new MaterialDialog.Builder(context)
                .title("まだ音楽の再生履歴がありません")
                .positiveText("OK")
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .theme(Theme.DARK);
    }
    public static MaterialDialog.Builder createHistoryDeleteDialog(final Context context, String history, MaterialDialog.SingleButtonCallback callback, DialogInterface.OnDismissListener dismissListener) {
        return new MaterialDialog.Builder(context)
                .title("再生履歴の削除")
                .content("再生履歴「" + history + "」を削除しますか？")
                .positiveText("削除する")
                .negativeText("キャンセル")
                .onAny(callback)
                .dismissListener(dismissListener)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .theme(Theme.DARK);
    }
    public static MaterialDialog.Builder createRetrieveMediaDialog(final Context context,  MaterialDialog.SingleButtonCallback callback) {
        return new MaterialDialog.Builder(context)
                .title("音楽ファイルの再ロード")
                .content("端末内の音楽ファイルの再ロードを実施します。（ファイルの量によっては少し時間がかかります。）よろしいですか？")
                .positiveText("再ロードする")
                .negativeText("キャンセル")
                .onAny(callback)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .theme(Theme.DARK);
    }
}
