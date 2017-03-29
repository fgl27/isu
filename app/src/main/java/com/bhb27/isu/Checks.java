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

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;

import android.widget.Toast;

import com.bhb27.isu.bootservice.MainService;
import com.bhb27.isu.preferencefragment.PreferenceFragment;
import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.tools.SafetyNetHelper;

public class Checks extends PreferenceFragment {

    private Preference mSuStatus, mRebootStatus, mSafetyNet;
    private Preference mChecksView;
    private PreferenceCategory mChecks;
    private String suVersion, executableFilePath, result;
    private int image;
    private boolean isCMSU;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Constants.PREF_NAME);
        addPreferencesFromResource(R.xml.checks);
        getActivity().setTheme(R.style.Switch_theme);
        getActivity().startService(new Intent(getActivity(), MainService.class));
        executableFilePath = getActivity().getFilesDir().getPath() + "/";

        suVersion = Tools.SuVersion(getActivity());
        isCMSU = Tools.SuVersionBool(suVersion);

        mChecks = (PreferenceCategory) getPreferenceManager().findPreference("checks_su");
        mChecksView = (Preference) getPreferenceManager().findPreference("checks_view");

        mSuStatus = (Preference) getPreferenceManager().findPreference("su_status");
        mSuStatus.setSummary(suVersion);
        mSuStatus.setIcon(Tools.SuVersionBool(suVersion) ? R.drawable.ok : R.drawable.warning);

        if (isCMSU)
           mChecks.removePreference(mChecksView);

        mRebootStatus = (Preference) getPreferenceManager().findPreference("reboot_status");
        if (Tools.RebootSupport(executableFilePath, getActivity()) || Tools.KernelSupport()) {
            mRebootStatus.setSummary(getString(R.string.ok));
            mRebootStatus.setIcon(R.drawable.ok);
        } else {
            mRebootStatus.setSummary(getString(R.string.missing));
            mRebootStatus.setIcon(R.drawable.warning);
            mRebootStatus.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle)
                        .setTitle(getString(R.string.reboot_support))
                        .setMessage(getString(R.string.reboot_support_missing))
                        .setPositiveButton(getString(R.string.download_folder),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.androidfilehost.com/?w=files&flid=120360")));
                                    } catch (ActivityNotFoundException ex) {
                                        Tools.DoAToast(getString(R.string.no_browser), getActivity());
                                    }
                                }
                            })
                        .setNegativeButton(getString(R.string.dismiss),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    return;
                                }
                            }).show();
                    return true;
                }
            });
        }

        mSafetyNet = (Preference) getPreferenceManager().findPreference("safety_net");
        mSafetyNet.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mSafetyNet.setIcon(R.drawable.test);
                mSafetyNet.setSummary(getString(R.string.safetyNet_testing));
                checkSafetyNet();
                return true;
            }
        });
        updatesafety(-10);
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void checkSafetyNet() {
        new SafetyNetHelper(getActivity()) {
            @Override
            public void handleResults(int i) {
                updatesafety(i);
            }
        }.requestTest();
    }

    public void updatesafety(int i) {
        switch (i) {
            case -10:
                image = R.drawable.interrogation;
                result = getString(R.string.safetyNet_untested);
                break;
            case -3:
                image = R.drawable.interrogation;
                result = getString(R.string.safetyNet_connection_suspended);
                break;
            case -2:
                image = R.drawable.interrogation;
                result = getString(R.string.safetyNet_connection_failed);
                break;
            case -1:
                image = R.drawable.warning;
                result = getString(R.string.safetyNet_error);
                break;
            case 0:
                image = R.drawable.warning;
                result = getString(R.string.safetyNet_fail);
                break;
            case 1:
            default:
                image = R.drawable.ok;
                result = getString(R.string.safetyNet_pass);
                break;
        }
        update();
    }

    public void update() {
        mSafetyNet.setSummary(result);
        mSafetyNet.setIcon(image);
    }

}
