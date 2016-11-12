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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import com.bhb27.isu.AboutActivity;
import com.bhb27.isu.Constants;
import com.bhb27.isu.root.RootUtils;
import com.bhb27.isu.Tools;

public class Main extends Activity {

    private TextView SuSwitchSummary, SuStatus, kernel_check, about, Selinux_State, su_version, su_version_summary,
    SelinuxStatus, folder_link;
    private String su_bin_version = "";
    private String kernel_support_rc = "";
    private String kernel_support_sh = "";
    private Switch suSwitch, SelinuxSwitch;

    public String bin_su = Constants.bin_su;
    public String xbin_su = Constants.xbin_su;
    public String bin_isu = Constants.bin_isu;
    public String xbin_isu = Constants.xbin_isu;
    public String bin_temp_su = Constants.bin_temp_su;

    ImageView ic_launcher;

    private String[] pokemonstrings;
    private String pokemon_app = "com.nianticlabs.pokemongo";

    private String TAG = Constants.TAG;

    private final String sepolicy = "/supolicy --live \"allow untrusted_app superuser_device:sock_file { write }\" \"allow priv_app superuser_device:sock_file { write }\" \"allow priv_app sudaemon:unix_stream_socket { connectto }\" \"allow untrusted_app sudaemon:unix_stream_socket { connectto }\" \"allow untrusted_app anr_data_file:dir { read }\" \"allow priv_app anr_data_file:dir { read }\" \"allow priv_app su_exec:file { execute write getattr setattr execute_no_trans }\" \"allow priv_app system_data_file:file { getattr open read }\" \"allow untrusted_app system_data_file:file { getattr open read }\" \"allow untrusted_app su_exec:file { execute write getattr setattr execute_no_trans }\";";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // random poke toast
        pokemonstrings = new String[] {
            getString(R.string.pokemongo_1), getString(R.string.pokemongo_2), getString(R.string.pokemongo_3),
                getString(R.string.pokemongo_4), getString(R.string.pokemongo_5), getString(R.string.pokemongo_6),
                getString(R.string.pokemongo_7), getString(R.string.pokemongo_8), getString(R.string.pokemongo_9),
                getString(R.string.pokemongo_10), getString(R.string.isu_by)
        };

        // Check if is CM-SU
        if (Tools.existFile(xbin_su, true)) {
            su_bin_version = RootUtils.runCommand("su --version") + "";
            copyAssets("libsupol.so");
            copyAssets("supolicy");
            RootUtils.runCommand("LD_LIBRARY_PATH=" + getFilesDir().getPath() + "/ " + getFilesDir().getPath() + sepolicy);
        } else if (Tools.IexistFile(xbin_isu, true))
            su_bin_version = RootUtils.runICommand("isu --version") + "";
        else
            su_bin_version = RootUtils.runCommand("su --version") + "";

        if (su_bin_version.contains("null"))
            su_bin_version = getString(R.string.device_not_root);

        suSwitch = (Switch) findViewById(R.id.suSwitch);
        suSwitch.setText(getString(R.string.su_switch));

        su_version = (TextView) findViewById(R.id.su_version);
        su_version.setText(getString(R.string.su_version));

        su_version_summary = (TextView) findViewById(R.id.su_version_summary);
        su_version_summary.setText(su_bin_version);

        SelinuxSwitch = (Switch) findViewById(R.id.SelinuxSwitch);
        SelinuxSwitch.setText(getString(R.string.selinux_switch));

        SelinuxStatus = (TextView) findViewById(R.id.SelinuxStatus);
        SelinuxStatus.setText(getString(R.string.selinux_state));

        Selinux_State = (TextView) findViewById(R.id.Selinux_State);
        Selinux_State.setText(Tools.getSELinuxStatus());

        SuSwitchSummary = (TextView) findViewById(R.id.SuSwitchSummary);
        SuStatus = (TextView) findViewById(R.id.SuStatus);

        folder_link = (TextView) findViewById(R.id.folder_link);
        kernel_check = (TextView) findViewById(R.id.kernel_check);
        // about button
        about = (Button) findViewById(R.id.buttonAbout);

        if (su_bin_version.contains("cm-su") || su_bin_version.contains("mk-su")) {

            SuSwitchSummary.setText(getString(R.string.su_state));

            //set the switch to ON or OFF
            if (Tools.existFile(xbin_su, true))
                suSwitch.setChecked(true);
            else
                suSwitch.setChecked(false);
            //attach a listener to check for changes in state
            suSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {

                    if (isChecked) {
                        // Mount rw to change mount ro after
                        RootUtils.runICommand("mount -o rw,remount /system");
                        RootUtils.runICommand("mv " + bin_temp_su + " " + bin_su);
                        RootUtils.runICommand("mv " + xbin_isu + " " + xbin_su);
                        RootUtils.runCommand("mount -o ro,remount /system");
                        if (Tools.existFile(xbin_su, true)) {
                            SuStatus.setText(getString(R.string.su_on));
                            if (isAppInstalled(pokemon_app)) {
                                DoAToast(getString(R.string.pokemongo_stop));
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
                        if (Tools.IexistFile(xbin_isu, true)) {
                            SuStatus.setText(getString(R.string.su_off));
                            if (isAppInstalled(pokemon_app)) {
                                DoAToast(getString(R.string.pokemongo_start));
                                //repeat two times long toast is too short
                                int Randon_number = RandomInt(pokemonstrings);
                                DoAToast(pokemonstrings[Randon_number]);
                                DoAToast(pokemonstrings[Randon_number]);
                            }
                        if (!Tools.isSELinuxActive()) {
                            DoAToast(getString(R.string.selinux_toast_ok));
                            RootUtils.runICommand(Constants.SETENFORCE + " 1");
                            if (Tools.isSELinuxActive())
                                DoAToast(getString(R.string.selinux_toast_ok));
                            else
                                DoAToast(getString(R.string.selinux_toast_nok));
                            Selinux_State.setText(Tools.getSELinuxStatus());
                            SelinuxSwitch.setChecked(Tools.isSELinuxActive());
                        }
                        } else
                            SuStatus.setText(getString(R.string.su_change_fail));
                    }

                }
            });


            //check the current state before we display the screen
            if (suSwitch.isChecked())
                SuStatus.setText(getString(R.string.su_on));
            else
                SuStatus.setText(getString(R.string.su_off));

            //Kernel support check
            if (Tools.existFile(xbin_su, true)) {
                kernel_support_rc = RootUtils.runCommand("grep -r -i isu_daemon *.rc ") + "";
                kernel_support_sh = RootUtils.runCommand("grep -r -i isu_daemon *.sh ") + "" +
                    RootUtils.runCommand("grep -r -i /system/xbin/isu /sbin/*.sh ");
                if (kernel_support_rc.contains("isu_daemon") && kernel_support_sh.contains("/system/xbin/isu")) {
                    kernel_check.setText(getString(R.string.isu_kernel_good));
                    folder_link.setVisibility(View.GONE);
                } else {
                    kernel_check.setTextColor(this.getResources().getColor(R.color.text_red));
                    kernel_check.setText(getString(R.string.isu_kernel_bad));
                    folder_link.setText(getString(R.string.isu_kernel_folder_link));
                }
            } else {
                kernel_support_rc = RootUtils.runICommand("grep -r -i isu_daemon *.rc ") + "";
                kernel_support_sh = RootUtils.runICommand("grep -r -i isu_daemon *.sh ") + "" +
                    RootUtils.runICommand("grep -r -i /system/xbin/isu /sbin/*.sh ");
                if (kernel_support_rc.contains("isu_daemon") && kernel_support_sh.contains("/system/xbin/isu")) {
                    kernel_check.setText(getString(R.string.isu_kernel_good));
                    folder_link.setVisibility(View.GONE);
                } else {
                    kernel_check.setTextColor(this.getResources().getColor(R.color.text_red));
                    kernel_check.setText(getString(R.string.isu_kernel_bad));
                    folder_link.setText(getString(R.string.isu_kernel_folder_link));
                }
            }

            folder_link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.androidfilehost.com/?w=files&flid=120360")));
                    } catch (ActivityNotFoundException ex) {
                        DoAToast(getString(R.string.no_browser));
                    }
                }
            });

            // Selinux switch
            SelinuxSwitch.setChecked(Tools.isSELinuxActive());

            SelinuxSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    if (isChecked) {
                        if (RootUtils.rooted())
                            RootUtils.runCommand(Constants.SETENFORCE + " 1");
                        else
                            RootUtils.runICommand(Constants.SETENFORCE + " 1");
                        Selinux_State.setText(Tools.getSELinuxStatus());
                    } else {
                        if (RootUtils.rooted())
                            RootUtils.runCommand(Constants.SETENFORCE + " 0");
                        else
                            RootUtils.runICommand(Constants.SETENFORCE + " 0");
                        Selinux_State.setText(Tools.getSELinuxStatus());
                    }
                }
            });
        } else {
            suSwitch.setEnabled(false);
            suSwitch.setTextColor(this.getResources().getColor(R.color.text_gray));
            suSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            SuSwitchSummary.setText(getString(R.string.su_not_cm));
            SelinuxSwitch.setEnabled(false);
            SelinuxSwitch.setTextColor(this.getResources().getColor(R.color.text_gray));
            SelinuxSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            Selinux_State.setVisibility(View.GONE);
            SuStatus.setVisibility(View.GONE);
            kernel_check.setTextColor(this.getResources().getColor(R.color.text_red));
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
                    DoAToast(pokemonstrings[Randon_number]);
                    DoAToast(pokemonstrings[Randon_number]);
                } else
                    DoAToast(getString(R.string.isu_by));
            }
        });
    }

    // simple toast function to center the message
    public void DoAToast(String message) {
        Toast toast = Toast.makeText(Main.this, message, Toast.LENGTH_LONG);
        TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
        if (view != null) view.setGravity(Gravity.CENTER);
        toast.show();
    }

    // Poke fun simple function to see if pokemongo is installed and call a fun toast base on a random number
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

    private void copyAssets(String filename) {

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

}
