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
package com.bhb27.isu.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bhb27.isu.R;
import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.Tools;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Constants.YES_ACTION.equals(action)) {
            Tools.SwitchSu(true, context);
            Tools.UpMain(context);
            String Toast = context.getString(R.string.per_app_active);
            if (Tools.getBoolean("main_restart_selinux", false, context) && !Tools.isSELinuxActive()) {
                Tools.SwitchSelinux(true, context);
                Toast = Toast + "\n" + context.getString(R.string.activate_selinux);
            } else if (!Tools.getBoolean("main_restart_selinux", false, context) && Tools.isSELinuxActive()) {
                Tools.SwitchSelinux(false, context);
                Toast = Toast + "\n" + context.getString(R.string.deactivate_selinux);
            }
            Tools.DoAToast("iSu " + Toast + "!", context);
        } else if (Constants.DISSMISS_ACTION.equals(action)) {
            Tools.ClearAllNotification(context);
        }
    }
}
