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

import android.app.Service;
import android.os.IBinder;
import android.widget.Toast;
import android.util.Log;
import android.content.Intent;

import com.bhb27.isu.Tools;
import com.bhb27.isu.root.RootUtils;
import com.bhb27.isu.Constants;

public class BootService extends Service {

    private static final String TAG = "iSu_Boot";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        RootUtils.runICommand("umount /system");
        RootUtils.runICommand("mv " + Constants.bin_temp_su + " " + Constants.bin_su);
        RootUtils.runICommand("mv " + Constants.xbin_isu + " " + Constants.xbin_su);
        if (RootUtils.rooted() && RootUtils.rootAccess())
            Toast.makeText(this, getString(R.string.isu_boot_service_ok), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getString(R.string.isu_boot_service_nok), Toast.LENGTH_LONG).show();
        Log.d(TAG, " Start");
        stopSelf();
    }

}
