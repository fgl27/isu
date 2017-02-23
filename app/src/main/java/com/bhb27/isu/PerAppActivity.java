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
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.bhb27.isu.perapp.PerAppMonitor;
import com.bhb27.isu.perapp.Per_App;
import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.tools.Constants;

import android.app.AlertDialog;
import java.util.List;
import java.util.ArrayList;
import android.content.DialogInterface;

public class PerAppActivity extends Activity {

    private Button perapp_isu, perapp_su;
    private Switch AutorestartSwitch;
    private ImageView ic_launcher;
    private AlertDialog.Builder mPerAppDialog;
    private Context PerAppActivityContext = null;
    private String TAG = Constants.TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perapp);
        PerAppActivityContext = this;

        perapp_isu = (Button) findViewById(R.id.buttonIsu);

        perapp_su = (Button) findViewById(R.id.buttonSu);

        AutorestartSwitch = (Switch) findViewById(R.id.AutorestartSwitch);

        perapp_isu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PerAppDialog("iSu");
            }
        });

        perapp_su.setOnClickListener(new View.OnClickListener() {
            Intent myIntent = new Intent(getApplicationContext(), AboutActivity.class);
            @Override
            public void onClick(View v) {
                PerAppDialog("Su");
            }
        });

        ic_launcher = (ImageView) findViewById(R.id.ic_launcher_perapp);
        ic_launcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.DoAToast(getString(R.string.isu_by), PerAppActivityContext);
            }
        });

        AutorestartSwitch.setChecked(Tools.getBoolean("auto_restart_su", false, PerAppActivityContext));
        AutorestartSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
                Tools.saveBoolean("auto_restart_su", isChecked, PerAppActivityContext);
            }
        });

    }

    private void PerAppDialog(String id) {
        if (!Per_App.isAccessibilityEnabled(PerAppActivityContext, PerAppMonitor.accessibilityId)) {
            startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
        } else {

            mPerAppDialog = new AlertDialog.Builder(PerAppActivityContext);
            if (id.equals("iSu"))
                mPerAppDialog.setTitle(getString(R.string.per_app_title_isu));
            else if (id.equals("Su"))
                mPerAppDialog.setTitle(getString(R.string.per_app_title_su));
            mPerAppDialog.setCancelable(true);

            final List < Per_App.App > apps = Per_App.getInstalledApps(PerAppActivityContext);

            final String[] packagelist = Per_App.getPackageNames(apps);
            final String[] mapplist = Per_App.getAppNames(apps, id, PerAppActivityContext);

            final String profile_id = id;
            final List < Integer > mSelectedApps = new ArrayList < Integer > ();
            final List < Integer > mDeSelectedApps = new ArrayList < Integer > ();

            final boolean[] checkedValues = Per_App.getExistingSelections(packagelist, profile_id, PerAppActivityContext);

            // Specify the list array, the items to be selected by default (null for none),
            // and the listener through which to receive callbacks when items are selected
            mPerAppDialog.setMultiChoiceItems(mapplist, checkedValues,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                        boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            mSelectedApps.add(which);
                        }
                        if (!isChecked) {
                            mDeSelectedApps.add(which);
                        }
                        if (!isChecked && mSelectedApps.contains(which)) {
                            // Else, if the item is already in the array, remove it
                            mSelectedApps.remove(Integer.valueOf(which));
                        }
                    }
                });

            // Set the action buttons
            mPerAppDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK, so save the mSelectedItems results somewhere
                    // or return them to the component that opened the dialog
                    if (mSelectedApps != null) {
                        for (int i = 0; i < mSelectedApps.size(); i++) {
                            int y = mSelectedApps.get(i);
                            String packageName = packagelist[y];

                            Log.d(TAG, "Saving " + packageName + " to " + profile_id);
                            Per_App.save_app(packageName, profile_id, PerAppActivityContext);
                        }
                    }
                    if (mDeSelectedApps != null) {
                        for (int i = 0; i < mDeSelectedApps.size(); i++) {
                            int y = mDeSelectedApps.get(i);
                            Per_App.remove_app(packagelist[y], profile_id, PerAppActivityContext);
                        }
                    }


                }
            });
            mPerAppDialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            mPerAppDialog.create();
            mPerAppDialog.show();
        }
    }

}
