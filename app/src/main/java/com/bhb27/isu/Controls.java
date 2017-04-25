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

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;

import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.Tools;

public class Controls extends PreferenceFragment implements
Preference.OnPreferenceChangeListener {

    private SwitchPreference mSuSwitch, mSelSwitch, mDebug;
    private Preference mControlsView, mTasker;
    private PreferenceCategory mControls;
    private String suVersion;
    private boolean isCMSU;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(Constants.PREF_NAME);
        addPreferencesFromResource(R.xml.controls);

        suVersion = Tools.SuVersion(getActivity());
        isCMSU = Tools.SuVersionBool(suVersion);

        mControls = (PreferenceCategory) findPreference("controls_su");

        mSuSwitch = (SwitchPreference) findPreference("su_switch");
        mSuSwitch.setOnPreferenceChangeListener(this);

        mSelSwitch = (SwitchPreference) findPreference("selinux_switch");
        mSelSwitch.setOnPreferenceChangeListener(this);

        mDebug = (SwitchPreference) findPreference("android_debug");
        mDebug.setOnPreferenceChangeListener(this);

        mControlsView = (Preference) findPreference("controls_view");

        mTasker = (Preference) findPreference("tasker");
        mTasker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String options = Constants.TASKER_SU_ON + "\n" + Constants.TASKER_SU_OFF + "\n" + Constants.TASKER_SU_INV + "\n\n" +
                    Constants.TASKER_SELINUX_ON + "\n" + Constants.TASKER_SELINUX_OFF + "\n" + Constants.TASKER_SELINUX_INV + "\n\n" +
                    Constants.TASKER_DEBUG_ON + "\n" + Constants.TASKER_DEBUG_OFF + "\n" + Constants.TASKER_DEBUG_INV + "\n\n" +
                    getString(R.string.tasker_help_extra_description) +
                    "su = " + getString(R.string.su_switch) + "\n" +
                    "selinux = " + getString(R.string.selinux_switch) + "\n" +
                    "debug = " + getString(R.string.anddebug_change) + "\n\n" +
                    "on = " + String.format(getString(R.string.tasker_help_extra_state), getString(R.string.activated)) +
                    "off = " + String.format(getString(R.string.tasker_help_extra_state), getString(R.string.deactivated)) +
                    "inverse = " + getString(R.string.tasker_help_inv);
                Tools.SimpleDialog(String.format(getString(R.string.tasker_help), options), getActivity());
                return true;
            }
        });

        updateState();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(updateControlsReceiver);
        } catch (IllegalArgumentException ignored) {}
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

        if (preference == mDebug) {
            boolean isChecked = (Boolean) objValue;
            Tools.AndroidDebugSet(isChecked, getActivity());
        }

        updateState();
        return true;
    }

    private void updateState() {
        if (mSuSwitch != null) {
            if (!isCMSU) {
                mSuSwitch.setEnabled(false);
                mSuSwitch.setSummary(getString(R.string.su_not_supported));
            } else {
                boolean su = Tools.SuBinary();
                mSuSwitch.setChecked(su);
                mSuSwitch.setSummary(su ? getString(R.string.activated) :
                    getString(R.string.deactivated));
            }
        }

        if (mSelSwitch != null) {
            boolean selinux = Tools.isSELinuxActive(getActivity());
            mSelSwitch.setChecked(selinux);
            mSelSwitch.setSummary(selinux ? getString(R.string.enforcing) :
                getString(R.string.permissive));
        }

        if (mDebug != null) {
            boolean anddebug = Tools.AndroidDebugState(getActivity());
            mDebug.setChecked(anddebug);
            mDebug.setSummary(anddebug ? getString(R.string.enable) :
                getString(R.string.disable));
        }

        try {
            getActivity().registerReceiver(updateControlsReceiver, new IntentFilter("updateControlsReceiver"));
        } catch (NullPointerException ignored) {}

        if (!isCMSU)
            mSuSwitch.setEnabled(false);
        else
            mControls.removePreference(mControlsView);
    }

    private final BroadcastReceiver updateControlsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState();
        }
    };

}
