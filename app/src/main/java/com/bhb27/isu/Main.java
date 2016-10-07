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

    private TextView switchStatus, switchStatus_summary, about;
    private String su_version = "";
    private Switch mySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if is CM-SU
        if (Tools.existFile("/system/bin/su", true) && Tools.existFile("/system/xbin/su", true))
            su_version = RootUtils.runCommand("su --version") + "";
        else if (Tools.IexistFile("/system/bin/isu", true) && Tools.IexistFile("/system/xbin/isu", true))
            su_version = RootUtils.runICommand("isu --version") + "";
        else
            su_version = RootUtils.runICommand("isu --version") + "";

        //Set layout base on SU version
        if (su_version.contains("cm-su"))
            setContentView(R.layout.activity_main);
        else
            setContentView(R.layout.activity_main_no_support);

        switchStatus = (TextView) findViewById(R.id.switchStatus);
        switchStatus_summary = (TextView) findViewById(R.id.switchStatus_summary);
        // about button
        about = (Button) findViewById(R.id.buttonAbout);

        if (su_version.contains("cm-su")) {
            mySwitch = (Switch) findViewById(R.id.mySwitch);
            mySwitch.setText(R.string.su_switch);
            switchStatus.setText(R.string.su_state);

            //set the switch to ON or OFF
            if (Tools.existFile("/system/bin/su", true) && Tools.existFile("/system/xbin/su", true))
                mySwitch.setChecked(true);
            else
                mySwitch.setChecked(false);
            //attach a listener to check for changes in state
            mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {

                    if (isChecked) {
                        // Make shore system is unmount if it is not no safety net verification pass
                        RootUtils.runICommand("umount /system");
                        RootUtils.runICommand("mv /system/bin/temp_su /system/bin/su");
                        RootUtils.runICommand("mv /system/xbin/isu /system/xbin/su");
                        if (Tools.existFile("/system/bin/su", true) && Tools.existFile("/system/xbin/su", true))
                            switchStatus_summary.setText("Su is currently ON");
                        else
                            switchStatus_summary.setText("Su State change Fail!");
                    } else {
                        // Make a link to isu so all root tool work
                        RootUtils.runCommand("ln -s -f /system/xbin/isu /system/bin/isu");
                        RootUtils.runCommand("mv /system/bin/su /system/bin/temp_su");
                        RootUtils.runCommand("mv /system/xbin/su /system/xbin/isu");
                        if (Tools.IexistFile("/system/bin/isu", true) && Tools.IexistFile("/system/xbin/isu", true))
                            switchStatus_summary.setText("Su is currently OFF");
                        else
                            switchStatus_summary.setText("Su State change Fail!");
                    }

                }
            });

            //check the current state before we display the screen
            if (mySwitch.isChecked()) {
                switchStatus_summary.setText("Su is currently ON");
            } else {
                switchStatus_summary.setText("Su is currently OFF");
            }
        } else {
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
