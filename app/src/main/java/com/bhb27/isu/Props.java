/*
 * Copyright (C) Felipe de Leon <fglfgl27@gmail.com>
 *
 * This file is part of iSu.
 *
 * iSu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * iSu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with iSu.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.bhb27.isu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatCheckBox;

import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.PreferenceFragment;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ArrayList;

import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.perapp.PropDB;

import com.afollestad.materialdialogs.MaterialDialog;

public class Props extends PreferenceFragment implements
Preference.OnPreferenceChangeListener {

    private String executableFilePath, suVersion;
    private ListPreference[] props = new ListPreference[Constants.props.length];
    private Preference mBuildFingerprint, mForceAllSafe, mForceAllUnsafe, mForceAllUnknown, mPropsAny, mPropsAnyList;
    private PreferenceCategory mPropsRemoveCat, mPropsEdit, mPropsSpecial, mPropsKnown, mPropsKnownList;
    private PreferenceScreen mPropsScreen;
    private boolean isCMSU, rootAccess;
    private AlertDialog.Builder mPerAppDialog;
    private Drawable originalIcon;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(Constants.PREF_NAME);
        addPreferencesFromResource(R.xml.props);
        rootAccess = Tools.rootAccess(getActivity());

        mPropsScreen = (PreferenceScreen) findPreference("props_pref_screen");

        mPropsRemoveCat = (PreferenceCategory) findPreference("props_remove");

        mPropsEdit = (PreferenceCategory) findPreference("props_ro");
        mPropsSpecial = (PreferenceCategory) findPreference("props_special");
        mPropsKnown = (PreferenceCategory) findPreference("know_props");
        mPropsKnownList = (PreferenceCategory) findPreference("know_props_list");

        suVersion = Tools.SuVersion(getActivity());
        isCMSU = Tools.SuVersionBool(suVersion);

        executableFilePath = getActivity().getFilesDir().getPath() + "/";

        String temp_value;
        for (int i = 0; i < Constants.props.length; i++) {
            temp_value = "";
            props[i] = (ListPreference) findPreference(Constants.props[i]);
            props[i].setOnPreferenceChangeListener(this);
            temp_value = Tools.getprop(Constants.props[i]);
            CharSequence[] entries = {
                Constants.props_OK[i] + " - " + getString(R.string.safe),
                Constants.props_NOK[i] + " - " + getString(R.string.unsafe),
                getString(R.string.unknown)
            };
            CharSequence[] entryValues = {
                Constants.props_OK[i],
                Constants.props_NOK[i],
                ""
            };
            props[i].setEntries(entries);
            props[i].setEntryValues(entryValues);

            mForceAllSafe = (Preference) findPreference("force_all_safe");
            mForceAllUnsafe = (Preference) findPreference("force_all_unsafe");
            mForceAllUnknown = (Preference) findPreference("force_all_unknown");

            mForceAllSafe.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new Execute(getActivity()).execute("green");
                    return true;
                }
            });
            mForceAllUnsafe.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new Execute(getActivity()).execute("red");
                    return true;
                }
            });
            mForceAllUnknown.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Tools.DoAToast(getString(R.string.unsafe_toast), getActivity());
                    return true;
                }
            });

            mBuildFingerprint = (Preference) findPreference(Constants.robuildfingerprint);
            originalIcon = mBuildFingerprint.getIcon();
            mBuildFingerprint.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // main dialog ask what to change
                    new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle)
                        .setTitle(getString(R.string.fingerprint_dialog_title))
                        .setMessage(getString(R.string.fingerprint_dialog_summary))
                        .setNeutralButton(getString(R.string.dismiss), //dissmiss
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    return;
                                }
                            })
                        .setPositiveButton(getString(R.string.fingerprint_dialog_know_safe), //know safe
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (!(Tools.getprop(Constants.robuildfingerprint)).equals(Constants.SAFEFINGERPRINT)) {
                                        Tools.resetprop(executableFilePath, Constants.robuildfingerprint, Constants.SAFEFINGERPRINT, getActivity(), true);
                                        finaldialog();
                                    } else
                                        Tools.DoAToast(getString(R.string.equals_values), getActivity());
                                }
                            })
                        .setNegativeButton(getString(R.string.fingerprint_dialog_personalized), //personalized
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // inner personalized dialog
                                    editonepropdialog(Constants.robuildfingerprint);
                                    return;
                                }
                            }).show();
                    return true;
                }
            });
        }

        mPropsAny = (Preference) findPreference("props_ro_edit");
        mPropsAny.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AnyPropDialog();
                return true;
            }
        });

        mPropsAnyList = (Preference) findPreference("props_ro_list");
        mPropsAnyList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                listdialog();
                return true;
            }
        });

        Tools.saveBoolean("prop_run", true, getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        rootAccess = Tools.rootAccess(getActivity());
        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(updatePropsReceiver);
        } catch (IllegalArgumentException ignored) {}
        Tools.closeSU();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        for (int i = 0; i < Constants.props.length; i++) {
            if (preference == props[i]) {
                String value = (String) objValue;
                updateprop(Constants.props[i], value);
                if (Constants.props[i].equals("ro.debuggable") && value.equals("0"))
                    Tools.stripadb(executableFilePath, getActivity());
            }
        }
        return true;
    }

    public void editonepropdialog(String prop) {
        final AppCompatEditText input = new AppCompatEditText(getActivity());
        input.setText(Tools.getprop(prop));
        new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle)
            .setTitle(getString(R.string.fingerprint_dialog_personalized_title) + prop)
            .setView(input)
            .setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newvalue = input.getText().toString();
                        if (newvalue.isEmpty()) {
                            Tools.DoAToast(getString(R.string.edited_text), getActivity());
                            return;
                        } else if (newvalue.equals(Tools.getprop(prop)))
                            Tools.DoAToast(getString(R.string.equals_values), getActivity());
                        else {
                            Tools.resetprop(executableFilePath, prop, newvalue, getActivity(), true);
                            finaldialog();
                        }
                        return;
                    }
                })
            .setNegativeButton(getString(R.string.dismiss),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                }).show();
    }

    public void finaldialog() {
        new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle)
            .setTitle(getString(R.string.fingerprint_dialog_result))
            .setNegativeButton(getString(R.string.dismiss),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                }).show();
        updateState();
    }

    public void listdialog() {
        new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle)
            .setTitle(getString(R.string.props_any_list_dialog_title))
            .setMessage(String.format(getString(R.string.props_any_list_dialog_summary),
                "\"" + getString(R.string.props_apply_boot) + "\"" + "\n\n" + List_props()))
            .setNegativeButton(getString(R.string.dismiss),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                }).show();
        updateState();
    }

    public void updateprop(String prop, String value) {
        Tools.resetprop(executableFilePath, prop, value, getActivity(), false);
        updateState();
    }

    private final BroadcastReceiver updatePropsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState();
        }
    };

    private void updateState() {
        if (rootAccess) {
            mPropsScreen.removePreference(mPropsRemoveCat);
            String[] value = new String[Constants.props.length];
            String summary = "";
            for (int i = 0; i < Constants.props.length; i++) {
                value[i] = Tools.getprop(Constants.props[i]);
                if (value[i] == null || value[i].isEmpty()) {
                    props[i].setValue("");
                    summary = getString(R.string.unknown);
                    props[i].setSummary(summary);
                    props[i].setIcon(R.drawable.interrogation);
                } else {
                    props[i].setValue(value[i]);
                    summary = value[i];
                    props[i].setSummary(summary);
                    props[i].setIcon(summary.equals(Constants.props_OK[i]) ? R.drawable.ok : R.drawable.warning);
                }
            }
            String BuildFingerprint = Build.FINGERPRINT;
            String RoBuildFingerprint = Tools.getprop(Constants.robuildfingerprint);
            if (Tools.getprop(Constants.robuildfingerprint).equals(Tools.getprop(Constants.robootbuildfingerprint))) {
                mBuildFingerprint.setSummary(Build.FINGERPRINT + getString(R.string.fingerprint_help));
                // use setIcon(Drawable) instead of setIcon(int) to avoid falls back to the previously-set in a new Drawable that is null
                mBuildFingerprint.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.warning));
            } else if (RoBuildFingerprint.equals(BuildFingerprint)) {
                mBuildFingerprint.setSummary(Build.FINGERPRINT);
                mBuildFingerprint.setIcon(originalIcon);
            } else {
                mBuildFingerprint.setSummary(RoBuildFingerprint + getString(R.string.fingerprint_apply));
                mBuildFingerprint.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.exclamation));
            }

            try {
                getActivity().registerReceiver(updatePropsReceiver, new IntentFilter("updatePropsReceiver"));
            } catch (NullPointerException ignored) {}
        } else {
            mPropsEdit.setEnabled(false);
            mPropsSpecial.setEnabled(false);
            mPropsKnown.setEnabled(false);
            mPropsKnownList.setEnabled(false);
        }
    }

    private void AnyPropDialog() {
        mPerAppDialog = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
        mPerAppDialog.setTitle(getString(R.string.props_any_edit_dialog_title));
        mPerAppDialog.setCancelable(true);

        final List < Integer > mSelectedBox = new ArrayList < Integer > ();
        final List < Integer > mDeSelectedBox = new ArrayList < Integer > ();

        final String[] mapplist = Tools.getallprop(executableFilePath, getActivity());

        final boolean[] checkedValues = new boolean[mapplist.length];
        for (int i = 0; i < mapplist.length; i++)
            checkedValues[i] = false;

        // Specify the list array, the items to be selected by default (null for none),
        // and the listener through which to receive callbacks when items are selected
        mPerAppDialog.setMultiChoiceItems(mapplist, checkedValues,
            new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which,
                    boolean isChecked) {
                    if (isChecked) {
                        // If the user checked the item, add it to the selected items
                        mSelectedBox.add(which);
                    }
                    if (!isChecked) {
                        mDeSelectedBox.add(which);
                    }
                    if (!isChecked && mSelectedBox.contains(which)) {
                        // Else, if the item is already in the array, remove it
                        mSelectedBox.remove(Integer.valueOf(which));
                    }
                }
            });

        // Set the action buttons
        mPerAppDialog.setPositiveButton(getString(R.string.edit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if ((mSelectedBox != null) && (mSelectedBox.size() > 0)) {
                    String[] PropsTo = new String[mSelectedBox.size()];
                    for (int i = 0; i < mSelectedBox.size(); i++) {
                        int selected = mSelectedBox.get(i);
                        PropsTo[i] = mapplist[selected];
                    }
                    EditProps(PropsTo);
                    return;
                }
                Tools.DoAToast(getString(R.string.props_any_no_selected), getActivity());
                return;
            }
        });
        mPerAppDialog.setNegativeButton(getString(R.string.dismiss), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        mPerAppDialog.create();
        mPerAppDialog.show();

    }

    public void save_prop(String app, String id) {
        PropDB propDB = new PropDB(getActivity());
        List < PropDB.PerAppItem > PerAppItem = propDB.getAllProps();
        for (int i = 0; i < PerAppItem.size(); i++) {
            String p = PerAppItem.get(i).getApp();
            if (p != null && p.equals(app)) {
                propDB.delApp(i);
            }
        }
        propDB.putApp(app, id);
        propDB.commit(getActivity());
    }
    //TODO fix this odd looking variables
    public String List_props() {
        PropDB propDB = new PropDB(getActivity());
        List < PropDB.PerAppItem > PropItem = propDB.getAllProps();
        final String[] props = new String[PropItem.size()];
        for (int i = 0; i < PropItem.size(); i++) {
            String prop = PropItem.get(i).getApp();
            String value = PropItem.get(i).getID();
            if (prop != null && value != null)
                props[i] = prop + "=" + value;
        }

        String result = "";
        for (int i = 0; i < props.length; i++) {
            result = ((i < (props.length - 1)) ? result + props[i] + "\n" : result + props[i]);
        }
        if (props.length == 0)
            return getString(R.string.props_no_changes);
        return result;
    }

    private void EditProps(String[] props) {
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(30, 20, 30, 20);

        TextView descriptionText = new TextView(getActivity());
        descriptionText.setText(getString(R.string.props_any_edit_dialog_summary));
        linearLayout.addView(descriptionText);

        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.setPadding(0, 0, 0, 10);
        linearLayout.addView(scrollView);

        LinearLayout editLayout = new LinearLayout(getActivity());
        editLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(editLayout);

        final AppCompatEditText[] EditProps = new AppCompatEditText[props.length];
        final AppCompatCheckBox[] ForceBP = new AppCompatCheckBox[props.length];
        final TextView[] descriptionAboveText = new TextView[props.length];
        final TextView[] descriptionBelowText = new TextView[props.length];

        for (int i = 0; i < props.length; i++) {
            descriptionAboveText[i] = new TextView(getActivity());
            descriptionAboveText[i].setText(String.format(getString(R.string.empty), props[i]));
            editLayout.addView(descriptionAboveText[i]);

            EditProps[i] = new AppCompatEditText(getActivity());
            EditProps[i].setText(Tools.getprop(props[i]));
            editLayout.addView(EditProps[i]);

            descriptionBelowText[i] = new TextView(getActivity());
            descriptionBelowText[i].setText(getString(R.string.props_any_edit_dialog_already_bp));
            ForceBP[i] = new AppCompatCheckBox(getActivity());
            ForceBP[i].setText(getString(R.string.props_any_edit_dialog_force_bp));

            if (Tools.PropIsinbp(props[i], getActivity()))
                editLayout.addView(descriptionBelowText[i]);
            else
                editLayout.addView(ForceBP[i]);
        }

        new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle)
            .setTitle(getString(R.string.props_any_edit_dialog_title))
            .setView(linearLayout)
            .setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                })
            .setPositiveButton(getString(R.string.apply), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int id) {
                    String finalmenssage = "\n", edited;
                    for (int i = 0; i < props.length; i++) {
                        edited = EditProps[i].getText().toString();
                        if (edited.isEmpty()) {
                            finalmenssage = finalmenssage + String.format(getString(R.string.edited_text_ro), props[i]);
                        } else if (edited.equals(Tools.getprop(props[i])))
                            finalmenssage = finalmenssage + String.format(getString(R.string.edited_text_equals), props[i]);
                        else {
                            if (((AppCompatCheckBox) ForceBP[i]).isChecked())
                                Tools.resetprop(executableFilePath, props[i], edited, getActivity(), true);
                            else
                                Tools.resetprop(executableFilePath, props[i], edited, getActivity(), false);
                            finalmenssage = finalmenssage + String.format(getString(R.string.edited_text_ok), props[i]);
                            save_prop(props[i], edited);
                        }
                        finalmenssage = finalmenssage + "\n";
                    }
                    finaldialogro(finalmenssage);
                    return;
                }
            }).show();
    }

    public void finaldialogro(String finalmenssage) {
        new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle)
            .setTitle(getString(R.string.edited_text_result))
            .setMessage(finalmenssage + getString(R.string.edited_text_sumary))
            .setNegativeButton(getString(R.string.dismiss),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                }).show();
        updateState();
    }

    private static class Execute extends AsyncTask < String, Void, Void > {
        private MaterialDialog progressDialog;
        private WeakReference < Context > contextRef;

        public Execute(Context context) {
            contextRef = new WeakReference < > (context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Context mContext = contextRef.get();
            progressDialog = new MaterialDialog.Builder(mContext)
                .title(mContext.getString(R.string.app_name))
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .show();
        }

        @Override
        protected Void doInBackground(String...params) {
            Context mContext = contextRef.get();
            String executableFilePath = mContext.getFilesDir().getPath() + "/";
            if (params[0].equals("green")) {
                progressDialog.setContent(String.format(mContext.getString(R.string.setting_all),
                    mContext.getString(R.string.know_props)) + mContext.getString(R.string.safe) + "...");
                Tools.resetallprop(executableFilePath, true, mContext);
                Tools.stripadb(executableFilePath, mContext);
            } else if (params[0].equals("red")) {
                progressDialog.setContent(String.format(mContext.getString(R.string.setting_all),
                    mContext.getString(R.string.know_props)) + mContext.getString(R.string.unsafe) + "...");
                Tools.resetallprop(executableFilePath, false, mContext);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Context mContext = contextRef.get();
            progressDialog.dismiss();
            Tools.SendBroadcast("updatePropsReceiver", mContext);
        }
    }

}
