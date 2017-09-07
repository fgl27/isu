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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;

import com.bhb27.isu.Start;
import com.bhb27.isu.tools.Tools;

public class StartMasked extends AppCompatActivity {

    private boolean appId;

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
    }

}
