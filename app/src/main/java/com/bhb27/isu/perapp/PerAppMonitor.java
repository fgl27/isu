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

import com.bhb27.isu.tools.RootUtils;
import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joe on 3/2/16.
 */
public class PerAppMonitor extends AccessibilityService {
    private static final String TAG = PerAppMonitor.class.getSimpleName();
    public static String sPackageName;
    public static String accessibilityId;
    String last_profile = "";
    long time = System.currentTimeMillis();
    private final Tools tools_class = new Tools();
    private String bin_su = Constants.bin_su;
    private String xbin_su = Constants.xbin_su;
    private String bin_isu = Constants.bin_isu;
    private String xbin_isu = Constants.xbin_isu;
    private String bin_temp_su = Constants.bin_temp_su;
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
            Log.d(TAG, "Package Name is " + sPackageName);
            if ((System.currentTimeMillis() - time) < 1000) {
                if (!sPackageName.equals(launcher) || !sPackageName.equals("com.android.systemui")) {
                    process_window_change(sPackageName);
                }
            } else if ((System.currentTimeMillis() - time) >= 1000) {
                process_window_change(sPackageName);
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void process_window_change(String packageName) {
        if (!Per_App.app_profile_exists(packageName, getApplicationContext())) {
            packageName = "Default";
            Log.d(TAG, "Profile does not exist. Using Default");
        }
        if (Per_App.app_profile_exists(packageName, getApplicationContext())) {
            ArrayList < String > info = new ArrayList < String > ();
            // Item 0 is package name Item 1 is the profile ID
            info = Per_App.app_profile_info(packageName, getApplicationContext());

            last_profile = info.get(1);
            time = System.currentTimeMillis();
            //active deactive su
            if (last_profile.equals("iSu") && Tools.SuBinary(xbin_su)) {
                tools_class.DoAToast("iSu " + getString(R.string.per_app_deactive) + "!", this);
                iSuSwitch(false, packageName);
            } else if (last_profile.equals("Su") && Tools.SuBinary(xbin_isu)) {
                tools_class.DoAToast("iSu " + getString(R.string.per_app_active) + "!", this);
                iSuSwitch(true, packageName);
            } else if (last_profile.equals("iSu") && Tools.SuBinary(xbin_isu) && !Tools.isSELinuxActive()) {
                RootUtils.runICommand(Constants.SETENFORCE + " 1");
                if (Tools.isSELinuxActive())
                    tools_class.DoAToast(getString(R.string.selinux_toast_ok), this);
                else
                    tools_class.DoAToast(getString(R.string.selinux_toast_nok), this);
            }

        }
    }

    public void iSuSwitch(boolean isChecked, String packageName) {
        if (isChecked) {
            // Mount rw to change mount ro after
            RootUtils.runICommand("mount -o rw,remount /system");
            RootUtils.runICommand("mv " + xbin_isu + " " + xbin_su);
            RootUtils.runCommand("mount -o ro,remount /system");
            if (Tools.getBoolean("restart_su", false, this)) {
                RootUtils.runCommand("am force-stop " + packageName);
                RootUtils.runCommand("am start " + packageName);
            }
        } else {
            // Make a link to isu so all root tool work
            RootUtils.runCommand("mount -o rw,remount /system");
            RootUtils.runCommand("ln -s -f " + xbin_isu + " " + bin_isu);
            RootUtils.runCommand("ln -s -f " + xbin_isu + " " + bin_isu);
            RootUtils.runCommand("mv " + bin_su + " " + bin_temp_su);
            RootUtils.runCommand("mv " + xbin_su + " " + xbin_isu);
            RootUtils.runICommand("mount -o ro,remount /system");
            if (!Tools.isSELinuxActive()) {
                RootUtils.runICommand(Constants.SETENFORCE + " 1");
                if (Tools.isSELinuxActive())
                    tools_class.DoAToast(getString(R.string.selinux_toast_ok), this);
                else
                    tools_class.DoAToast(getString(R.string.selinux_toast_nok), this);

            }
        }
    }
}
