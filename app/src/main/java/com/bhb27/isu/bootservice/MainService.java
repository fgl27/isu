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
import android.os.Build;
import android.os.IBinder;
import android.content.Context;
import android.util.Log;
import android.content.Intent;

import java.io.File;
import java.io.IOException;

import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.tools.Constants;

public class MainService extends Service {

    private static final String TAG = "iSu_MainService";
    private String executableFilePath;
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
        executableFilePath = getFilesDir().getPath() + "/";
        isCMSU = Tools.SuVersionBool(Tools.SuVersion(this));
        Sepolicy(this);
        extractresetprop(this);
        Tools.WriteSettings(this);
        if (!Tools.getBoolean("run_boot", false, this))
            Tools.saveBoolean("run_boot", true, this);
        if (isCMSU) {
            if (Tools.NewexistFile(Constants.bin_su, true, this))
                Tools.delbinsu(this);

            Tools.subackup(this);
        }

        // Create a blank profiles.json to prevent logspam.
        File file = new File(getFilesDir() + "/per_app.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        file = new File(getFilesDir() + "/prop.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, " Run");
        stopSelf();
    }


    public void extractresetprop(Context context) {
        if (!Tools.NewexistFile(executableFilePath + "resetprop", true, context) ||
            !Tools.NewexistFile(executableFilePath + "resetproparm64", true, context) ||
            !Tools.NewexistFile(executableFilePath + "resetpropx86", true, context) ||
            !Tools.NewexistFile(executableFilePath + "busybox", true, context)) {
            Tools.extractAssets(executableFilePath, "resetprop", context);
            Tools.extractAssets(executableFilePath, "resetproparm64", context);
            Tools.extractAssets(executableFilePath, "resetpropx86", context);
            Tools.extractAssets(executableFilePath, "busybox", context);
        }
    }

    public void Sepolicy(Context context) {
        if (!Tools.NewexistFile(executableFilePath + "libsupol.so", true, context) ||
            !Tools.NewexistFile(executableFilePath + "supolicy", true, context)) {
            Tools.extractAssets(executableFilePath, "libsupol.so", context);
            Tools.extractAssets(executableFilePath, "supolicy", context);
        }
        Tools.PatchSepolicy(executableFilePath, context);
    }

}
