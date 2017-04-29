/*
 * Copyright (C) 2016-2017 Felipe de Leon <fglfgl27@gmail.com>
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
package com.bhb27.isu.bootservice;

import android.app.Service;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.content.Intent;

import com.bhb27.isu.tools.Tools;

public class BootService extends Service {

    private static final String TAG = "iSu_BootService";
    private boolean isCMSU;

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
        String executableFilePath = getFilesDir().getPath() + "/";
        Tools.PatchSepolicy(executableFilePath, this);
        isCMSU = Tools.SuVersionBool(Tools.SuVersion(this));
        if (Tools.getBoolean("prop_run", false, this) && Tools.getBoolean("apply_props", false, this)) {
            if (isCMSU) Tools.stripsu(executableFilePath, this);
            Log.d(TAG, " Apply props");
            Tools.applyprop(this, executableFilePath);
            Tools.applyDbProp(this, executableFilePath);
        }
        Tools.WriteSettings(this);
        if (isCMSU && (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) && !Tools.ReadSystemPatch(this))
            Tools.SystemPatch(executableFilePath, this);
        if (isCMSU) Tools.subackup(executableFilePath, this);
        Log.d(TAG, " Run");
        stopSelf();
    }

}
