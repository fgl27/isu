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
package com.bhb27.isu.services;

import android.content.BroadcastReceiver;
import android.content.Context;
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
            context.startService(new Intent(context, BootService.class));
            Log.d(TAG, " Started action " + action + " run_boot " + run_boot);
            if (Tools.getBoolean("apply_su", false, context) && Tools.SuVersionBool(Tools.SuVersion(context)))
                context.startService(new Intent(context, SuService.class));
        } else
            Log.d(TAG, "Not Started action " + action + " rootAccess " + rootAccess + " run_boot " + run_boot);

    }
}
