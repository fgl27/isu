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
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.bhb27.isu.Main;
import com.bhb27.isu.R;
import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.tools.Constants;

@TargetApi(24)
public class QuickTileSU extends TileService {

    private String xbin_su = Constants.xbin_su; 

    @Override
    public void onStartListening() {
        super.onStartListening();
        boolean su = (Tools.SuVersionBool(Tools.SuVersion(this)));
        if (su) {
            getQsTile().setLabel((Tools.SuBinary(xbin_su) ?
                this.getString(R.string.activated) : this.getString(R.string.deactivated)));
        } else
            getQsTile().setLabel(this.getString(R.string.not_available));
        getQsTile().updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        boolean su = (Tools.SuVersionBool(Tools.SuVersion(this)));
        if (su) {
            Tools.SwitchSu(!Tools.SuBinary(xbin_su), false, this);
            getQsTile().setLabel((Tools.SuBinary(xbin_su) ?
                this.getString(R.string.activated) : this.getString(R.string.deactivated)));
            Tools.UpMain(this);
            getQsTile().updateTile();
        }
    }

}
