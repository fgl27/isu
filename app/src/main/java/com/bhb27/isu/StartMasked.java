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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Bundle;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.bhb27.isu.Start;
import com.bhb27.isu.tools.Tools;

public class StartMasked extends AppCompatActivity {

    private boolean appId, rootAccess;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context SMcontext = this;
        appId = Tools.appId(SMcontext);
        rootAccess = Tools.rootAccess(SMcontext);
        check(SMcontext);
    }

    private void check(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkWStorage()) {
                request_writeexternalstorage();
                SimpleDialog(getString(R.string.request_access), context);
            } else if (!rootAccess) {
                rootAccess = Tools.rootAccess(context);
                SimpleDialog(getString(R.string.request_access), context);
            } else action(context);
        } else {
            if (!rootAccess) check(context);
            else action(context);
        }
    }

    private void action(Context context) {
        appId = Tools.appId(context);
        if (!appId) {
            context.startActivity(new Intent(context, Start.class));
            finish();
        } else {
           new Tools.HideTask(context).execute();
        }
    }

    @TargetApi(23 | 24 | 25)
    private boolean checkWStorage() {
        int hasWriteExternalPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return hasWriteExternalPermission == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(23 | 24 | 25)
    private void request_writeexternalstorage() {
        requestPermissions(new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            },
            REQUEST_CODE_ASK_PERMISSIONS);
    }

    public void SimpleDialog(String message, Context context) {
        new AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setCancelable(false)
            .setMessage(message)
            .setNegativeButton(context.getString(R.string.dismiss),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        check(context);
                        return;
                    }
                }).show();
    }
}
