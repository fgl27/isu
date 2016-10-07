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

    private TextView switchStatus, about;
    private Switch mySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchStatus = (TextView) findViewById(R.id.switchStatus_summary);
        mySwitch = (Switch) findViewById(R.id.mySwitch);
        about = (Button) findViewById(R.id.buttonAbout);

        //set the switch to ON 
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
                    RootUtils.runICommand("mv /system/bin/isu /system/bin/su");
                    RootUtils.runICommand("mv /system/xbin/isu /system/xbin/su");
                    if (Tools.existFile("/system/bin/su", true) && Tools.existFile("/system/xbin/su", true))
                        switchStatus.setText("Su is currently ON");
                    else
                        switchStatus.setText("Su State change Fail!, Su is currently OFF");
                } else {
                    RootUtils.runCommand("mv /system/bin/su /system/bin/isu");
                    RootUtils.runCommand("mv /system/xbin/su /system/xbin/isu");
                    if (Tools.IexistFile("/system/bin/isu", true) && Tools.IexistFile("/system/xbin/isu", true))
                        switchStatus.setText("Su is currently OFF");
                    else
                        switchStatus.setText("Su State change Fail!, Su is currently ON");
                    switchStatus.setText("Su is currently OFF");
                }

            }
        });

        //check the current state before we display the screen
        if (mySwitch.isChecked()) {
            switchStatus.setText("Su is currently ON");
        } else {
            switchStatus.setText("Su is currently OFF");
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