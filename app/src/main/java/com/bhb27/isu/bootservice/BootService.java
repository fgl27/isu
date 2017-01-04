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
package com.bhb27.isu.bootservice;

import android.app.Service;
import android.os.IBinder;
import android.util.Log;
import android.content.Intent;

import com.bhb27.isu.tools.RootUtils;
import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.tools.Constants;

public class BootService extends Service {

    private static final String TAG = "iSu_BootService";
    private final String sepolicy = Constants.sepolicy;
    private String xbin_su = Constants.xbin_su;
    private String xbin_isu = Constants.xbin_isu;

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
        if (Tools.SuBinary(xbin_su))
            RootUtils.runCommand("LD_LIBRARY_PATH=" + executableFilePath + " " + executableFilePath + sepolicy);
        else if (Tools.SuBinary(xbin_isu))
            RootUtils.runICommand("LD_LIBRARY_PATH=" + executableFilePath + " " + executableFilePath + sepolicy);
        Log.d(TAG, " Run");
        stopSelf();
    }

}
