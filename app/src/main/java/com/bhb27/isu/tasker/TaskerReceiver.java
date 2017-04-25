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
package com.bhb27.isu.tasker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bhb27.isu.R;
import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.tools.Tools;

public class TaskerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        boolean suversion = Tools.SuVersionBool(Tools.SuVersion(context));
        if (Constants.TASKER_SU_INV.equals(action) && suversion)
            Tools.SwitchSu(!Tools.SuBinary(), false, context);
        else if (Constants.TASKER_SELINUX_INV.equals(action))
            Tools.SwitchSelinux(!Tools.isSELinuxActive(context), context);
        else if (Constants.TASKER_DEBUG_INV.equals(action))
            Tools.AndroidDebugSet(!Tools.AndroidDebugState(context), context);
        else if (Constants.TASKER_SU_ON.equals(action) && suversion) {
            if (!Tools.SuBinary()) Tools.SwitchSu(true, false, context);
            else Tools.ChangeSUToast(true, context, "");
        } else if (Constants.TASKER_SU_OFF.equals(action) && suversion) {
            if (Tools.SuBinary()) Tools.SwitchSu(false, false, context);
            else Tools.ChangeSUToast(false, context, "");
        } else if (Constants.TASKER_SELINUX_ON.equals(action) && !Tools.isSELinuxActive(context))
            Tools.SwitchSelinux(true, context);
        else if (Constants.TASKER_SELINUX_OFF.equals(action) && Tools.isSELinuxActive(context))
            Tools.SwitchSelinux(false, context);
        else if (Constants.TASKER_DEBUG_ON.equals(action) && !Tools.AndroidDebugState(context))
            Tools.AndroidDebugSet(true, context);
        else if (Constants.TASKER_DEBUG_OFF.equals(action) && Tools.AndroidDebugState(context))
            Tools.AndroidDebugSet(false, context);
    }
}
