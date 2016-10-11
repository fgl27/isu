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
import android.app.Activity;
import android.graphics.Paint;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import android.view.Gravity;
import android.content.Intent;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.Random;

import com.bhb27.isu.Constants;
import com.bhb27.isu.Tools;
import com.bhb27.isu.root.RootUtils;
import com.bhb27.isu.AboutActivity;

public class Main extends Activity {

    private TextView switchStatus, switchStatus_summary, kernel_check, about, BootSwitch_summary, su_version_text;
    private String su_version = "";
    private String kernel_support = "";
    private Switch suSwitch, BootSwitch;

    public String bin_su = Constants.bin_su;
    public String xbin_su = Constants.xbin_su;
    public String bin_isu = Constants.bin_isu;
    public String xbin_isu = Constants.xbin_isu;
    public String bin_temp_su = Constants.bin_temp_su;

    ImageView ic_launcher;

    private String[] pokemonstrings;
    private String pokemon_app = "com.nianticlabs.pokemongo";

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
        if (Tools.existFile(xbin_su, true))
            su_version = RootUtils.runCommand("su --version") + "";
        else if (Tools.IexistFile(xbin_isu, true))
            su_version = RootUtils.runICommand("isu --version") + "";
        else
            su_version = RootUtils.runCommand("su --version") + "";

        if (su_version.contains("null"))
            su_version = getString(R.string.device_not_root);

        suSwitch = (Switch) findViewById(R.id.suSwitch);
        suSwitch.setText(getString(R.string.su_switch));

        su_version_text = (TextView) findViewById(R.id.su_version);
        su_version_text.setText(su_version);

        BootSwitch = (Switch) findViewById(R.id.BootSwitch);
        BootSwitch.setText(getString(R.string.boot_switch));

        BootSwitch_summary = (TextView) findViewById(R.id.BootSwitch_summary);
        BootSwitch_summary.setText(getString(R.string.boot_switch_summary));

        switchStatus = (TextView) findViewById(R.id.switchStatus);
        switchStatus_summary = (TextView) findViewById(R.id.switchStatus_summary);
        // about button
        about = (Button) findViewById(R.id.buttonAbout);

        if (su_version.contains("cm-su") || su_version.contains("SUPERSU")) {
            kernel_check = (TextView) findViewById(R.id.kernel_check);
            switchStatus.setText(getString(R.string.su_state));

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
                        // Make shore system is unmount if it is not no safety net verification pass
                        RootUtils.runICommand(" mount -o rw,remount /system");
                        RootUtils.runICommand("mv " + bin_temp_su + " " + bin_su);
                        RootUtils.runICommand("mv " + xbin_isu + " " + xbin_su);
                        RootUtils.runCommand("umount /system");
                        if (Tools.existFile(xbin_su, true)) {
                            switchStatus_summary.setText(getString(R.string.su_on));
                            if (isAppInstalled(pokemon_app)) {
                                DoAToast(getString(R.string.pokemongo_stop));
                            }
                        } else
                            switchStatus_summary.setText(getString(R.string.su_change_fail));
                    } else {
                        // Make a link to isu so all root tool work
                        RootUtils.runCommand(" mount -o rw,remount /system");
                        RootUtils.runCommand("ln -s -f " + xbin_isu + " " + bin_isu);
                        RootUtils.runCommand("mv " + bin_su + " " + bin_temp_su);
                        RootUtils.runCommand("mv " + xbin_su + " " + xbin_isu);
                        RootUtils.runICommand("umount /system");
                        if (Tools.IexistFile(xbin_isu, true)) {
                            switchStatus_summary.setText(getString(R.string.su_off));
                            if (isAppInstalled(pokemon_app)) {
                                DoAToast(getString(R.string.pokemongo_start));
                                //repeat two times long toast is too short
                                int Randon_number = RandomInt(pokemonstrings);
                                DoAToast(pokemonstrings[Randon_number]);
                                DoAToast(pokemonstrings[Randon_number]);
                            }
                        } else
                            switchStatus_summary.setText(getString(R.string.su_change_fail));
                    }

                }
            });


            //check the current state before we display the screen
            if (suSwitch.isChecked())
                switchStatus_summary.setText(getString(R.string.su_on));
            else
                switchStatus_summary.setText(getString(R.string.su_off));

            //Kernel support check
            if (Tools.existFile(xbin_su, true)) {
                kernel_support = RootUtils.runCommand("grep -r -i isu_daemon *.rc ") + "";
                if (kernel_support.contains("isu_daemon"))
                    kernel_check.setText(getString(R.string.isu_kernel_good));
                else {
                    BootSwitch.setEnabled(false);
                    BootSwitch.setTextColor(this.getResources().getColor(R.color.text_gray));
                    BootSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    BootSwitch_summary.setVisibility(View.GONE);
                    kernel_check.setTextColor(this.getResources().getColor(R.color.text_red));
                    kernel_check.setText(getString(R.string.isu_kernel_bad));
                }
            } else {
                kernel_support = RootUtils.runICommand("grep -r -i isu_daemon *.rc ") + "";
                if (kernel_support.contains("isu_daemon"))
                    kernel_check.setText(getString(R.string.isu_kernel_good));
                 else {
                    BootSwitch.setEnabled(false);
                    BootSwitch.setTextColor(this.getResources().getColor(R.color.text_gray));
                    BootSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    BootSwitch_summary.setVisibility(View.GONE);
                    kernel_check.setTextColor(this.getResources().getColor(R.color.text_red));
                    kernel_check.setText(getString(R.string.isu_kernel_bad));
                }
            }

            // Boot switch
            BootSwitch.setChecked(Tools.getBoolean("enable_su_on_boot", false, Main.this));

            BootSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                    Tools.saveBoolean("enable_su_on_boot", isChecked, Main.this);
                }
            });
        } else {
            suSwitch.setEnabled(false);
            suSwitch.setTextColor(this.getResources().getColor(R.color.text_gray));
            suSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            switchStatus.setText(getString(R.string.su_not_cm));
            switchStatus_summary.setVisibility(View.GONE);
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

}
