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
import android.os.IBinder;
import android.content.Context;
import android.util.Log;
import android.content.Intent;

import com.bhb27.isu.tools.Tools;

public class MainService extends Service {

    private static final String TAG = "iSu_MainService";
    private String executableFilePath;
    private boolean isCMSU, Patched;
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
        Context context = getApplicationContext();
        Tools.SendBroadcast("saveRunReceiver", context);
        Tools.patches(getFilesDir().getPath() + "/", context);
        Log.d(TAG, " Run ");
        Tools.closeSU();
        stopSelf();
    }

}
