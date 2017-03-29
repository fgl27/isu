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
package com.bhb27.isu.tileservice;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

import com.bhb27.isu.Main;
import com.bhb27.isu.R;
import com.bhb27.isu.tools.Tools;

@TargetApi(24)
public class QuickTileiSu extends TileService {

    @Override
    public void onClick() {
        if (isLocked()) {
            unlockAndRun();
        } else {
            launch();
        }
    }

    private void unlockAndRun() {
        unlockAndRun(new Runnable() {
            @Override
            public void run() {
                launch();
            }
        });
    }

    private void launch() {
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

}
