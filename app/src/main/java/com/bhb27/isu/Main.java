/*
 * Copyright (C) 2016 Felipe de Leon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bhb27.isu;

import android.widget.Toast;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Paint;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;

import com.bhb27.isu.Tools;
import com.bhb27.isu.root.RootUtils;
import com.bhb27.isu.AboutActivity;

public class Main extends Activity {

    private TextView switchStatus, switchStatus_summary, su_warning, kernel_check, about;
    private String su_version = "";
    private String kernel_support = "";
    private Switch suSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        switchStatus = (TextView) findViewById(R.id.switchStatus);
        switchStatus_summary = (TextView) findViewById(R.id.switchStatus_summary);
        // about button
        about = (Button) findViewById(R.id.buttonAbout);

        if (su_version.contains("cm-su")) {
            suSwitch.setText(R.string.su_switch);
            su_warning = (TextView) findViewById(R.id.su_warning);
            kernel_check = (TextView) findViewById(R.id.kernel_check);
            switchStatus.setText(R.string.su_state);

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
                        if (Tools.existFile("/system/bin/su", true) && Tools.existFile("/system/xbin/su", true))
                            switchStatus_summary.setText(R.string.su_on);
                        else
                            switchStatus_summary.setText(R.string.su_change_fail);
                    } else {
                        // Make a link to isu so all root tool work
                        RootUtils.runCommand("ln -s -f /system/xbin/isu /system/bin/isu");
                        RootUtils.runCommand("mv /system/bin/su /system/bin/temp_su");
                        RootUtils.runCommand("mv /system/xbin/su /system/xbin/isu");
                        if (Tools.IexistFile("/system/bin/isu", true) && Tools.IexistFile("/system/xbin/isu", true))
                            switchStatus_summary.setText(R.string.su_off);
                        else
                            switchStatus_summary.setText(R.string.su_change_fail);
                    }

                }
            });

            //check the current state before we display the screen
            if (suSwitch.isChecked())
                switchStatus_summary.setText(R.string.su_on);
            else
                switchStatus_summary.setText(R.string.su_off);

            //Kernel support check
            if (Tools.existFile("/system/bin/su", true) && Tools.existFile("/system/xbin/su", true)) {
                kernel_support = RootUtils.runCommand("grep -r -i temp_su *.sh ") + RootUtils.runCommand("grep -r -i isu *.sh ") +
                    RootUtils.runCommand("grep -r -i /sbin/temp_su *.sh ") + RootUtils.runCommand("grep -r -i /sbin/isu *.sh ") + "";
                if (kernel_support.contains("/system/bin/temp_su") && kernel_support.contains("/system/xbin/isu")) {
                    kernel_check.setText(R.string.isu_kernel_good);
                    su_warning.setText(R.string.su_warning);
                } else {
                    kernel_check.setTextColor(this.getResources().getColor(R.color.text_red));
                    kernel_check.setText(R.string.isu_kernel_bad);
                }
            } else {
                kernel_support = RootUtils.runICommand("grep -r -i temp_su *.sh ") + RootUtils.runICommand("grep -r -i isu *.sh ") +
                    RootUtils.runICommand("grep -r -i /sbin/temp_su *.sh ") + RootUtils.runICommand("grep -r -i /sbin/isu *.sh ") + "";
                if (kernel_support.contains("/system/bin/temp_su") && kernel_support.contains("/system/xbin/isu")) {
                    kernel_check.setText(R.string.isu_kernel_good);
                    su_warning.setText(R.string.su_warning);
                } else {
                    kernel_check.setTextColor(this.getResources().getColor(R.color.text_red));
                    kernel_check.setText(R.string.isu_kernel_bad);
                }
            }
        } else {
            suSwitch.setText(R.string.su_switch);
            suSwitch.setEnabled(false);
            suSwitch.setTextColor(this.getResources().getColor(R.color.text_gray));
            suSwitch.setPaintFlags(suSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            switchStatus.setText(R.string.su_not_cm);
            switchStatus_summary.setText(su_version);
        }

        about.setText(R.string.about);
        about.setOnClickListener(new View.OnClickListener() {
            Intent myIntent = new Intent(getApplicationContext(), AboutActivity.class);
            @Override
            public void onClick(View v) {
                startActivity(myIntent);
            }
        });
    }
}
