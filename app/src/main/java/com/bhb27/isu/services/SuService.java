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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.content.Intent;

import java.io.File;
import java.io.IOException;

import com.bhb27.isu.R;
import com.bhb27.isu.tools.Tools;

public class SuService extends Service {

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private final int NOTIFY_ID = 101;
    private static final String id = "iSu_SUService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String title = getString(R.string.app_name);
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel mChannel = mNotifyManager.getNotificationChannel(id);
            mChannel = new NotificationChannel(id, title, NotificationManager.IMPORTANCE_LOW);
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
        int sleep = Integer.valueOf(Tools.readString("apply_su_delay", null, this));
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        Tools.SwitchSu(false, false, this);
        Log.d(id, " RUN sleep " + sleep);
        stopSelf();
    }

}
