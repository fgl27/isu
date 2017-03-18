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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.content.ContextCompat;

import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.tools.Constants;

public class PropActivity extends Activity {

    private TextView rodebuggable, rosecure, robuildtags, bl_state, flash_locked, roverifiedbootstate, rodebuggable_summary, rosecure_summary, robuildtags_summary, bl_state_summary, flash_locked_summary, roverifiedbootstate_summary, rodebuggable_help, rosecure_help, robuildtags_help, bl_state_help, flash_locked_help, roverifiedbootstate_help;

    private View bl_state_view, flash_locked_view, roverifiedbootstate_view;

    private Context PropActivityContext = null;

    private String executableFilePath, Sdebuggable = Constants.props[1], Sbuildtags = Constants.props[0], Sbl_state = Constants.props[2], Sflash_locked = Constants.props[3], Sroverifiedbootstate = Constants.props[4], Srosecure = Constants.props[5];

    private ImageView ic_launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prop);
        PropActivityContext = this;
        executableFilePath = getFilesDir().getPath() + "/";

        rodebuggable = (TextView) findViewById(R.id.rodebuggable);
        rosecure = (TextView) findViewById(R.id.rosecure);
        robuildtags = (TextView) findViewById(R.id.robuildtags);
        bl_state = (TextView) findViewById(R.id.bl_state);
        flash_locked = (TextView) findViewById(R.id.flash_locked);
        roverifiedbootstate = (TextView) findViewById(R.id.roverifiedbootstate);

        rodebuggable.setText(String.format(getString(R.string.equal), Sdebuggable));
        rosecure.setText(String.format(getString(R.string.equal), Srosecure));
        robuildtags.setText(String.format(getString(R.string.equal), Sbuildtags));
        bl_state.setText(String.format(getString(R.string.equal), Sbl_state));
        flash_locked.setText(String.format(getString(R.string.equal), Sflash_locked));
        roverifiedbootstate.setText(String.format(getString(R.string.equal), Sroverifiedbootstate));

        rodebuggable_summary = (TextView) findViewById(R.id.rodebuggable_summary);
        rosecure_summary = (TextView) findViewById(R.id.rosecure_summary);
        robuildtags_summary = (TextView) findViewById(R.id.robuildtags_summary);
        bl_state_summary = (TextView) findViewById(R.id.bl_state_summary);
        flash_locked_summary = (TextView) findViewById(R.id.flash_locked_summary);
        roverifiedbootstate_summary = (TextView) findViewById(R.id.roverifiedbootstate_summary);

        rodebuggable_help = (TextView) findViewById(R.id.rodebuggable_help);
        rosecure_help = (TextView) findViewById(R.id.rosecure_help);
        robuildtags_help = (TextView) findViewById(R.id.robuildtags_help);
        bl_state_help = (TextView) findViewById(R.id.bl_state_help);
        flash_locked_help = (TextView) findViewById(R.id.flash_locked_help);
        roverifiedbootstate_help = (TextView) findViewById(R.id.roverifiedbootstate_help);

        String used_b_rb = getString(R.string.used_by) + "\n" + getString(R.string.banks) + "\n" + getString(R.string.root_beer);
        String safety = getString(R.string.used_by) + "\n" + getString(R.string.safety_net);

        rodebuggable_help.setText(used_b_rb);
        rosecure_help.setText(used_b_rb);
        robuildtags_help.setText(used_b_rb);
        bl_state_help.setText(safety);
        flash_locked_help.setText(safety);
        roverifiedbootstate_help.setText(safety);

        bl_state_view = (View) findViewById(R.id.bl_state_view);
        flash_locked_view = (View) findViewById(R.id.flash_locked_view);
        roverifiedbootstate_view = (View) findViewById(R.id.roverifiedbootstate_view);

        updateprop();
        Tools.stripsu(executableFilePath);
        Tools.saveprop(PropActivityContext);

        rodebuggable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sdebuggable, "1", "0");
            }
        });
        rodebuggable_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sdebuggable, "1", "0");
            }
        });

        rosecure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Srosecure, "1", "0");
            }
        });
        rosecure_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Srosecure, "1", "0");
            }
        });

        robuildtags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sbuildtags, "test-keys", "release-keys");
            }
        });
        robuildtags_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sbuildtags, "test-keys", "release-keys");
            }
        });

        bl_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sbl_state, "1", "0");
            }
        });
        bl_state_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sbl_state, "1", "0");
            }
        });

        flash_locked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sflash_locked, "0", "1");
            }
        });
        flash_locked_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sflash_locked, "0", "1");
            }
        });

        roverifiedbootstate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sroverifiedbootstate, "green", "red");
            }
        });
        roverifiedbootstate_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sroverifiedbootstate, "green", "red");
            }
        });

        ic_launcher = (ImageView) findViewById(R.id.ic_launcher_perapp);
        ic_launcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.DoAToast(getString(R.string.isu_by), PropActivityContext);
            }
        });


    }

    public void updateprop(String prop, String defaultvalue, String newvalue) {
        boolean prop_value = Tools.getprop(prop).contains(defaultvalue);
        Tools.saveString(prop, prop_value ? newvalue : defaultvalue, PropActivityContext);
        Tools.resetprop(executableFilePath, prop_value ? prop + " " + newvalue : prop + " " + defaultvalue);
        updateprop();
    }

    public void updateprop() {
        String debuggable = Tools.getprop(Sdebuggable);
        String secure = Tools.getprop(Srosecure);
        String buildtags = Tools.getprop(Sbuildtags);
        String blstate = Tools.getprop(Sbl_state);
        String flashlocked = Tools.getprop(Sflash_locked);
        String verifiedbootstate = Tools.getprop(Sroverifiedbootstate);

        rodebuggable_summary.setText(debuggable);
        rosecure_summary.setText(secure);
        robuildtags_summary.setText(buildtags);
        bl_state_summary.setText(blstate);
        flash_locked_summary.setText(flashlocked);
        roverifiedbootstate_summary.setText(verifiedbootstate);

        rodebuggable_summary.setTextColor(debuggable.contains("1") ? getColorWrapper(PropActivityContext, R.color.colorAccent) :
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen));
        rosecure_summary.setTextColor(secure.contains("0") ? getColorWrapper(PropActivityContext, R.color.colorAccent) :
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen));
        robuildtags_summary.setTextColor(buildtags.contains("test") ? getColorWrapper(PropActivityContext, R.color.colorAccent) :
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen));
        bl_state_summary.setTextColor(blstate.contains("1") ? getColorWrapper(PropActivityContext, R.color.colorAccent) :
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen));
        flash_locked_summary.setTextColor(flashlocked.contains("0") ? getColorWrapper(PropActivityContext, R.color.colorAccent) :
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen));
        roverifiedbootstate_summary.setTextColor(verifiedbootstate.contains("green") ? getColorWrapper(PropActivityContext, R.color.colorButtonGreen) :
            getColorWrapper(PropActivityContext, R.color.colorAccent));

        if (blstate == null || blstate.isEmpty()) {
            bl_state.setVisibility(View.GONE);
            bl_state_summary.setVisibility(View.GONE);
            bl_state_help.setVisibility(View.GONE);
            bl_state_view.setVisibility(View.GONE);
        }
        if (blstate == null || blstate.isEmpty()) {
            flash_locked.setVisibility(View.GONE);
            flash_locked_summary.setVisibility(View.GONE);
            flash_locked_help.setVisibility(View.GONE);
            flash_locked_view.setVisibility(View.GONE);
        }
        if (blstate == null || blstate.isEmpty()) {
            roverifiedbootstate.setVisibility(View.GONE);
            roverifiedbootstate_summary.setVisibility(View.GONE);
            roverifiedbootstate_help.setVisibility(View.GONE);
            roverifiedbootstate_view.setVisibility(View.GONE);
        }
    }

    private static int getColorWrapper(Context context, int id) {
        return ContextCompat.getColor(context, id);
    }

}
