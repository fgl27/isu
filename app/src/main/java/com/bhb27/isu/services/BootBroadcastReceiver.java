/*
 * Copyright (C) Felipe de Leon <fglfgl27@gmail.com>
 *
 * this file is part of iSu.
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
package com.bhb27.isu.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.content.Intent;
import android.util.Log;

import com.bhb27.isu.tools.Tools;

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "iSu_BReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // only run if the app has run before and main has extracted asserts
        String action = intent.getAction();
        boolean run_boot = Tools.getBoolean("run_boot", false, context);
        boolean rootAccess = Tools.rootAccess(context);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) && rootAccess && run_boot) {
            Log.d(TAG, " Started action " + action + " run_boot " + run_boot);

            if (Tools.getBoolean("prop_run", false, context) && Tools.getBoolean("apply_props", false, context))
                ContextCompat.startForegroundService(context, new Intent(context, PropsService.class));

            ContextCompat.startForegroundService(context, new Intent(context, BootService.class));

            if (Tools.getBoolean("apply_su", false, context) && Tools.SuVersionBool(Tools.SuVersion(context))) {

                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent serviceIntent = new Intent("com.bhb27.isu.services.SuServiceReceiver.RUN");
                serviceIntent.putExtra("RUN", 100);
                serviceIntent.setClass(context, SuServiceReceiver.class);
                serviceIntent.setAction("RUN");

                PendingIntent pi = PendingIntent.getBroadcast(context, 100, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Integer.valueOf(Tools.readString("apply_su_delay", "0", context)), pi);
            }

        } else Log.d(TAG, "Not Started action " + action + " rootAccess " + rootAccess + " run_boot " + run_boot);
    }
}
