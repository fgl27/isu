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
package com.bhb27.isu.perapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joe on 3/2/16.
 */
public class PerAppMonitor extends AccessibilityService {

    private static final String TAG = "iSu" + PerAppMonitor.class.getSimpleName();
    public static String accessibilityId, sPackageName;
    String last_package = "", last_profile = "", dont_profile = "";
    long time = System.currentTimeMillis();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityServiceInfo serviceInfo = this.getServiceInfo();
        accessibilityId = serviceInfo.getId();

        PackageManager localPackageManager = getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        String launcher = localPackageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.getPackageName() != null) {
            sPackageName = event.getPackageName().toString();
            Log.d(TAG, "Package Name is " + sPackageName + " time " + (System.currentTimeMillis() - time));
            if ((System.currentTimeMillis() - time) < 2000) {
                if (!sPackageName.equals(launcher) && !sPackageName.equals("com.android.systemui")) {
                    process_window_change(sPackageName);
                }
            } else if ((System.currentTimeMillis() - time) >= 2000) {
                process_window_change(sPackageName);
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void process_window_change(String packageName) {
        if (Per_App.app_profile_exists(packageName, getApplicationContext()))
            dont_profile = Per_App.app_profile_info(packageName, getApplicationContext()).get(1);
        else dont_profile = "";
        if (dont_profile.equals("dont")) {
           Log.d(TAG, "Profile = " + dont_profile + " app " + packageName);
        } else {
            if (Tools.getBoolean("auto_restart_su", false, this)) {
                if (!packageName.equals(last_package) && !packageName.equals("com.android.systemui")) {
                    if (!Per_App.app_profile_exists(packageName, getApplicationContext()))
                        last_profile = "Su";
                    else {
                        ArrayList < String > info = new ArrayList < String > ();
                        // Item 0 is package name Item 1 is the profile ID
                        info = Per_App.app_profile_info(packageName, getApplicationContext());
                        last_profile = info.get(1);
                    }
                    last_package = packageName;
                    time = System.currentTimeMillis();
                    change();
                    Log.d(TAG, "auto restart profile " + last_profile + " packageName = " + packageName );
                }
            } else {
                if (!Per_App.app_profile_exists(packageName, getApplicationContext())) {
                    packageName = "Default";
                    Log.d(TAG, "Profile does not exist. Using Default");
                }
                if (Per_App.app_profile_exists(packageName, getApplicationContext())) {
                    ArrayList < String > info = new ArrayList < String > ();
                    // Item 0 is package name Item 1 is the profile ID
                    info = Per_App.app_profile_info(packageName, getApplicationContext());
                    last_package = packageName;
                    last_profile = info.get(1);
                    time = System.currentTimeMillis();
                    change();
                    Log.d(TAG, "normal profile " + last_profile + " packageName = " + packageName );
                }
            }
        }
    }

    public void change() {
        //active deactive su selinux
        if (last_profile.equals("Su") && !Tools.SuBinary()) {
            Tools.SwitchSu(true, true, this);
        } else if (last_profile.equals("iSu") && Tools.SuBinary())
            Tools.SwitchSu(false, true, this);
        else {
            String Toast = "";
            if (Tools.getBoolean("selinux_settings_switch", false, this)) {
                String selinux_su_off = Tools.readString("selinux_su_off", null, this);
                String selinux_su_on = Tools.readString("selinux_su_on", null, this);
                boolean selinux = Tools.isSELinuxActive(this);
                if (last_profile.equals("iSu")) {
                    if (!selinux && selinux_su_off.equals("0")) {
                        Tools.SwitchSelinux(true, this);
                        Toast = Toast + "\n" + getString(R.string.activate_selinux);
                    } else if (selinux && selinux_su_off.equals("1")) {
                        Tools.SwitchSelinux(false, this);
                        Toast = Toast + "\n" + getString(R.string.deactivate_selinux);
                    }
                } else if (last_profile.equals("Su")) {
                    if (!selinux && selinux_su_on.equals("0")) {
                        Tools.SwitchSelinux(true, this);
                        Toast = Toast + "\n" + getString(R.string.activate_selinux);
                    } else if (selinux && selinux_su_on.equals("1")) {
                        Tools.SwitchSelinux(false, this);
                        Toast = Toast + "\n" + getString(R.string.deactivate_selinux);
                    }
                }
            }
            if (Tools.getBoolean("anddebug_settings", false, this)) {
                String anddebug_su_off = Tools.readString("anddebug_su_off", null, this);
                String anddebug_su_on = Tools.readString("anddebug_su_on", null, this);
                boolean anddebug = Tools.AndroidDebugState(this);
                if (last_profile.equals("iSu")) {
                    if (!anddebug && anddebug_su_on.equals("1")) {
                        Tools.AndroidDebugSet(true, this);
                        Toast = Toast + "\n" + getString(R.string.activate_anddebug);
                    } else if (anddebug && anddebug_su_on.equals("0")) {
                        Tools.AndroidDebugSet(false, this);
                        Toast = Toast + "\n" + getString(R.string.deactivate_anddebug);
                    }
                } else if (last_profile.equals("Su")) {
                    if (!anddebug && anddebug_su_off.equals("1")) {
                        Tools.AndroidDebugSet(true, this);
                        Toast = Toast + "\n" + getString(R.string.activate_anddebug);
                    } else if (anddebug && anddebug_su_off.equals("0")) {
                        Tools.AndroidDebugSet(false, this);
                        Toast = Toast + "\n" + getString(R.string.deactivate_anddebug);
                    }
                }
            }
            if (Tools.getBoolean("toast_notifications", true, this) && !Toast.isEmpty())
                Tools.DoAToast("iSu " + Toast + "!", this);
        }
    }
}
