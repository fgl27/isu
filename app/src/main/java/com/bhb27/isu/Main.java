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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.animation.Animation;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.bhb27.isu.AboutActivity;
import com.bhb27.isu.PerAppActivity;
import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.Tools;

public class Main extends Activity {

    private TextView SuSwitchSummary, SuStatus, kernel_check, Selinux_State, su_version, su_version_summary,
    SelinuxStatus, download_folder_link, per_app_summary, ChangeSuSelinuxSwitch_summary, SuSelinuxSwitch_summary, AndDebugSwitch_summary, ChangeAndDebugSwitch_summary;
    private Button about, per_app, buttonprop, test;
    private Switch suSwitch, SelinuxSwitch, iSuNotification, iSuToastNotification, ChangeSuSelinuxSwitch, SuSelinuxSwitch, AndDebugSwitch, ChangeAndDebugSwitch;

    private ImageView ic_launcher;

    private String TAG = Constants.TAG;

    private final String sepolicy = Constants.sepolicy;

    private boolean upMain = false;

    private String suVersion;
    public String executableFilePath;
    private boolean isCMSU;

    private Context MainContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainContext = this;
        executableFilePath = getFilesDir().getPath() + "/";

        suVersion = Tools.SuVersion(MainContext);
        isCMSU = Tools.SuVersionBool(suVersion);

        suSwitch = (Switch) findViewById(R.id.suSwitch);
        SuSwitchSummary = (TextView) findViewById(R.id.SuSwitchSummary);
        SuStatus = (TextView) findViewById(R.id.SuStatus);
        su_version = (TextView) findViewById(R.id.su_version);
        su_version_summary = (TextView) findViewById(R.id.su_version_summary);
        su_version_summary.setText(suVersion);

        SelinuxSwitch = (Switch) findViewById(R.id.SelinuxSwitch);
        SelinuxStatus = (TextView) findViewById(R.id.SelinuxStatus);
        Selinux_State = (TextView) findViewById(R.id.Selinux_State);

        AndDebugSwitch = (Switch) findViewById(R.id.AndDebugSwitch);
        AndDebugSwitch_summary = (TextView) findViewById(R.id.AndDebugSwitch_summary);
        ChangeAndDebugSwitch = (Switch) findViewById(R.id.ChangeAndDebugSwitch);
        ChangeAndDebugSwitch_summary = (TextView) findViewById(R.id.ChangeAndDebugSwitch_summary);

        iSuNotification = (Switch) findViewById(R.id.iSuNotification);
        iSuToastNotification = (Switch) findViewById(R.id.iSuToastNotification);
        SuSelinuxSwitch = (Switch) findViewById(R.id.SuSelinuxSwitch);
        SuSelinuxSwitch_summary = (TextView) findViewById(R.id.SuSelinuxSwitch_summary);

        ChangeSuSelinuxSwitch = (Switch) findViewById(R.id.ChangeSuSelinuxSwitch);
        ChangeSuSelinuxSwitch_summary = (TextView) findViewById(R.id.ChangeSuSelinuxSwitch_summary);

        per_app = (Button) findViewById(R.id.buttonPer_app);
        per_app_summary = (TextView) findViewById(R.id.per_app);

        buttonprop = (Button) findViewById(R.id.buttonprop);
        test = (Button) findViewById(R.id.test);

        download_folder_link = (TextView) findViewById(R.id.download_folder_link);
        kernel_check = (TextView) findViewById(R.id.kernel_check);
        // about button
        about = (Button) findViewById(R.id.buttonAbout);
        about.setOnClickListener(new View.OnClickListener() {
            Intent myIntent = new Intent(getApplicationContext(), AboutActivity.class);
            @Override
            public void onClick(View v) {
                startActivity(myIntent);
            }
        });

        ic_launcher = (ImageView) findViewById(R.id.ic_launcher);
        ic_launcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.DoAToast(getString(R.string.isu_by), MainContext);
            }
        });

        //reboot support check
        if (RebootSupport()) {
            kernel_check.setText(getString(R.string.isu_reboot));
            download_folder_link.setVisibility(View.GONE);
        } else if (Tools.KernelSupport()) {
            kernel_check.setText(getString(R.string.isu_kernel_good));
            download_folder_link.setVisibility(View.GONE);
        } else {
            kernel_check.setTextColor(getColorWrapper(MainContext, R.color.colorAccent));
            kernel_check.setText(getString(R.string.isu_kernel_bad));
            download_folder_link.setText(getString(R.string.download_folder_link));
            download_folder_link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.androidfilehost.com/?w=files&flid=120360")));
                    } catch (ActivityNotFoundException ex) {
                        Tools.DoAToast(getString(R.string.no_browser), MainContext);
                    }
                }
            });
        }

        ChangeAndDebugSwitch.setChecked(Tools.getBoolean("adb_change", false, MainContext));
        iSuNotification.setChecked(Tools.getBoolean("isu_notification", false, MainContext));
        iSuToastNotification.setChecked(Tools.getBoolean("toast_notifications", true, MainContext));
        SuSelinuxSwitch.setChecked(Tools.getBoolean("restart_selinux", false, MainContext));
        ChangeSuSelinuxSwitch.setChecked(Tools.getBoolean("su_selinux_change", true, MainContext));


        UpdateMain(isCMSU);
        UpdateMainListners(isCMSU);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCMSU = Tools.SuVersionBool(Tools.SuVersion(MainContext));
        if (upMain && isCMSU) UpdateMain(isCMSU);
        else this.onCreate(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Tools.SuVersionBool(Tools.SuVersion(MainContext))) {
            try {
                MainContext.unregisterReceiver(updateMainReceiver);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    protected void UpdateMain(boolean CMSU) {
        if (CMSU) {

            suSwitch.setChecked(Tools.SuBinary());
            SelinuxSwitch.setChecked(Tools.isSELinuxActive());
            AndDebugSwitch.setChecked(Tools.AndroidDebugState(MainContext));

            SuStatus.setText((suSwitch.isChecked() ? getString(R.string.activated) :
                getString(R.string.deactivated)));
            SuSwitchSummary.setText(getString(R.string.su_state));

            SuStatus.setTextColor((Tools.SuBinary()) ? getColorWrapper(MainContext, R.color.colorAccent) :
                getColorWrapper(MainContext, R.color.colorButtonGreen));

            try {
                MainContext.registerReceiver(updateMainReceiver, new IntentFilter("updateMainReceiver"));
            } catch (NullPointerException ignored) {}

            upMain = true;
        } else {
            suSwitch.setEnabled(false);
            suSwitch.setTextColor(getColorWrapper(MainContext, R.color.text_gray));
            suSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            SuSwitchSummary.setText(getString(R.string.su_not_cm));
            su_version.setVisibility(View.GONE);
            SelinuxSwitch.setEnabled(false);
            SelinuxSwitch.setTextColor(getColorWrapper(MainContext, R.color.text_gray));
            SelinuxSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            ChangeSuSelinuxSwitch.setEnabled(false);
            ChangeSuSelinuxSwitch.setTextColor(getColorWrapper(MainContext, R.color.text_gray));
            ChangeSuSelinuxSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            ChangeSuSelinuxSwitch_summary.setVisibility(View.GONE);
            SuSelinuxSwitch.setEnabled(false);
            SuSelinuxSwitch.setTextColor(getColorWrapper(MainContext, R.color.text_gray));
            SuSelinuxSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            AndDebugSwitch.setEnabled(false);
            AndDebugSwitch.setTextColor(getColorWrapper(MainContext, R.color.text_gray));
            AndDebugSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            AndDebugSwitch_summary.setVisibility(View.GONE);
            ChangeAndDebugSwitch.setVisibility(View.GONE);
            ChangeAndDebugSwitch_summary.setVisibility(View.GONE);
            iSuNotification.setEnabled(false);
            iSuNotification.setTextColor(getColorWrapper(MainContext, R.color.text_gray));
            iSuNotification.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            iSuToastNotification.setEnabled(false);
            iSuToastNotification.setTextColor(getColorWrapper(MainContext, R.color.text_gray));
            iSuToastNotification.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            SuSelinuxSwitch_summary.setVisibility(View.GONE);
            per_app.setEnabled(false);
            per_app_summary.setText(getString(R.string.not_available));
            SuStatus.setVisibility(View.GONE);
            kernel_check.setTextColor(getColorWrapper(MainContext, R.color.colorAccent));
            kernel_check.setText(getString(R.string.isu_kernel_no_su));
            upMain = false;
        }
        su_version_summary.setTextColor((!CMSU) ? getColorWrapper(MainContext, R.color.colorAccent) :
            getColorWrapper(MainContext, R.color.colorButtonGreen));
        Selinux_State.setText(Tools.getSELinuxStatus());
        Selinux_State.setTextColor((!Tools.isSELinuxActive()) ? getColorWrapper(MainContext, R.color.colorAccent) :
            getColorWrapper(MainContext, R.color.colorButtonGreen));
    }

    protected void UpdateMainListners(boolean CMSU) {
        if (CMSU) {
            suSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    if (Tools.SuBinary() != isChecked) {
                        Tools.SwitchSu(isChecked, false, MainContext);
                        Tools.UpMain(MainContext);
                    }
                }
            });

            SelinuxSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    if (Tools.isSELinuxActive() != isChecked) {
                        Tools.SwitchSelinux(isChecked, MainContext);
                        Tools.UpMain(MainContext);
                    }
                }
            });

            AndDebugSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    if (Tools.AndroidDebugState(MainContext) != isChecked) {
                        Tools.AndroidDebugSet(isChecked, MainContext);
                        Tools.UpMain(MainContext);
                    }
                }
            });

            ChangeAndDebugSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    Tools.saveBoolean("adb_change", isChecked, MainContext);
                }
            });

            per_app.setOnClickListener(new View.OnClickListener() {
                Intent myIntent = new Intent(getApplicationContext(), PerAppActivity.class);
                @Override
                public void onClick(View v) {
                    startActivity(myIntent);
                }
            });

            buttonprop.setOnClickListener(new View.OnClickListener() {
                Intent myIntent = new Intent(getApplicationContext(), PropActivity.class);
                @Override
                public void onClick(View v) {
                    startActivity(myIntent);
                }
            });

            test.setOnClickListener(new View.OnClickListener() {
                Intent myIntent = new Intent(getApplicationContext(), PropActivity.class);
                @Override
                public void onClick(View v) {
                    new StripeExecute().execute(executableFilePath, "com.bhb27.turbotoast", "com.app.test");
                }
            });

            iSuNotification.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    Tools.saveBoolean("isu_notification", isChecked, MainContext);
                }
            });

            iSuToastNotification.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    Tools.saveBoolean("toast_notifications", isChecked, MainContext);
                }
            });

            SuSelinuxSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    Tools.saveBoolean("restart_selinux", isChecked, MainContext);
                }
            });

            ChangeSuSelinuxSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    Tools.saveBoolean("su_selinux_change", isChecked, MainContext);
                }
            });
        }

        Runnable runThread = new Runnable() {
            public void run() {
                Sepolicy();
                extractresetprop();
                Tools.WriteSettings(MainContext);
                // Only run boot service if app was used and is CM SU
                if (isCMSU && !Tools.getBoolean("run_boot", false, MainContext))
                    Tools.saveBoolean("run_boot", true, MainContext);

                // Create a blank profiles.json to prevent logspam.
                File file = new File(getFilesDir() + "/per_app.json");
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(runThread).start();
    }

    private class StripeExecute extends AsyncTask < String, Void, Void > {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainContext);
            progressDialog.setTitle(getString(R.string.app_name));
            progressDialog.setMessage(getString(R.string.running));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String...params) {
            Tools.stripapp(params[0], params[1], params[2]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }
    }

    private final BroadcastReceiver updateMainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            UpdateMain(true);
        }
    };

    public void extractresetprop() {
        if (!Tools.NewexistFile(executableFilePath + "resetprop", true) ||
            !Tools.NewexistFile(executableFilePath + "resetproparm64", true) ||
            !Tools.NewexistFile(executableFilePath + "resetpropx86", true) ||
            !Tools.NewexistFile(executableFilePath + "busybox", true)) {
            extractAssets(executableFilePath, "resetprop");
            extractAssets(executableFilePath, "resetproparm64");
            extractAssets(executableFilePath, "resetpropx86");
            extractAssets(executableFilePath, "busybox");
        }
    }

    public void Sepolicy() {
        if (!Tools.NewexistFile(executableFilePath + "libsupol.so", true) ||
            !Tools.NewexistFile(executableFilePath + "supolicy", true)) {
            extractAssets(executableFilePath, "libsupol.so");
            extractAssets(executableFilePath, "supolicy");
        }
        Tools.PatchSepolicy(executableFilePath);
    }

    public void extractAssets(String executableFilePath, String filename) {
        executableFilePath = executableFilePath + filename;
        AssetManager assetManager = getAssets();
        InputStream inStream = null;
        OutputStream outStream = null;

        try {

            inStream = assetManager.open(filename);
            outStream = new FileOutputStream(executableFilePath); // for override file content
            //outStream = new FileOutputStream(out,true); // for append file content

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();

        } catch (IOException e) {
            Log.e(TAG, "Failed to copy asset file: " + filename, e);
        }
        File execFile = new File(executableFilePath);
        execFile.setExecutable(true);
        Log.d(TAG, "Copy success: " + filename);
    }

    private static int getColorWrapper(Context context, int id) {
        return ContextCompat.getColor(context, id);
    }

    private boolean RebootSupport() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            if (Tools.ReadSystemPatch())
                return true;
            if (!Tools.NewexistFile(executableFilePath + "isush", true) ||
                !Tools.NewexistFile(executableFilePath + "superuser.rc", true)) {
                extractAssets(executableFilePath, "isush");
                extractAssets(executableFilePath, "superuser.rc");
            }
            Tools.SystemPatch(executableFilePath);
            if (Tools.ReadSystemPatch())
                return true;
        }
        return false;
    }

}
