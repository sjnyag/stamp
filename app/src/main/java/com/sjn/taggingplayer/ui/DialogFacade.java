package com.sjn.taggingplayer.ui;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.sjn.taggingplayer.R;

public class DialogFacade {
    public static MaterialDialog.Builder createTagDeleteDialog(final Context context, String tag, MaterialDialog.SingleButtonCallback callback) {
        return new MaterialDialog.Builder(context)
                .title("タグの削除")
                .content("タグ「" + tag + "」を削除しますか？")
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
}
