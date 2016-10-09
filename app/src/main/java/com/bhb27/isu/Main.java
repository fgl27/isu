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
import android.view.View;
import android.view.Gravity;
import android.content.Intent;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.Random;

import com.bhb27.isu.Tools;
import com.bhb27.isu.root.RootUtils;
import com.bhb27.isu.AboutActivity;

public class Main extends Activity {

    private TextView switchStatus, switchStatus_summary, su_warning, kernel_check, about;
    private String su_version = "";
    private String kernel_support = "";
    private Switch suSwitch;

    private String[] pokemonstrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // random poke toast
        pokemonstrings = new String[] {
            getString(R.string.pokemongo_1), getString(R.string.pokemongo_2), getString(R.string.pokemongo_3),
                getString(R.string.pokemongo_4), getString(R.string.pokemongo_5), getString(R.string.pokemongo_6),
                getString(R.string.pokemongo_7), getString(R.string.pokemongo_8), getString(R.string.pokemongo_9),
                getString(R.string.pokemongo_10), getString(R.string.pokemongo_11)
        };

        // Check if is CM-SU
        if (Tools.existFile("/system/bin/su", true) && Tools.existFile("/system/xbin/su", true))
            su_version = RootUtils.runCommand("su --version") + "";
        else if (Tools.IexistFile("/system/bin/isu", true) && Tools.IexistFile("/system/xbin/isu", true))
            su_version = RootUtils.runICommand("isu --version") + "";
        else
            su_version = RootUtils.runCommand("su --version") + "";

        if (su_version.contains("null"))
            su_version = getString(R.string.device_not_root);


        suSwitch = (Switch) findViewById(R.id.suSwitch);
        suSwitch.setText(getString(R.string.su_switch));
        switchStatus = (TextView) findViewById(R.id.switchStatus);
        switchStatus_summary = (TextView) findViewById(R.id.switchStatus_summary);
        // about button
        about = (Button) findViewById(R.id.buttonAbout);

        if (su_version.contains("cm-su")) {
            su_warning = (TextView) findViewById(R.id.su_warning);
            kernel_check = (TextView) findViewById(R.id.kernel_check);
            switchStatus.setText(getString(R.string.su_state));

            //set the switch to ON or OFF
            if (Tools.existFile("/system/bin/su", true) && Tools.existFile("/system/xbin/su", true))
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
                        RootUtils.runICommand("umount /system");
                        RootUtils.runICommand("mv /system/bin/temp_su /system/bin/su");
                        RootUtils.runICommand("mv /system/xbin/isu /system/xbin/su");
                        if (Tools.existFile("/system/bin/su", true) && Tools.existFile("/system/xbin/su", true)) {
                            switchStatus_summary.setText(getString(R.string.su_on));
                            if (isAppInstalled("com.nianticlabs.pokemongo")) {
                                DoAToast(getString(R.string.pokemongo_stop));
                            }
                        } else
                            switchStatus_summary.setText(getString(R.string.su_change_fail));
                    } else {
                        // Make a link to isu so all root tool work
                        RootUtils.runCommand("ln -s -f /system/xbin/isu /system/bin/isu");
                        RootUtils.runCommand("mv /system/bin/su /system/bin/temp_su");
                        RootUtils.runCommand("mv /system/xbin/su /system/xbin/isu");
                        if (Tools.IexistFile("/system/bin/isu", true) && Tools.IexistFile("/system/xbin/isu", true)) {
                            switchStatus_summary.setText(getString(R.string.su_off));
                            if (isAppInstalled("com.nianticlabs.pokemongo")) {
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
            if (Tools.existFile("/system/bin/su", true) && Tools.existFile("/system/xbin/su", true)) {
                kernel_support = RootUtils.runCommand("grep -r -i temp_su *.sh ") + RootUtils.runCommand("grep -r -i isu *.sh ") +
                    RootUtils.runCommand("grep -r -i /sbin/temp_su *.sh ") + RootUtils.runCommand("grep -r -i /sbin/isu *.sh ") + "";
                if (kernel_support.contains("/system/bin/temp_su") && kernel_support.contains("/system/xbin/isu")) {
                    kernel_check.setText(getString(R.string.isu_kernel_good));
                    su_warning.setText(getString(R.string.su_warning));
                } else {
                    kernel_check.setTextColor(this.getResources().getColor(R.color.text_red));
                    kernel_check.setText(getString(R.string.isu_kernel_bad));
                }
            } else {
                kernel_support = RootUtils.runICommand("grep -r -i temp_su *.sh ") + RootUtils.runICommand("grep -r -i isu *.sh ") +
                    RootUtils.runICommand("grep -r -i /sbin/temp_su *.sh ") + RootUtils.runICommand("grep -r -i /sbin/isu *.sh ") + "";
                if (kernel_support.contains("/system/bin/temp_su") && kernel_support.contains("/system/xbin/isu")) {
                    kernel_check.setText(getString(R.string.isu_kernel_good));
                    su_warning.setText(getString(R.string.su_warning));
                } else {
                    kernel_check.setTextColor(this.getResources().getColor(R.color.text_red));
                    kernel_check.setText(getString(R.string.isu_kernel_bad));
                }
            }
        } else {
            suSwitch.setEnabled(false);
            suSwitch.setTextColor(this.getResources().getColor(R.color.text_gray));
            suSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            switchStatus.setText(getString(R.string.su_not_cm));
            switchStatus_summary.setText(su_version);
        }

        about.setText(getString(R.string.about));
        about.setOnClickListener(new View.OnClickListener() {
            Intent myIntent = new Intent(getApplicationContext(), AboutActivity.class);
            @Override
            public void onClick(View v) {
                startActivity(myIntent);
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
