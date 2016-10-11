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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bhb27.isu.root.RootUtils;

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "iSu_BReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Tools.getBoolean("enable_su_on_boot", false, context) && !RootUtils.rooted())
            context.startService(new Intent(context, BootService.class));
        Log.d(TAG, " Started");
    }
}
