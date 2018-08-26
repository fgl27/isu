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
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;

import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.Tools;

public class Controls extends PreferenceFragment implements
Preference.OnPreferenceChangeListener {

    private SwitchPreference mSuSwitch, mSelSwitch, mFakeSelSwitch, mDebug, mDebugRoot;
    private Preference mControlsView, mTasker;
    private PreferenceCategory mControls;
    private String suVersion;
    private boolean isCMSU, rootAccess;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(Constants.PREF_NAME);
        addPreferencesFromResource(R.xml.controls);

        suVersion = Tools.SuVersion(getActivity());
        isCMSU = Tools.SuVersionBool(suVersion);
        rootAccess = Tools.rootAccess(getActivity());

        mControls = (PreferenceCategory) findPreference("controls_su");

        mSuSwitch = (SwitchPreference) findPreference("su_switch");
        mSuSwitch.setOnPreferenceChangeListener(this);

        mSelSwitch = (SwitchPreference) findPreference("selinux_switch");
        mSelSwitch.setOnPreferenceChangeListener(this);

        mFakeSelSwitch = (SwitchPreference) findPreference("fake_selinux_switch");
        mFakeSelSwitch.setOnPreferenceChangeListener(this);

        mDebug = (SwitchPreference) findPreference("android_debug");
        mDebug.setOnPreferenceChangeListener(this);

        mDebugRoot = (SwitchPreference) findPreference("android_debug_root");
        mDebugRoot.setOnPreferenceChangeListener(this);

        mControlsView = (Preference) findPreference("controls_view");

        mTasker = (Preference) findPreference("tasker");
        mTasker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String isu_file = Tools.readString("cmiyc", null, getActivity());

                ViewGroup base_parent = (ViewGroup) getActivity().findViewById(R.id.base_parent);
                View alertLayout = LayoutInflater.from(getActivity()).inflate(R.layout.tasker_dialog, base_parent, false);

                final String[] cmds = new String[] {
                        Constants.TASKER_SU_ON,
                        Constants.TASKER_SU_OFF,
                        Constants.TASKER_SU_INV,
                        Constants.TASKER_SELINUX_ON,
                        Constants.TASKER_SELINUX_OFF,
                        Constants.TASKER_SELINUX_INV,
                        Constants.TASKER_DEBUG_ON,
                        Constants.TASKER_DEBUG_OFF,
                        Constants.TASKER_DEBUG_INV
                };

                final int[] cmdIDs = new int[] {
                        R.id.TASKER_SU_ON,
                        R.id.TASKER_SU_OFF,
                        R.id.TASKER_SU_INV,
                        R.id.TASKER_SELINUX_ON,
                        R.id.TASKER_SELINUX_OFF,
                        R.id.TASKER_SELINUX_INV,
                        R.id.TASKER_DEBUG_ON,
                        R.id.TASKER_DEBUG_OFF,
                        R.id.TASKER_DEBUG_INV
                };

                final TextView[] cmdText = new TextView[cmds.length];

                for (int i = 0; i < cmds.length; i++) {
                    cmdText[i] = (TextView) alertLayout.findViewById(cmdIDs[i]);
                    cmdText[i].setText(TaskerDialogcmd(cmds[i], isu_file));
                }

                new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle)
                    .setTitle(getString(R.string.tasker))
                    .setView(alertLayout)
                    .setNegativeButton(getString(R.string.dismiss), null)
                    .show();

                return true;
            }
        });
    }

    private String TaskerDialogcmd(String cmd, String isu_file) {
            return "su -c 'am broadcast -a " +
            cmd + " -n com.bhb27.isu/.tasker.TaskerReceiver' | " + isu_file + " -c 'am broadcast -a " +
            cmd + " -n com.bhb27.isu/.tasker.TaskerReceiver'";
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
            getActivity().unregisterReceiver(updateControlsReceiver);
        } catch (IllegalArgumentException ignored) {}
        Tools.closeSU();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mSuSwitch) {
            boolean isChecked = (Boolean) objValue;
            Tools.SwitchSu(isChecked, false, getActivity());
        }

        if (preference == mSelSwitch) {
            boolean isChecked = (Boolean) objValue;
            Tools.SwitchSelinux(isChecked, getActivity());
        }

        if (preference == mFakeSelSwitch) {
            boolean isChecked = (Boolean) objValue;
            if (isChecked) {
                Tools.FakeSelinux(getActivity());
                Tools.SwitchSelinux(isChecked, getActivity());
            }
        }

        if (preference == mDebug) {
            boolean isChecked = (Boolean) objValue;
            Tools.AndroidDebugSet(isChecked, getActivity());
        }

        if (preference == mDebugRoot) {
            boolean isChecked = (Boolean) objValue;
            Tools.SetAndroidDebugRoot(isChecked, getActivity());
        }

        updateState();
        return true;
    }

    private void updateState() {
        if (rootAccess) {
            if (!isCMSU) {
                mSuSwitch.setEnabled(false);
                mSuSwitch.setSummary(getString(R.string.su_not_supported));
            } else {
                mControls.removePreference(mControlsView);
                boolean su = Tools.SuBinary();
                mSuSwitch.setChecked(su);
                mSuSwitch.setSummary(su ? getString(R.string.activated) :
                    getString(R.string.deactivated));
            }

            boolean selinux = Tools.isSELinuxActive(getActivity());
            mSelSwitch.setChecked(selinux);
            mSelSwitch.setSummary(selinux ? getString(R.string.enforcing) :
                getString(R.string.permissive));

            boolean anddebug = Tools.AndroidDebugState(getActivity());
            mDebug.setChecked(anddebug);
            mDebug.setSummary(anddebug ? getString(R.string.enable) :
                getString(R.string.disable));

            boolean anddebugRoot = Tools.AndroidDebugRoot();
            mDebugRoot.setChecked(anddebugRoot);
            mDebugRoot.setSummary(anddebugRoot ? getString(R.string.enable) :
                getString(R.string.disable));

            try {
                getActivity().registerReceiver(updateControlsReceiver, new IntentFilter("updateControlsReceiver"));
            } catch (NullPointerException ignored) {}
        } else {
            mControlsView.setSummary(getString(R.string.device_not_root));
            mSuSwitch.setEnabled(false);
            mSelSwitch.setEnabled(false);
            mFakeSelSwitch.setEnabled(false);
            mDebug.setEnabled(false);
            mDebugRoot.setEnabled(false);
            mTasker.setEnabled(false);
        }

    }

    private final BroadcastReceiver updateControlsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState();
        }
    };

}
