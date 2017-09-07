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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;

import java.util.List;
import java.util.ArrayList;

import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.perapp.PerAppMonitor;
import com.bhb27.isu.perapp.Per_App;

public class Monitor extends PreferenceFragment implements
Preference.OnPreferenceChangeListener {

    private Preference mPerAppDontCare, mPerAppActive, mPerAppDeactive, mMonitorView, mMonitorWarning;
    private PreferenceCategory mMonitor;
    private SwitchPreference mAutoRestartSU, mAutoRestartISU;
    private AlertDialog.Builder mPerAppDialog;
    private String TAG = Constants.TAG, suVersion;
    private boolean isCMSU;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(Constants.PREF_NAME);
        addPreferencesFromResource(R.xml.monitor);

        suVersion = Tools.SuVersion(getActivity());
        isCMSU = Tools.SuVersionBool(suVersion);

        mMonitor = (PreferenceCategory) findPreference("monitor_su");

        mPerAppDontCare = (Preference) findPreference("per_app_dontcare");
        mPerAppActive = (Preference) findPreference("per_app_active");
        mAutoRestartSU = (SwitchPreference) findPreference("auto_restart_su");
        mAutoRestartISU = (SwitchPreference) findPreference("auto_restart_isu");
        mPerAppDeactive = (Preference) findPreference("per_app_deactive");
        mMonitorView = (Preference) findPreference("per_app_view");
        mMonitorWarning = (Preference) findPreference("per_app_warning");

        mAutoRestartSU.setOnPreferenceChangeListener(this);
        mAutoRestartISU.setOnPreferenceChangeListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();
        Tools.closeSU();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mAutoRestartISU) {
            boolean isChecked = (Boolean) objValue;
            boolean restartsu = Tools.getBoolean("auto_restart_su", false, getActivity());
            if (isChecked && restartsu) {
                Tools.saveBoolean("auto_restart_su", !isChecked, getActivity());
                mAutoRestartSU.setChecked(!isChecked);
                Tools.DoAToast(getString(R.string.auto_restart_vice_versa), getActivity());
            }
        } else if (preference == mAutoRestartSU) {
            boolean isChecked = (Boolean) objValue;
            boolean restartisu = Tools.getBoolean("auto_restart_isu", false, getActivity());
            if (isChecked && restartisu) {
                Tools.saveBoolean("auto_restart_isu", !isChecked, getActivity());
                mAutoRestartISU.setChecked(!isChecked);
                Tools.DoAToast(getString(R.string.auto_restart_vice_versa), getActivity());
            }
        }
        return true;
    }

    private void updateState() {
        if (!isCMSU) {
            updatePrefs(false);
            mMonitor.removePreference(mMonitorWarning);
            if (!Tools.rootAccess(getActivity()))
                mMonitorView.setSummary(getString(R.string.device_not_root));
        } else {
            mMonitor.removePreference(mMonitorView);
            mMonitor.addPreference(mMonitorWarning);
            if (!Per_App.isAccessibilityEnabled(getActivity(), PerAppMonitor.accessibilityId)) {
                updatePrefs(false);
                mMonitorWarning.setIcon(R.drawable.warning);
                mMonitorWarning.setSummary(getString(R.string.monitor_warning));
                mMonitorWarning.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
                        return true;
                    }
                });
            } else {
                updatePrefs(true);
                mMonitorWarning.setIcon(R.drawable.ok);
                mMonitorWarning.setSummary(getString(R.string.monitor_ok));
                mMonitorWarning.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
                        return true;
                    }
                });
                mPerAppDontCare.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        PerAppDialog("dont");
                        return true;
                    }
                });

                mPerAppActive.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        PerAppDialog("Su");
                        return true;
                    }
                });

                mPerAppDeactive.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        PerAppDialog("iSu");
                        return true;
                    }
                });
            }
        }
    }

    private void updatePrefs(boolean state) {
        mPerAppDontCare.setEnabled(state);
        mPerAppActive.setEnabled(state);
        mAutoRestartSU.setEnabled(state);
        mAutoRestartISU.setEnabled(state);
        mPerAppDeactive.setEnabled(state);
    }

    private void PerAppDialog(String id) {
        if (!Per_App.isAccessibilityEnabled(getActivity(), PerAppMonitor.accessibilityId)) {
            startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
        } else {

            mPerAppDialog = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
            if (id.equals("iSu"))
                mPerAppDialog.setTitle(getString(R.string.per_app_title_isu));
            else if (id.equals("Su"))
                mPerAppDialog.setTitle(getString(R.string.per_app_title_su));
            else if (id.equals("dont"))
                mPerAppDialog.setTitle(getString(R.string.per_app_dontcare));
            mPerAppDialog.setCancelable(true);

            final List < Per_App.App > apps = Per_App.getInstalledApps(getActivity());

            final String[] packagelist = Per_App.getPackageNames(apps);
            final String[] mapplist = Per_App.getAppNames(apps, id, getActivity());

            final String profile_id = id;
            final List < Integer > mSelectedApps = new ArrayList < Integer > ();
            final List < Integer > mDeSelectedApps = new ArrayList < Integer > ();

            final boolean[] checkedValues = Per_App.getExistingSelections(packagelist, profile_id, getActivity());

            // Specify the list array, the items to be selected by default (null for none),
            // and the listener through which to receive callbacks when items are selected
            mPerAppDialog.setMultiChoiceItems(mapplist, checkedValues,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                        boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            mSelectedApps.add(which);
                        }
                        if (!isChecked) {
                            mDeSelectedApps.add(which);
                        }
                        if (!isChecked && mSelectedApps.contains(which)) {
                            // Else, if the item is already in the array, remove it
                            mSelectedApps.remove(Integer.valueOf(which));
                        }
                    }
                });

            // Set the action buttons
            mPerAppDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK, so save the mSelectedItems results somewhere
                    // or return them to the component that opened the dialog
                    if (mSelectedApps != null) {
                        for (int i = 0; i < mSelectedApps.size(); i++) {
                            int y = mSelectedApps.get(i);
                            String packageName = packagelist[y];

                            Log.d(TAG, "Saving " + packageName + " to " + profile_id);
                            Per_App.save_app(packageName, profile_id, getActivity());
                        }
                    }
                    if (mDeSelectedApps != null) {
                        for (int i = 0; i < mDeSelectedApps.size(); i++) {
                            int y = mDeSelectedApps.get(i);
                            Per_App.remove_app(packagelist[y], profile_id, getActivity());
                        }
                    }


                }
            });
            mPerAppDialog.setNegativeButton(getString(R.string.dismiss), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            mPerAppDialog.create();
            mPerAppDialog.show();
        }
    }
}
