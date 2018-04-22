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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v14.preference.PreferenceFragment;
import android.util.Log;

import com.bhb27.isu.BuildConfig;
import com.bhb27.isu.Main;
import com.bhb27.isu.services.MainService;
import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.SafetyNetHelper;
import com.bhb27.isu.tools.Tools;

public class Checks extends PreferenceFragment {

    private Preference mSuStatus, mRebootStatus, mSafetyNet, mLog, mSafetyNet_remove, mChecksView, mUpdate, mUpdate_remove, mHelp, mHide;
    private PreferenceCategory mChecks, mSafety, mChecksUpdates;
    private String suVersion, executableFilePath, result, version, link;
    private int image;
    private boolean isCMSU, rootAccess, temprootAccess, update_removed, appId, isu_hide, needpUp, iSuisUp, FirstStart, isuInstalled, run;
    public SafetyNetHelper.Result SNCheckResult;

    private AlertDialog Dial;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(Constants.PREF_NAME);
        addPreferencesFromResource(R.xml.checks);
        rootAccess = Tools.rootAccess(getActivity());
        executableFilePath = getActivity().getFilesDir().getPath() + "/";
        FirstStart = false;

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
                                        Tools.browser("https://www.androidfilehost.com/?w=files&flid=120360", getActivity());
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
        } else {
            if (!rootAccess)
                mChecksView.setSummary(getString(R.string.device_not_root));
            mChecks.removePreference(mRebootStatus);
        }

        mHide = (Preference) findPreference("hide");

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
                Tools.logStatus(getActivity());
                return true;
            }
        });

        mLog = (Preference) findPreference("check_log");
        mLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (check_writeexternalstorage()) new Tools.LogToZip(getActivity()).execute();
                else Tools.DoAToast(getString(R.string.cant_generating), getActivity());
                return true;
            }
        });

        mHelp = (Preference) findPreference("check_help");
        mHelp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Tools.browser("http://forum.xda-developers.com/android/apps-games/isu-simple-app-to-deactivate-activate-t3478348", getActivity());
                return true;
            }
        });

        try {
            getActivity().registerReceiver(updateChecksReceiver, new IntentFilter("updateChecksReceiver"));
        } catch (NullPointerException ignored) {}

        try {
            getActivity().registerReceiver(saveRunReceiver, new IntentFilter("saveRunReceiver"));
        } catch (NullPointerException ignored) {}

        updateStateCheck();

        run = Tools.getBoolean("run_boot", false, getActivity());
        if (rootAccess && (!Tools.PatchesDone(getActivity()) || !run)) getActivity().startService(new Intent(getActivity(), MainService.class));
        updateHidePref(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FirstStart) {
            temprootAccess = Tools.rootAccess(getActivity());
            if (rootAccess != temprootAccess) {
                rootAccess = temprootAccess;
                Tools.updateMain(getActivity(), (String.format(getString(R.string.reloading), getString(R.string.su_access))));
            }
            updateHidePref(getActivity());
            run = Tools.getBoolean("run_boot", false, getActivity());
            if (rootAccess && (!Tools.PatchesDone(getActivity()) || !run)) getActivity().startService(new Intent(getActivity(), MainService.class));
        } else FirstStart = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(updateChecksReceiver);
        } catch (IllegalArgumentException ignored) {}

        try {
            getActivity().unregisterReceiver(saveRunReceiver);
        } catch (IllegalArgumentException ignored) {}

        if (Dial != null) Dial.dismiss();
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
            boolean adbRoot = Tools.AndroidDebugRoot() && Tools.AndroidDebugState(getActivity());
            boolean selinux = false;
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
                result += "\n\n";
                if (isCMSU && rootAccess) {
                    boolean su = Tools.SuBinary();
                    result += getString(R.string.su_state) + (su ? getString(R.string.fail_reason) + ": " : ": ");
                    result += (su ? getString(R.string.activated) : getString(R.string.deactivated)) + "\n";
                }
                if (rootAccess) {
                    selinux = Tools.isSELinuxActive(getActivity());
                    result += getString(R.string.selinux_state) + (!selinux ? getString(R.string.fail_reason) + ": " : ": ");
                    result += (selinux ? getString(R.string.enforcing) : getString(R.string.permissive)) + "\n";

                } else {
                    result += getString(R.string.su_state) + ": ";
                    result += getString(R.string.device_not_root) + "\n";

                    selinux = Tools.isSELinuxActiveNoROOT();
                    result += getString(R.string.selinux_state) + (!selinux ? getString(R.string.fail_reason) + ": " : ": ");
                    result += (selinux ? getString(R.string.enforcing) : getString(R.string.permissive)) + "\n";
                }
                result += getString(R.string.adb_state_root) + (adbRoot ? getString(R.string.fail_reason) + ": " : ": ");
                result += (adbRoot ? getString(R.string.running) : getString(R.string.not_running)) + "\n";

                String redprops = Tools.redProps();
                result += getString(R.string.props) + (!redprops.isEmpty() ? getString(R.string.fail_reason) + ": " : ": ");
                result += (redprops.isEmpty() ? getString(R.string.props_status_good) : " " + redprops);
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

    public void updateHidePref(Context context) {
        if (!rootAccess) {
            mHide.setEnabled(false);
            mHide.setSummary(getString(R.string.device_not_root));
        } else {
            appId = Tools.appId(context);
            if (!appId) {
                isu_hide = ("" + Tools.runCommand("pm list packages | grep " + BuildConfig.APPLICATION_ID + " | cut -d: -f2", Tools.SuBinary(), getActivity())).contains(BuildConfig.APPLICATION_ID);
                if (isu_hide) {
                    mHide.setIcon(R.drawable.warning);
                    mHide.setSummary(getString(R.string.not_hide));
                } else {
                    isuInstalled = Tools.isuInstalled(context);
                    mHide.setTitle(getString(R.string.unhide_title));
                    if (needpUp) {
                        iSuisUp = Tools.NeedUpdate(getActivity());
                        if (iSuisUp) {
                            mHide.setIcon(R.drawable.warning);
                            mHide.setSummary(getString(R.string.need_update));
                        }
                        updateStateMasked(iSuisUp);
                    } else if (!isuInstalled) {
                        mHide.setIcon(R.drawable.warning);
                        mHide.setSummary(getString(R.string.isu_not_instaled));
                    } else {
                        mHide.setIcon(R.drawable.exclamation);
                        mHide.setSummary(getString(R.string.is_hide));
                    }
                }
            }
            mHide.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (!appId && isu_hide) {
                        Tools.runCommand("pm hide " + BuildConfig.APPLICATION_ID, Tools.SuBinary(), context);
                        updateHidePref(getActivity());
                    } else if (!appId) {
                        if (iSuisUp)
                            UpHideDialog(getActivity());
                        else if (isuInstalled)
                            UnHideDialog(getActivity());
                    } else HideDialog(getActivity());
                    return true;
                }
            });
        }
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
        } else return true;
        return false;
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
        version = Tools.readString("last_app_version", null, getActivity());
        link = Tools.readString("last_app_link", null, getActivity());
        if (version != null && !version.isEmpty() && link != null && !link.isEmpty()) {
            double versionDownload = (Math.floor(Float.valueOf(version) * 100) / 100);
            double versionApp = (Math.floor(Float.valueOf(BuildConfig.VERSION_NAME) * 100) / 100);
            if (versionDownload > versionApp) {
                appId = Tools.appId(getActivity());
                if (rootAccess && !appId) {
                    needpUp = true;
                    updateHidePref(getActivity());
                } else updateStateMasked(false);
            } else if (versionDownload <= versionApp) {
                needpUp = false;
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

    private void updateStateMasked(boolean useHide) {
        if (useHide) {
            mUpdate.setSummary(getString(R.string.update_use_hide));
            mUpdate.setIcon(R.drawable.interrogation);
            mUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Tools.DoAToast(getString(R.string.update_use_hide), getActivity());
                    return true;
                }
            });
        } else {
            mUpdate.setSummary(String.format(getString(R.string.update_summary_out), version) + " " + BuildConfig.VERSION_NAME + getString(R.string.update_link));
            mUpdate.setIcon(R.drawable.warning);
            mUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Tools.browser(link, getActivity());
                    return true;
                }
            });
        }
    }

    private void updateStateNoInternet() {
        mUpdate.setSummary(getString(R.string.update_summary_fail));
        mUpdate.setIcon(R.drawable.interrogation);
        mUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                updateStateCheck();
                return true;
            }
        });
    }

    private void updateStateCheck() {
        update_removed = true;
        mChecksUpdates.removePreference(mUpdate);
        mChecksUpdates.addPreference(mUpdate_remove);
        new Tools.CheckUpdate(getActivity()).execute("https://raw.githubusercontent.com/bhb27/scripts/master/etc/isuv.txt");
    }

    private final BroadcastReceiver saveRunReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            saveR();
        }
    };

    public void saveR() {
        if (Tools.rootAccess(getActivity()))
            Tools.saveBoolean("run_boot", true, getActivity());
    }

    public void HideDialog(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setCancelable(false)
            .setTitle(getString(R.string.hide_title))
            .setMessage(getString(R.string.hide_summary) + getString(R.string.hide_isu))
            .setNeutralButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Tools.SwitchSu(true, false, getActivity());
                        Tools.saveInt("hide_app_count", 1, context);
                        context.startActivity(new Intent(context, StartMasked.class));
                    }
                })
            .setPositiveButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                });
        Dial = dialog.create();
        Dial.show();
    }

    public void UnHideDialog(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setTitle(getString(R.string.unhide_title))
            .setMessage(getString(R.string.unhide_summary))
            .setNeutralButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Tools.SwitchSu(true, false, getActivity());
                        Tools.HideiSu(context);
                    }
                })
            .setPositiveButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                });
        Dial = dialog.create();
        Dial.show();
    }

    public void UpHideDialog(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setTitle(getString(R.string.need_update_title))
            .setMessage(getString(R.string.need_update_summary))
            .setNeutralButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Tools.UpHideiSu(context);
                    }
                })
            .setPositiveButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                });
        Dial = dialog.create();
        Dial.show();
    }
}
