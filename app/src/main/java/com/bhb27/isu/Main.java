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
import java.util.Random;

import com.bhb27.isu.AboutActivity;
import com.bhb27.isu.PerAppActivity;
import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.RootUtils;
import com.bhb27.isu.tools.Tools;

public class Main extends Activity {

    private TextView SuSwitchSummary, SuStatus, kernel_check, about, Selinux_State, su_version, su_version_summary,
    SelinuxStatus, download_folder_link, per_app, per_app_summary;
    private String su_bin_version = "";
    private Switch suSwitch, SelinuxSwitch;

    private String bin_su = Constants.bin_su;
    private String xbin_su = Constants.xbin_su;
    private String bin_isu = Constants.bin_isu;
    private String xbin_isu = Constants.xbin_isu;
    private String bin_temp_su = Constants.bin_temp_su;

    ImageView ic_launcher;

    private String[] pokemonstrings;
    private String pokemon_app = "com.nianticlabs.pokemongo";

    private String TAG = Constants.TAG;

    private final String sepolicy = Constants.sepolicy;

    private final Tools tools_class = new Tools();
    private Context MainContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	String suVersion = SuVersion();
        PatchSepolicy();

        MainContext = this;

        // random poke toast
        pokemonstrings = new String[] {
            getString(R.string.pokemongo_1), getString(R.string.pokemongo_2), getString(R.string.pokemongo_3),
                getString(R.string.pokemongo_4), getString(R.string.pokemongo_5), getString(R.string.pokemongo_6),
                getString(R.string.pokemongo_7), getString(R.string.pokemongo_8), getString(R.string.pokemongo_9),
                getString(R.string.pokemongo_10), getString(R.string.isu_by)
        };

        suSwitch = (Switch) findViewById(R.id.suSwitch);
        suSwitch.setText(getString(R.string.su_switch));

        su_version = (TextView) findViewById(R.id.su_version);
        su_version.setText(getString(R.string.su_version));

        su_version_summary = (TextView) findViewById(R.id.su_version_summary);
        su_version_summary.setText(suVersion);

        SelinuxSwitch = (Switch) findViewById(R.id.SelinuxSwitch);
        SelinuxSwitch.setText(getString(R.string.selinux_switch));

        SelinuxStatus = (TextView) findViewById(R.id.SelinuxStatus);
        SelinuxStatus.setText(getString(R.string.selinux_state));

        Selinux_State = (TextView) findViewById(R.id.Selinux_State);
        Selinux_State.setText(Tools.getSELinuxStatus());

        SuSwitchSummary = (TextView) findViewById(R.id.SuSwitchSummary);
        SuStatus = (TextView) findViewById(R.id.SuStatus);

        per_app = (Button) findViewById(R.id.buttonPer_app);
        per_app_summary = (TextView) findViewById(R.id.per_app);

        download_folder_link = (TextView) findViewById(R.id.download_folder_link);
        kernel_check = (TextView) findViewById(R.id.kernel_check);
        // about button
        about = (Button) findViewById(R.id.buttonAbout);

        if (SuVersionBool(suVersion)) {

            SuSwitchSummary.setText(getString(R.string.su_state));

            //set the switch to ON or OFF
            if (Tools.SuBinary(xbin_su))
                suSwitch.setChecked(true);
            else
                suSwitch.setChecked(false);
            //attach a listener to check for changes in state
            suSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    iSuSwitch(isChecked);
                }
            });

	    per_app_summary.setText(getString(R.string.accessibility_service_desc));
	    per_app.setText(getString(R.string.set_per_app));
            per_app.setOnClickListener(new View.OnClickListener() {
		Intent myIntent = new Intent(getApplicationContext(), PerAppActivity.class);
                @Override
                public void onClick(View v) {
                    startActivity(myIntent);
                }
            });

            //check the current state before we display the screen
            if (suSwitch.isChecked())
                SuStatus.setText(getString(R.string.su_on));
            else
                SuStatus.setText(getString(R.string.su_off));

            //Kernel support check
            if (tools_class.KernelSupport()) {
                kernel_check.setText(getString(R.string.isu_kernel_good));
                download_folder_link.setVisibility(View.GONE);
            } else {
                kernel_check.setTextColor(getColorWrapper(MainContext, R.color.text_red));
                kernel_check.setText(getString(R.string.isu_kernel_bad));
                download_folder_link.setText(getString(R.string.download_folder_link));
            }

            download_folder_link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.androidfilehost.com/?w=files&flid=120360")));
                    } catch (ActivityNotFoundException ex) {
                        tools_class.DoAToast(getString(R.string.no_browser), MainContext);
                    }
                }
            });

            // Selinux switch
            SelinuxSwitch.setChecked(Tools.isSELinuxActive());
            SelinuxSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    SelinuxSwitch(isChecked);
                }
            });
        } else {
            suSwitch.setEnabled(false);
            suSwitch.setTextColor(getColorWrapper(MainContext, R.color.text_gray));
            suSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            SuSwitchSummary.setText(getString(R.string.su_not_cm));
            SelinuxSwitch.setEnabled(false);
            SelinuxSwitch.setTextColor(getColorWrapper(MainContext, R.color.text_gray));
            SelinuxSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            Selinux_State.setVisibility(View.GONE);
            SuStatus.setVisibility(View.GONE);
            kernel_check.setTextColor(getColorWrapper(MainContext, R.color.text_red));
            kernel_check.setText(getString(R.string.isu_kernel_no_su));
        }

        about.setText(getString(R.string.about));
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
                if (isAppInstalled(pokemon_app)) {
                    //repeat two times long toast is too short
                    int Randon_number = RandomInt(pokemonstrings);
                    tools_class.DoAToast(pokemonstrings[Randon_number], MainContext);
                    tools_class.DoAToast(pokemonstrings[Randon_number], MainContext);
                } else
                    tools_class.DoAToast(getString(R.string.isu_by), MainContext);
            }
        });
    }

    // Poke fun simple function to see if Pokemon go is installed and call a fun toast base on a random number
    // http://stackoverflow.com/a/27156435/6645820 + http://stackoverflow.com/a/424548/6645820
    //package is installed function
    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    // Random int base on a String[] length
    public int RandomInt(String[] max_size) {
        Random rand = new Random();
        int generate = 0;
        generate = rand.nextInt(max_size.length);
        return generate;
    }

    public void PatchSepolicy() {
        copyAssets("libsupol.so");
        copyAssets("supolicy");
        if (Tools.SuBinary(xbin_su))
            RootUtils.runCommand("LD_LIBRARY_PATH=" + getFilesDir().getPath() + "/ " + getFilesDir().getPath() + sepolicy);
        else if (Tools.SuBinary(xbin_isu))
            RootUtils.runICommand("LD_LIBRARY_PATH=" + getFilesDir().getPath() + "/ " + getFilesDir().getPath() + sepolicy);
    }

    public void copyAssets(String filename) {

        String executableFilePath = getFilesDir().getPath() + "/" + filename;
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
        if (isChecked) {
            // Mount rw to change mount ro after
            RootUtils.runICommand("mount -o rw,remount /system");
            RootUtils.runICommand("mv " + bin_temp_su + " " + bin_su);
            RootUtils.runICommand("mv " + xbin_isu + " " + xbin_su);
            RootUtils.runCommand("mount -o ro,remount /system");
            if (Tools.SuBinary(xbin_su)) {
                SuStatus.setText(getString(R.string.su_on));
                if (isAppInstalled(pokemon_app)) {
                    tools_class.DoAToast(getString(R.string.pokemongo_stop), MainContext);
                }
            } else
                SuStatus.setText(getString(R.string.su_change_fail));
        } else {
            // Make a link to isu so all root tool work
            RootUtils.runCommand("mount -o rw,remount /system");
            RootUtils.runCommand("ln -s -f " + xbin_isu + " " + bin_isu);
            RootUtils.runCommand("mv " + bin_su + " " + bin_temp_su);
            RootUtils.runCommand("mv " + xbin_su + " " + xbin_isu);
            RootUtils.runICommand("mount -o ro,remount /system");
            if (Tools.SuBinary(xbin_isu)) {
                SuStatus.setText(getString(R.string.su_off));
                if (isAppInstalled(pokemon_app)) {
                    tools_class.DoAToast(getString(R.string.pokemongo_start), MainContext);
                    //repeat two times long toast is too short
                    int Randon_number = RandomInt(pokemonstrings);
                    tools_class.DoAToast(pokemonstrings[Randon_number], MainContext);
                    tools_class.DoAToast(pokemonstrings[Randon_number], MainContext);
                }
                if (!Tools.isSELinuxActive()) {
                    tools_class.DoAToast(getString(R.string.selinux_toast_ok), MainContext);
                    RootUtils.runICommand(Constants.SETENFORCE + " 1");
                    if (Tools.isSELinuxActive())
                        tools_class.DoAToast(getString(R.string.selinux_toast_ok), MainContext);
                    else
                        tools_class.DoAToast(getString(R.string.selinux_toast_nok), MainContext);
                    Selinux_State.setText(Tools.getSELinuxStatus());
                    SelinuxSwitch.setChecked(Tools.isSELinuxActive());
                }
            } else
                SuStatus.setText(getString(R.string.su_change_fail));
        }
    }

    private void SelinuxSwitch(boolean isChecked) {
        if (isChecked) {
            if (Tools.SuBinary(xbin_su))
                RootUtils.runCommand(Constants.SETENFORCE + " 1");
            else if (Tools.SuBinary(xbin_isu))
                RootUtils.runICommand(Constants.SETENFORCE + " 1");
            Selinux_State.setText(Tools.getSELinuxStatus());
        } else {
            if (Tools.SuBinary(xbin_su))
                RootUtils.runCommand(Constants.SETENFORCE + " 0");
            else if (Tools.SuBinary(xbin_isu))
                RootUtils.runICommand(Constants.SETENFORCE + " 0");
            Selinux_State.setText(Tools.getSELinuxStatus());
        }
    }

    private String SuVersion() {
        // Check if is CM-SU
        if (Tools.SuBinary(xbin_su)) {
            su_bin_version = RootUtils.runCommand("su --version") + "";
        } else if (Tools.SuBinary(xbin_isu))
            su_bin_version = RootUtils.runICommand("isu --version") + "";
        else
            su_bin_version = RootUtils.runCommand("su --version") + "";

        if (su_bin_version.contains("null"))
            su_bin_version = getString(R.string.device_not_root);

        return su_bin_version;
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

}
