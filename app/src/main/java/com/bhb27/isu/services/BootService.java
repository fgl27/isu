/*
 * Copyright (C) 2016-2017 Felipe de Leon <fglfgl27@gmail.com>
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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.content.Intent;

import com.bhb27.isu.R;
import com.bhb27.isu.tools.Tools;

public class BootService extends Service {

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private final int NOTIFY_ID = 101;
    private static final String id = "iSu_BootService";

    private boolean isCMSU;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String title = getString(R.string.props_apply_boot);
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel mChannel = mNotifyManager.getNotificationChannel(id);
            mChannel = new NotificationChannel(id, title, NotificationManager.IMPORTANCE_NONE);
            mNotifyManager.createNotificationChannel(mChannel);
            mBuilder = new NotificationCompat.Builder(this, id)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notification)
                .setChannelId(id);

            startForeground(NOTIFY_ID, mBuilder.build());
        }

        init();
    }

    private void init() {
        Context context = getApplicationContext();
        String executableFilePath = getFilesDir().getPath() + "/";
        if (!Tools.PatchesDone(context)) Tools.patches(executableFilePath, context);

        isCMSU = Tools.SuVersionBool(Tools.SuVersion(context));
        if (Tools.getBoolean("prop_run", false, context) && Tools.getBoolean("apply_props", false, context)) {
            Log.d(id, " Applying props");
            Tools.applyprop(context, executableFilePath);
            Tools.applyDbProp(context, executableFilePath);
            if (isCMSU && Tools.readString("ro.debuggable", null, context).equals("0"))
                Tools.stripadb(executableFilePath, context);
        }
        if (Tools.getBoolean("fake_selinux_switch", false, context)) {
            Log.d(id, " fake_selinux");
            Tools.FakeSelinux(context);
            Tools.SwitchSelinux(true, context);
        }

        if (isCMSU && (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) && !Tools.ReadSystemPatch(context))
            Tools.SystemPatch(executableFilePath, context);

        Tools.closeSU();
        Log.d(id, " Run");
        stopSelf();
    }

}
