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
import android.support.v4.content.ContextCompat;
import android.content.Intent;
import android.util.Log;

import com.bhb27.isu.tools.Tools;

public class SuServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "iSu_SuServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
            Log.d(TAG, " Start");
        // only run if the app has run before and main has extracted asserts
        String action = intent.getAction();

        if (action.equals("RUN")) {
            Tools.SwitchSu(false, false, context);
            Log.d(TAG, " RUN");
        } else Log.d(TAG, "Not Started action " + action);
    }
}
