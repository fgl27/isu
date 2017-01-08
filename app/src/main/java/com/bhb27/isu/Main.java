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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    SelinuxStatus, download_folder_link, per_app_summary;
    private Button about, per_app;
    private Switch suSwitch, SelinuxSwitch;

    private String bin_su = Constants.bin_su;
    private String xbin_su = Constants.xbin_su;
    private String bin_isu = Constants.bin_isu;
    private String xbin_isu = Constants.xbin_isu;
    private String bin_temp_su = Constants.bin_temp_su;

    private ImageView ic_launcher;

    private String TAG = Constants.TAG;

    private final String sepolicy = Constants.sepolicy;

    private boolean upMain = false;

    private String suVersion;
    private boolean isCMSU;

    private Context MainContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainContext = this;

        LinearLayout layout = (LinearLayout) findViewById(R.id.MainLayout);
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setFillAfter(true);
        animation.setDuration(750);
        layout.startAnimation(animation);

        suVersion = Tools.SuVersion(MainContext);
        isCMSU = SuVersionBool(suVersion);

        Runnable runSepolicy = new Runnable() {
            public void run() {
                Sepolicy();
                // Only run boot service if app was used and is CM SU
                if (isCMSU && !Tools.getBoolean("run_boot", false, MainContext))
                    Tools.saveBoolean("run_boot", true, MainContext);
            }
        };
        new Thread(runSepolicy).start();

        suSwitch = (Switch) findViewById(R.id.suSwitch);
        SuSwitchSummary = (TextView) findViewById(R.id.SuSwitchSummary);
        SuStatus = (TextView) findViewById(R.id.SuStatus);
        su_version = (TextView) findViewById(R.id.su_version);
        su_version_summary = (TextView) findViewById(R.id.su_version_summary);
        su_version_summary.setText(suVersion);

        SelinuxSwitch = (Switch) findViewById(R.id.SelinuxSwitch);
        SelinuxStatus = (TextView) findViewById(R.id.SelinuxStatus);
        Selinux_State = (TextView) findViewById(R.id.Selinux_State);
        Selinux_State.setText(Tools.getSELinuxStatus());

        per_app = (Button) findViewById(R.id.buttonPer_app);
        per_app_summary = (TextView) findViewById(R.id.per_app);

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
            kernel_check.setTextColor(getColorWrapper(MainContext, R.color.text_red));
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
        UpdateMain(isCMSU);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCMSU = SuVersionBool(Tools.SuVersion(MainContext));
        if (upMain && isCMSU) UpdateMain(isCMSU);
        else this.onCreate(null);
    }

    protected void UpdateMain(boolean CMSU) {
        if (CMSU) {

            suSwitch.setChecked(Tools.SuBinary(xbin_su));
            SuStatus.setText((suSwitch.isChecked() ? getString(R.string.activated) :
                getString(R.string.deactivated)));
            suSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    iSuSwitch(isChecked);
                }
            });
            SuSwitchSummary.setText(getString(R.string.su_state));

            // Selinux switch
            SelinuxSwitch.setChecked(Tools.isSELinuxActive());
            SelinuxSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    SelinuxSwitch(isChecked);
                }
            });

            per_app.setOnClickListener(new View.OnClickListener() {
                Intent myIntent = new Intent(getApplicationContext(), PerAppActivity.class);
                @Override
                public void onClick(View v) {
                    startActivity(myIntent);
                }
            });

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
            per_app.setTextColor(getColorWrapper(MainContext, R.color.text_gray));
            per_app.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            per_app.setEnabled(false);
            per_app_summary.setText(getString(R.string.not_available));
            SuStatus.setVisibility(View.GONE);
            kernel_check.setTextColor(getColorWrapper(MainContext, R.color.text_red));
            kernel_check.setText(getString(R.string.isu_kernel_no_su));
            upMain = false;
        }
    }

    public void Sepolicy() {
        String executableFilePath = getFilesDir().getPath() + "/";
        if (!Tools.NewexistFile(executableFilePath + "libsupol.so", true) ||
            !Tools.NewexistFile(executableFilePath + "supolicy", true)) {
            extractAssets(executableFilePath + "libsupol.so", "libsupol.so");
            extractAssets(executableFilePath + "supolicy", "supolicy");
        }
        Tools.PatchSepolicy(executableFilePath);
    }

    public void extractAssets(String executableFilePath, String filename) {

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
        Log.e(TAG, "Copy success: " + filename);
    }

    private void iSuSwitch(boolean isChecked) {
        Tools.SwitchSu(isChecked);
        if (isChecked)
            SuStatus.setText((Tools.SuBinary(xbin_su) ? getString(R.string.activated) :
                getString(R.string.su_change_fail)));
        else {
            SuStatus.setText((Tools.SuBinary(xbin_isu) ? getString(R.string.deactivated) :
                getString(R.string.su_change_fail)));
            if (!Tools.isSELinuxActive()) {
                Tools.SwitchSelinux(true);
                Tools.DoAToast("iSu " +
                    (Tools.isSELinuxActive() ? getString(R.string.selinux_toast_ok) :
                        getString(R.string.selinux_toast_nok)), MainContext);
                Selinux_State.setText(Tools.getSELinuxStatus());
                SelinuxSwitch.setChecked(Tools.isSELinuxActive());
            }
        }
    }
    private void SelinuxSwitch(boolean isChecked) {
        Tools.SwitchSelinux(isChecked);
        Selinux_State.setText(Tools.getSELinuxStatus());
    }

    private static int getColorWrapper(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(id);
        } else {
            //noinspection deprecation
            return context.getResources().getColor(id);
        }
    }

    private boolean SuVersionBool(String suVersion) {
        if (suVersion.contains("cm-su") || suVersion.contains("mk-su") ||
            suVersion.contains("16 com.android.settings"))
            return true;
        else return false;
    }

    private boolean RebootSupport() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            String executableFilePath = getFilesDir().getPath() + "/";
            if (Tools.ReadSystemPatch())
                return true;
            if (!Tools.NewexistFile(executableFilePath + "isush", true) ||
                !Tools.NewexistFile(executableFilePath + "superuser.rc", true)) {
                extractAssets(executableFilePath + "isush", "isush");
                extractAssets(executableFilePath + "superuser.rc", "superuser.rc");
            }
            Tools.SystemPatch(executableFilePath);
            if (Tools.ReadSystemPatch())
                return true;
        }
        return false;
    }

}
