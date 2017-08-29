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

import android.app.Service;
import android.content.Context;
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
        Context context = this;
        executableFilePath = getFilesDir().getPath() + "/";
        isCMSU = Tools.SuVersionBool(Tools.SuVersion(context));
        Sepolicy(context);
        extractBusybox(context);
        Tools.WriteSettings(context);
        Tools.BPBackup(context);
        if (!Tools.getBoolean("run_boot", false, context))
            Tools.saveBoolean("run_boot", true, context);
        if (isCMSU) {
            if (Tools.NewexistFile(Constants.bin_su, true, context))
                Tools.delbinsu(context);

            Tools.subackup(executableFilePath, context);
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
        Tools.closeSU();
        stopSelf();
    }


    public void extractBusybox(Context context) {
        if (!Tools.NewexistFile(executableFilePath + "busybox", true, context)) {
            Tools.extractAssets(executableFilePath, "busybox" + Tools.abiX(), context);
            Tools.runCommand("mv -f " + executableFilePath + "busybox" + Tools.abiX() + " " + executableFilePath + "busybox", Tools.SuBinary(), context);
        }
    }

    public void Sepolicy(Context context) {
        Tools.PatchSepolicy(executableFilePath, context);
    }

}
