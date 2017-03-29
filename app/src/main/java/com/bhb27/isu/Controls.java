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

import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.preference.Preference.OnPreferenceClickListener;

import com.bhb27.isu.preferencefragment.PreferenceFragment;
import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.Tools;

public class Controls extends PreferenceFragment implements
Preference.OnPreferenceChangeListener {

    private SwitchPreference mSuSwitch, mSelSwitch, mDebug;
    private Preference mControlsView;
    private PreferenceCategory mControls;
    private String suVersion;
    private boolean isCMSU;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Constants.PREF_NAME);
        addPreferencesFromResource(R.xml.controls);
        getActivity().setTheme(R.style.Switch_theme);

        suVersion = Tools.SuVersion(getActivity());
        isCMSU = Tools.SuVersionBool(suVersion);

        mControls = (PreferenceCategory) getPreferenceManager().findPreference("controls_su");

        mSuSwitch = (SwitchPreference) getPreferenceManager().findPreference("su_switch");
        mSuSwitch.setOnPreferenceChangeListener(this);

        mSelSwitch = (SwitchPreference) getPreferenceManager().findPreference("selinux_switch");
        mSelSwitch.setOnPreferenceChangeListener(this);

        mDebug = (SwitchPreference) getPreferenceManager().findPreference("android_debug");
        mDebug.setOnPreferenceChangeListener(this);

        mControlsView = (Preference) getPreferenceManager().findPreference("controls_view");

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
            getActivity().unregisterReceiver(updateMainReceiver);
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
            boolean selinux = Tools.isSELinuxActive();
            mSelSwitch.setChecked(selinux);
            mSelSwitch.setSummary(Tools.getSELinuxStatus());
        }

        if (mDebug != null) {
            boolean anddebug = Tools.AndroidDebugState(getActivity());
            mDebug.setChecked(anddebug);
            mDebug.setSummary(anddebug ? getString(R.string.enable) :
                getString(R.string.disable));
        }

        try {
            getActivity().registerReceiver(updateMainReceiver, new IntentFilter("updateMainReceiver"));
        } catch (NullPointerException ignored) {}

        if (!isCMSU)
            mSuSwitch.setEnabled(false);
        else
            mControls.removePreference(mControlsView);
    }

    private final BroadcastReceiver updateMainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState();
        }
    };

}
