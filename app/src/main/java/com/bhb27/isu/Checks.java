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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v14.preference.PreferenceFragment;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
	import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.bhb27.isu.BuildConfig;
import com.bhb27.isu.Main;
import com.bhb27.isu.services.MainService;
import com.bhb27.isu.perapp.PerAppMonitor;
import com.bhb27.isu.perapp.Per_App;
import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.SafetyNetHelper;
import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.BuildConfig;

import org.zeroturnaround.zip.ZipUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Checks extends PreferenceFragment {

    private Preference mSuStatus, mRebootStatus, mSafetyNet, mLog, mSafetyNet_remove, mChecksView, mUpdate, mUpdate_remove;
    private PreferenceCategory mChecks, mSafety, mChecksUpdates;
    private String suVersion, executableFilePath, result;
    private int image;
    private boolean isCMSU, rootAccess, update_removed;
    public SafetyNetHelper.Result SNCheckResult;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(Constants.PREF_NAME);
        addPreferencesFromResource(R.xml.checks);
        rootAccess = Tools.rootAccess(getActivity());
        getActivity().startService(new Intent(getActivity(), MainService.class));
        executableFilePath = getActivity().getFilesDir().getPath() + "/";

        suVersion = Tools.SuVersion(getActivity());
        isCMSU = Tools.SuVersionBool(suVersion);

        mChecks = (PreferenceCategory) findPreference("checks_su");
        mSafety = (PreferenceCategory) findPreference("safety");
        mChecksUpdates = (PreferenceCategory) findPreference("checks_update");
        mChecksView = (Preference) findPreference("checks_view");

        mSuStatus = (Preference) findPreference("su_status");
        mSuStatus.setSummary(suVersion);
        mSuStatus.setIcon(Tools.SuVersionBool(suVersion) ? R.drawable.ok : R.drawable.warning);

        mRebootStatus = (Preference) findPreference("reboot_status");

        mUpdate = (Preference) findPreference("update");
        updateStateNoInternet();

        mUpdate_remove = (Preference) findPreference("update_remove");
        mUpdate_remove.setLayoutResource(R.layout.preference_progressbar_two);
        mChecksUpdates.removePreference(mUpdate_remove);

        if (isCMSU) {
            mChecks.removePreference(mChecksView);
            if (Tools.RebootSupport(executableFilePath, getActivity()) || Tools.KernelSupport(getActivity())) {
                mRebootStatus.setSummary(getString(R.string.ok));
                mRebootStatus.setIcon(R.drawable.ok);
            } else {
                mRebootStatus.setSummary(getString(R.string.missing));
                mRebootStatus.setIcon(R.drawable.warning);
                mRebootStatus.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
        } else mChecks.removePreference(mRebootStatus);

        mLog = (Preference) findPreference("check_log");
        mLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (check_writeexternalstorage())
                    new Execute(getActivity()).execute();
                else
                    Tools.DoAToast(getString(R.string.cant_generating), getActivity());
                return true;
            }
        });

        mSafetyNet_remove = (Preference) findPreference("safety_net_remove");
        mSafetyNet_remove.setLayoutResource(R.layout.preference_progressbar);
        mSafety.removePreference(mSafetyNet_remove);
        mSafetyNet = (Preference) findPreference("safety_net");
        mSafetyNet.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mSafety.removePreference(mSafetyNet);
                mSafety.addPreference(mSafetyNet_remove);
                checkSafetyNet();
                return true;
            }
        });

        try {
            getActivity().registerReceiver(updateChecksReceiver, new IntentFilter("updateChecksReceiver"));
        } catch (NullPointerException ignored) {}
        new RequestTask(getActivity()).execute("https://raw.githubusercontent.com/bhb27/scripts/master/etc/isuv.txt");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rootAccess != Tools.rootAccess(getActivity())) {
            rootAccess = Tools.rootAccess(getActivity());
            Tools.updateMain(getActivity(), (String.format(getString(R.string.reloading), getString(R.string.su_access))));
        }
        new RequestTask(getActivity()).execute("https://raw.githubusercontent.com/bhb27/scripts/master/etc/isuv.txt");
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(updateChecksReceiver);
        } catch (IllegalArgumentException ignored) {}
        Tools.closeSU();
    }

    public void checkSafetyNet() {
        new SafetyNetHelper(Main.FragmentActivityWeakReference.get()) {
            @Override
            public void handleResults(Result result) {
                SNCheckResult = result;
                updatesafety();
            }
        }.requestTest();
    }

    public void updatesafety() {
        if (SNCheckResult.failed) {
            image = R.drawable.interrogation;
            result = SNCheckResult.errmsg;
        } else {
            boolean pass = (SNCheckResult.ctsProfile && SNCheckResult.basicIntegrity);
            String pass_result = (pass ? " " + getString(R.string.safetyNet_pass) : " " + getString(R.string.safetyNet_fail));
            String cts_result = (SNCheckResult.ctsProfile ? " " + getString(R.string.safetyNet_pass) : " " + getString(R.string.safetyNet_fail));
            String basicI_result = (SNCheckResult.basicIntegrity ? " " + getString(R.string.safetyNet_pass) : " " + getString(R.string.safetyNet_fail));
            result = getString(R.string.safetyNet_check_success) + pass_result;
            result += "\n" + ("ctsProfile: " + cts_result);
            result += "\n" + ("basicIntegrity: " + basicI_result);

            if (pass)
                image = R.drawable.ok;
            else {
                image = R.drawable.warning;
                if (isCMSU) {
                    result += "\n\n" + getString(R.string.su_state) + ": ";
                    result += (Tools.SuBinary() ? getString(R.string.activated) : getString(R.string.deactivated)) + "\n";
                    result += getString(R.string.selinux_state) + ": ";
                    result += (Tools.isSELinuxActive(getActivity()) ? getString(R.string.enforcing) : getString(R.string.permissive)) + "\n";
                    result += getString(R.string.adb_state) + ": ";
                    result += (Tools.AndroidDebugState(getActivity()) ? getString(R.string.activated) : getString(R.string.deactivated));
                }
            }
        }
        update();
    }

    public void update() {
        mSafety.removePreference(mSafetyNet_remove);
        mSafety.addPreference(mSafetyNet);
        mSafetyNet.setSummary(result);
        mSafetyNet.setIcon(image);
    }

    private static class Execute extends AsyncTask < Void, Void, String > {
        private ProgressDialog progressDialog;
        private WeakReference < Context > contextRef;

        public Execute(Context context) {
            contextRef = new WeakReference < > (context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Context mContext = contextRef.get();
            progressDialog = new ProgressDialog(mContext, R.style.AlertDialogStyle);
            progressDialog.setTitle(mContext.getString(R.string.app_name));
            progressDialog.setMessage(mContext.getString(R.string.generating_log));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void...params) {
            boolean su = Tools.SuBinary();
            Context mContext = contextRef.get();
            String executableFilePath = mContext.getFilesDir().getPath() + "/";
            String sdcard = Environment.getExternalStorageDirectory().getPath();
            String log_folder = sdcard + "/iSu_Logs/";
            String log_temp_folder = sdcard + "/iSu_Logs/tmpziplog/";
            String zip_file = sdcard + "/iSu_Logs/" + "iSu_log" + getDate() + ".zip";
            String logcat = log_temp_folder + "logcat.txt";
            String tmplogcat = log_temp_folder + "tmplogcat.txt";
            String logcatC = "logcat -d ";
            String dmesgC = "dmesg ";
            String getpropC = "getprop ";

            String isuconfig = log_temp_folder + "iSu_config.txt";
            String data_folder = mContext.getFilesDir().getParentFile().getAbsolutePath();
            String perappjson = "per_app.json Accessibility Enabled = " +
                Per_App.isAccessibilityEnabled(mContext, PerAppMonitor.accessibilityId) + "\n";
            String propjson = "\n\nprop.json\n";
            String prefs = "\n\nprefs\n";
            String paths = "\n\npaths\n";

            if (!Tools.NewexistFile(log_folder, true, mContext)) {
                File dir = new File(log_folder);
                dir.mkdir();
            }
            if (Tools.NewexistFile(log_temp_folder, true, mContext)) {
                Tools.runCommand("rm -rf " + log_temp_folder, su, mContext);
                File dir = new File(log_temp_folder);
                dir.mkdir();
            } else {
                File dir = new File(log_temp_folder);
                dir.mkdir();
            }
            Tools.runCommand(logcatC + " > " + logcat, su, mContext);
            Tools.runCommand(dmesgC + " > " + log_temp_folder + "dmesg.txt", su, mContext);
            Tools.runCommand(getpropC + " > " + log_temp_folder + "getprop.txt", su, mContext);
            Tools.runCommand("echo '" + perappjson + "' >> " + isuconfig, su, mContext);
            Tools.runCommand("cat " + executableFilePath + "per_app.json >> " + isuconfig, su, mContext);
            Tools.runCommand("echo '" + propjson + "' >> " + isuconfig, su, mContext);
            Tools.runCommand("cat " + executableFilePath + "prop.json >> " + isuconfig, su, mContext);
            Tools.runCommand("echo '" + prefs + "' >> " + isuconfig, su, mContext);
            Tools.runCommand("cat " + data_folder + "/shared_prefs/" + Constants.PREF_NAME + ".xml >> " + isuconfig, su, mContext);
            Tools.runCommand("echo '" + paths + "' >> " + isuconfig, su, mContext);
            Tools.runCommand("echo '" + log_folder + "' >> " + isuconfig, su, mContext);
            Tools.runCommand("echo '" + data_folder + "' >> " + isuconfig, su, mContext);
            Tools.runCommand((su ? "which su" : "which " + Tools.readString("cmiyc", null, mContext)) + " >> " + isuconfig, su, mContext);
            Tools.runCommand("echo 'iSu version " + BuildConfig.VERSION_NAME + "' >> " + isuconfig, su, mContext);
            Tools.runCommand("rm -rf " + log_temp_folder + "logcat_wile.txt", su, mContext);
            // ZipUtil doesn’t understand folder name that end with /
            // Logcat some times is too long and the zip logcat.txt may be empty, do some check
            while (true) {
                ZipUtil.pack(new File(sdcard + "/iSu_Logs/tmpziplog"), new File(zip_file));
                ZipUtil.unpackEntry(new File(zip_file), "logcat.txt", new File(tmplogcat));
                if (Tools.compareFiles(logcat, tmplogcat, true, mContext)) {
                    Log.d(Constants.TAG, "ziped logcat.txt is ok");
                    Tools.runCommand("rm -rf " + log_temp_folder, su, mContext);
                    break;
                } else {
                    Log.d(Constants.TAG, "logcat.txt is nok");
                    Tools.runCommand("rm -rf " + zip_file, su, mContext);
                    Tools.runCommand("rm -rf " + tmplogcat, su, mContext);
                }
            }
            return zip_file;
        }

        @Override
        protected void onPostExecute(String zip) {
            super.onPostExecute(zip);
            Context mContext = contextRef.get();
            progressDialog.dismiss();
            Tools.SimpleDialog(String.format(mContext.getString(R.string.generating_log_move), zip), mContext);
        }
    }

    public static String getDate() {
        DateFormat dateformate = new SimpleDateFormat("MMM_dd_yyyy_HH_mm", Locale.US);
        Date date = new Date();
        String Final_Date = "_" + dateformate.format(date);
        return Final_Date;
    }

    @TargetApi(23 | 24 | 25)
    private boolean check_writeexternalstorage() {
        if (Build.VERSION.SDK_INT >= 23) {
            int hasWriteExternalPermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteExternalPermission == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            if (hasWriteExternalPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_ASK_PERMISSIONS);
            }
            hasWriteExternalPermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteExternalPermission == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    private static class RequestTask extends AsyncTask < String, String, String > {
        private WeakReference < Context > contextRef;

        public RequestTask(Context context) {
            contextRef = new WeakReference < > (context);
        }

        @Override
        protected String doInBackground(String...site) {
            String webPage = site[0];
            try {
                OkHttpClient client = new OkHttpClient.Builder().build();
                Request request = new Request.Builder().url(webPage).build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Context mContext = contextRef.get();
            if (result != null && !result.isEmpty()) {
                String[] sitesplit = result.split(",");
                Tools.saveString("last_app_version", sitesplit[0], mContext);
                Tools.saveString("last_app_link", sitesplit[1], mContext);
            } else {
                Tools.saveString("last_app_version", "", mContext);
                Tools.saveString("last_app_link", "", mContext);
            }
            Tools.SendBroadcast("updateChecksReceiver", mContext);
        }

    }


    private final BroadcastReceiver updateChecksReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState();
        }
    };

    private void updateState() {
        if (update_removed) {
            mChecksUpdates.removePreference(mUpdate_remove);
            mChecksUpdates.addPreference(mUpdate);
            update_removed = false;
        }
        String version = Tools.readString("last_app_version", null, getActivity());
        String link = Tools.readString("last_app_link", null, getActivity());
        if (version != null && !version.isEmpty() && link != null && !link.isEmpty()) {
            double versionDownload = Float.valueOf(version);
            double versionApp = Float.valueOf(BuildConfig.VERSION_NAME);
            if (versionDownload > versionApp) {
                mUpdate.setSummary(String.format(getString(R.string.update_summary_out), version) + " " + BuildConfig.VERSION_NAME + getString(R.string.update_link));
                mUpdate.setIcon(R.drawable.warning);
                mUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                        } catch (ActivityNotFoundException ex) {
                            Tools.DoAToast(getString(R.string.no_browser), getActivity());
                        }
                        return true;
                    }
                });
            } else if (versionDownload <= versionApp) {
                mUpdate.setSummary(getString(R.string.update_summary_up));
                mUpdate.setIcon(R.drawable.ok);
                mUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //do nothing just to prevent clicks
                        return true;
                    }
                });
            }
        } else updateStateNoInternet();
    }

    private void updateStateNoInternet() {
        mUpdate.setSummary(getString(R.string.update_summary_fail));
        mUpdate.setIcon(R.drawable.interrogation);
        mUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                update_removed = true;
                mChecksUpdates.removePreference(mUpdate);
                mChecksUpdates.addPreference(mUpdate_remove);
                new RequestTask(getActivity()).execute("https://raw.githubusercontent.com/bhb27/scripts/master/etc/isuv.txt");
                return true;
            }
        });
    }

}
