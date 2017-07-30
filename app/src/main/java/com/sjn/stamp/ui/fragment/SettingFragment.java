package com.sjn.stamp.ui.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sjn.stamp.R;
import com.sjn.stamp.ui.DialogFacade;
import com.sjn.stamp.ui.activity.DrawerActivity;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.RealmHelper;

public class SettingFragment extends PreferenceFragmentCompat {

    private static final String TAG = LogHelper.makeLogTag(SettingFragment.class);

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findPreference(getString(R.string.import_backup)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFacade.createConfirmDialog(getActivity(), R.string.dialog_confirm_import, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case NEGATIVE:
                                return;
                            case POSITIVE:
                                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                                progressDialog.setMessage("処理中");
                                progressDialog.show();
                                RealmHelper.importBackUp(getActivity());
                                progressDialog.dismiss();
                                DialogFacade.createRestartDialog(getActivity(), new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        getActivity().recreate();
                                    }
                                }).show();
                                return;
                        }
                    }
                }, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                }).show();
                return true;
            }
        });

        findPreference(getString(R.string.export_backup)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                DialogFacade.createConfirmDialog(getActivity(), R.string.dialog_confirm_export, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case NEGATIVE:
                                return;
                            case POSITIVE:
                                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                                progressDialog.setMessage("処理中");
                                progressDialog.show();
                                RealmHelper.exportBackUp(getActivity());
                                progressDialog.dismiss();
                                return;
                        }
                    }
                }, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                }).show();
                return true;
            }
        });

        findPreference(getString(R.string.licence)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((DrawerActivity) getActivity()).navigateToBrowser(new SettingFragment(), true);
                return true;
            }
        });
    }

}
