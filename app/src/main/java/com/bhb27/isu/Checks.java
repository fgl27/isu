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

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.bhb27.isu.bootservice.MainService;
import com.bhb27.isu.preferencefragment.PreferenceFragment;
import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.RootUtils;
import com.bhb27.isu.tools.SafetyNetHelper;
import com.bhb27.isu.tools.Tools;

import org.zeroturnaround.zip.ZipUtil;

public class Checks extends PreferenceFragment {

    private Preference mSuStatus, mRebootStatus, mSafetyNet, mLog;
    private Preference mChecksView;
    private PreferenceCategory mChecks;
    private String suVersion, executableFilePath, result;
    private int image;
    private boolean isCMSU;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

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

        mLog = (Preference) getPreferenceManager().findPreference("check_log");
        mLog.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (check_writeexternalstorage())
                    new Execute().execute();
                else 
                    Tools.DoAToast(getString(R.string.cant_generating), getActivity());
                return true;
            }
        });

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

    private class Execute extends AsyncTask < Void, Void, String > {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(getString(R.string.app_name));
            progressDialog.setMessage(getString(R.string.generating_log));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void...params) {
            boolean su = Tools.SuBinary();
            String sdcard = Environment.getExternalStorageDirectory().getPath();
            String log_folder = sdcard + "/iSu_Logs/";
            String log_temp_folder = sdcard + "/iSu_Logs/tmpziplog/";
            boolean zip_ok = false;
            String zip_file = sdcard + "/iSu_Logs/" + "iSu_log" + getDate() + ".zip";
            String logcat = log_temp_folder + "logcat.txt";
            String tmplogcat = log_temp_folder + "tmplogcat.txt";
            String logcatC = "logcat -d ";
            String dmesgC = "dmesg ";
            String getpropC = "getprop ";

            if (!Tools.NewexistFile(log_folder, true)) {
                File dir = new File(log_folder);
                dir.mkdir();
            }
            if (Tools.NewexistFile(log_temp_folder, true)) {
                runCommand("rm -rf " + log_temp_folder, su);
                File dir = new File(log_temp_folder);
                dir.mkdir();
            } else {
                File dir = new File(log_temp_folder);
                dir.mkdir();
            }
            runCommand(logcatC + " > " + logcat, su);
            runCommand(dmesgC + " > " + log_temp_folder + "dmesg.txt", su);
            runCommand(getpropC + " > " + log_temp_folder + "getprop.txt", su);
            runCommand("rm -rf " + log_temp_folder + "logcat_wile.txt", su);
            // ZipUtil doesn’t understand folder name that end with /
            // Logcat some times is too long and the zip logcat.txt may be empty, do some check
            while (!zip_ok) {
                ZipUtil.pack(new File(sdcard + "/iSu_Logs/tmpziplog"), new File(zip_file));
                ZipUtil.unpackEntry(new File(zip_file), "logcat.txt", new File(tmplogcat));
                if (Tools.compareFiles(logcat, tmplogcat, true)) {
                    Log.d(Constants.TAG, "ziped logcat.txt is ok");
                    runCommand("rm -rf " + log_temp_folder, su);
                    zip_ok = true;
                } else {
                    Log.d(Constants.TAG, "logcat.txt is nok");
                    runCommand("rm -rf " + zip_file, su);
                    runCommand("rm -rf " + tmplogcat, su);
                }
            }
            return zip_file;
        }

        @Override
        protected void onPostExecute(String zip) {
            super.onPostExecute(zip);
            progressDialog.dismiss();
            LogResultDialog(String.format(getString(R.string.generating_log_move), zip));
        }
    }

    public void runCommand(String command, boolean su) {
        if (su)
            RootUtils.runCommand(command);
        else
            RootUtils.runICommand(command);
    }

    public String getDate() {
        DateFormat dateformate = new SimpleDateFormat("MMM_dd_yyyy_HH_mm", Locale.US);
        Date date = new Date();
        String Final_Date = "_" + dateformate.format(date);
        return Final_Date;
    }

    public void LogResultDialog(String message) {
        new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle)
            .setMessage(message)
            .setNegativeButton(getString(R.string.dismiss),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                }).show();
    }

    @TargetApi(23|24|25)
    private boolean check_writeexternalstorage() {
        if (Build.VERSION.SDK_INT >= 23) {
            int hasWriteExternalPermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteExternalPermission == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            if (hasWriteExternalPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            hasWriteExternalPermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteExternalPermission == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }
}
