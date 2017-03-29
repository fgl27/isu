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

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.bhb27.isu.preferencefragment.PreferenceFragment;
import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.Tools;

public class Props extends PreferenceFragment implements
Preference.OnPreferenceChangeListener {

    private String executableFilePath, suVersion;
    private ListPreference[] props = new ListPreference[Constants.props.length];
    private Preference mForceAllSafe, mForceAllUnsafe;
    private boolean isCMSU;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Constants.PREF_NAME);
        addPreferencesFromResource(R.xml.props);
        getActivity().setTheme(R.style.Switch_theme);

        suVersion = Tools.SuVersion(getActivity());
        isCMSU = Tools.SuVersionBool(suVersion);

        executableFilePath = getActivity().getFilesDir().getPath() + "/";

        String temp_value;
        for (int i = 0; i < Constants.props.length; i++) {
            temp_value = "";
            props[i] = (ListPreference) getPreferenceManager().findPreference(Constants.props[i]);
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

            mForceAllSafe = (Preference) getPreferenceManager().findPreference("force_all_safe");
            mForceAllUnsafe = (Preference) getPreferenceManager().findPreference("force_all_unsafe");

            mForceAllSafe.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Tools.resetallprop(executableFilePath, true, getActivity());
                    updateState();
                    return true;
                }
            });
            mForceAllUnsafe.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Tools.resetallprop(executableFilePath, false, getActivity());
                    updateState();
                    return true;
                }
            });
        }

        Runnable runThread = new Runnable() {
            public void run() {
                if (Tools.SuVersionBool(Tools.SuVersion(getActivity())))
                    Tools.stripsu(executableFilePath);
                Tools.saveBoolean("prop_run", true, getActivity());
            }
        };
        new Thread(runThread).start();

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
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        for (int i = 0; i < Constants.props.length; i++) {
            if (preference == props[i]) {
                String value = (String) objValue;
                updateprop(Constants.props[i], value);
            }
        }
        return true;
    }

    public void updateprop(String prop, String value) {
        Tools.resetprop(executableFilePath, prop, value, getActivity());
        updateState();
    }

    private void updateState() {
        String[] value = new String[Constants.props.length];
        String summary = "";
        for (int i = 0; i < Constants.props.length; i++) {
            value[i] = Tools.getprop(Constants.props[i]);
            if (value[i] == null || value[i].isEmpty()) {
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
    }
}
