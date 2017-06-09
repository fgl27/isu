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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.R;

/**
 * Created by joe on 3/2/16.
 */
public class PerAppMonitor extends AccessibilityService {

    private static final String TAG = "iSu" + PerAppMonitor.class.getSimpleName();
    public static String accessibilityId, sPackageName;
    String last_package = "", last_profile = "", dont_profile = "";
    long time = System.currentTimeMillis(), SwitchSuDelay, delaysResult = 0;
    private int systemdelay = 3500, allowdelay = 0;
    private Context context;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        context = this;
        accessibilityId = this.getServiceInfo().getId();

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.getPackageName() == null)
            return;

        //delay check after su was changed using another tool
        allowdelay = Integer.valueOf(Tools.readString("allow_delay", "0", context));
        SwitchSuDelay = Tools.getLong(Constants.SWICH_DELAY, 0, context);
        delaysResult = (SwitchSuDelay + allowdelay);

        if (delaysResult > System.currentTimeMillis())
            Log.d(TAG, "current delay result " +
                (delaysResult - System.currentTimeMillis()) + " mseconds");
        else {
            sPackageName = event.getPackageName().toString();
            Log.d(TAG, "Package Name is " + sPackageName + " time " + (System.currentTimeMillis() - time));
            if (sPackageName.equals("com.bhb27.isu") || sPackageName.equals("com.android.systemui"))
                return;
            else if ((System.currentTimeMillis() - time) < systemdelay) {
                if (!sPackageName.equals(UserLauncher()))
                    process_window_change(sPackageName);
            } else
                process_window_change(sPackageName);
        }
    }

    @Override
    public void onInterrupt() {

    }

    public String UserLauncher() {
        PackageManager localPackageManager = getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        return localPackageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
    }

    private void process_window_change(String packageName) {
        boolean profile_exists = (Per_App.app_profile_exists(packageName, getApplicationContext()));
        if (profile_exists)
            dont_profile = Per_App.app_profile_info(packageName, getApplicationContext()).get(1);
        else dont_profile = "";
        if (dont_profile.equals("dont")) {
            Log.d(TAG, "Profile = " + dont_profile + " app " + packageName);
        } else {
            if (Tools.getBoolean("auto_restart_su", false, context)) {
                if (!profile_exists)
                    last_profile = "Su";
                else {
                    // Item 0 is package name Item 1 is the profile ID
                    last_profile = Per_App.app_profile_info(packageName, getApplicationContext()).get(1);
                }
                change();
                last_package = packageName;
                time = System.currentTimeMillis();
                Log.d(TAG, "auto restart profile " + last_profile + " packageName = " + packageName);
            } else {
                if (!profile_exists) {
                    packageName = "Default";
                    Log.d(TAG, "Profile does not exist. Using Default");
                }
                if (profile_exists) {
                    // Item 0 is package name Item 1 is the profile ID
                    last_profile = Per_App.app_profile_info(packageName, getApplicationContext()).get(1);
                    time = System.currentTimeMillis();
                    change();
                    last_package = packageName;
                    Log.d(TAG, "normal profile " + last_profile + " packageName = " + packageName);
                }
            }
        }
    }

    public void change() {
        //active deactive su selinux
        if (last_profile.equals("Su") && !Tools.SuBinary()) {
            Tools.SwitchSu(true, true, context);
        } else if (last_profile.equals("iSu") && Tools.SuBinary())
            Tools.SwitchSu(false, true, context);
        else if (last_profile.equals("Su") || last_profile.equals("iSu"))
            Tools.ChangeSUToast(last_profile.equals("Su") ? true : false, context, "");
    }
}
