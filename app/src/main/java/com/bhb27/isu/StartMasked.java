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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.bhb27.isu.Start;
import com.bhb27.isu.tools.Tools;

public class StartMasked extends AppCompatActivity {

    private boolean appId;
    private AlertDialog Dial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context SMcontext = this;
        appId = Tools.appId(SMcontext);

        if (appId)
            new Tools.HideTask(SMcontext).execute();
        else {
            SMcontext.startActivity(new Intent(SMcontext, Main.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            finish();
        }
        try {
            SMcontext.registerReceiver(RunSimpleHideDialog, new IntentFilter("RunSimpleHideDialog"));
        } catch (NullPointerException ignored) {}

        try {
            SMcontext.registerReceiver(RunSimpleDialogFail, new IntentFilter("RunSimpleDialogFail"));
        } catch (NullPointerException ignored) {}

    }

    @Override
    public void onPause() {
        super.onPause();
        if (Dial != null) Dial.dismiss();
        try {
            this.unregisterReceiver(RunSimpleHideDialog);
        } catch (IllegalArgumentException ignored) {}

        try {
            this.unregisterReceiver(RunSimpleDialogFail);
        } catch (IllegalArgumentException ignored) {}

        Tools.closeSU();
        finish();
    }

    public void SimpleHideDialog(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setCancelable(false)
            .setMessage(String.format(getString(R.string.hide_success), Tools.readString("hide_app_name", "", context)))
            .setNegativeButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean su = Tools.SuBinary();
                        Tools.runCommand("am start -n " + Tools.readString("hide_app_name", "", context) + "/" + BuildConfig.APPLICATION_ID + ".StartMasked", su, context);
                        Tools.runCommand("pm hide " + BuildConfig.APPLICATION_ID, su, context);
                        return;
                    }
                });
        Dial = dialog.create();
        Dial.show();
    }

    private final BroadcastReceiver RunSimpleHideDialog = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SimpleHideDialog(context);
        }
    };

    public void SimpleDialogFail(String message, Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setMessage(getString(R.string.hide_fail))
            .setNegativeButton(context.getString(R.string.dismiss),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.startActivity(new Intent(context, Start.class));
                        return;
                    }
                });
        Dial = dialog.create();
        Dial.show();
    }

    private final BroadcastReceiver RunSimpleDialogFail = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SimpleHideDialog(context);
        }
    };
}
