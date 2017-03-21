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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.content.ContextCompat;

import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.tools.Constants;

public class PropActivity extends Activity {

    private TextView rodebuggable, rosecure, robuildtags, bl_state, flash_locked, roverifiedbootstate, roveritymode, robuildtype, robuildselinux, robootselinux,
    rodebuggable_summary, rosecure_summary, robuildtags_summary, bl_state_summary, flash_locked_summary, roverifiedbootstate_summary, roveritymode_summary, robuildtype_summary, robuildselinux_summary, robootselinux_summary,
    rodebuggable_help, rosecure_help, robuildtags_help, bl_state_help, flash_locked_help, roverifiedbootstate_help, roveritymode_help, robuildtype_help, robuildselinux_help, robootselinux_help;

    private View rodebuggable_view, rosecure_view, robuildtags_view, bl_state_view, flash_locked_view, roverifiedbootstate_view, roveritymode_view, robuildtype_view, robuildselinux_view, robootselinux_view;

    private Context PropActivityContext = null;

    private String executableFilePath;

    private String Sdebuggable = Constants.props[1], Sbuildtags = Constants.props[0],
        Sbl_state = Constants.props[2], Sflash_locked = Constants.props[3],
        Sroverifiedbootstate = Constants.props[4], Srosecure = Constants.props[5],
        Sroveritymode = Constants.props[6], Srobuildtype = Constants.props[7],
        Srobuildselinux = Constants.props[8], Srobootselinux = Constants.props[9];

    private String VOKdebuggable = Constants.props_OK[1], VOKbuildtags = Constants.props_OK[0],
        VOKbl_state = Constants.props_OK[2], VOKflash_locked = Constants.props_OK[3],
        VOKroverifiedbootstate = Constants.props_OK[4], VOKrosecure = Constants.props_OK[5],
        VOKroveritymode = Constants.props_OK[6], VOKrobuildtype = Constants.props_OK[7],
        VOKrobuildselinux = Constants.props_OK[8], VOKrobootselinux = Constants.props_OK[9];

    private String VNOKdebuggable = Constants.props_NOK[1], VNOKbuildtags = Constants.props_NOK[0],
        VNOKbl_state = Constants.props_NOK[2], VNOKflash_locked = Constants.props_NOK[3],
        VNOKroverifiedbootstate = Constants.props_NOK[4], VNOKrosecure = Constants.props_NOK[5],
        VNOKroveritymode = Constants.props_NOK[6], VNOKrobuildtype = Constants.props_NOK[7],
        VNOKrobuildselinux = Constants.props_NOK[8], VNOKrobootselinux = Constants.props_NOK[9];

    private ImageView ic_launcher;

    private Button set_all_green, set_all_red;

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
        roveritymode = (TextView) findViewById(R.id.roveritymode);
        robuildtype = (TextView) findViewById(R.id.robuildtype);
        robuildselinux = (TextView) findViewById(R.id.robuildselinux);
        robootselinux = (TextView) findViewById(R.id.robootselinux);

        set_all_green = (Button) findViewById(R.id.set_all_green);
        set_all_red = (Button) findViewById(R.id.set_all_red);

        rodebuggable.setText(String.format(getString(R.string.equal), Sdebuggable));
        rosecure.setText(String.format(getString(R.string.equal), Srosecure));
        robuildtags.setText(String.format(getString(R.string.equal), Sbuildtags));
        bl_state.setText(String.format(getString(R.string.equal), Sbl_state));
        flash_locked.setText(String.format(getString(R.string.equal), Sflash_locked));
        roverifiedbootstate.setText(String.format(getString(R.string.equal), Sroverifiedbootstate));
        roveritymode.setText(String.format(getString(R.string.equal), Sroveritymode));
        robuildtype.setText(String.format(getString(R.string.equal), Srobuildtype));
        robuildselinux.setText(String.format(getString(R.string.equal), Srobuildselinux));
        robootselinux.setText(String.format(getString(R.string.equal), Srobootselinux));

        rodebuggable_summary = (TextView) findViewById(R.id.rodebuggable_summary);
        rosecure_summary = (TextView) findViewById(R.id.rosecure_summary);
        robuildtags_summary = (TextView) findViewById(R.id.robuildtags_summary);
        bl_state_summary = (TextView) findViewById(R.id.bl_state_summary);
        flash_locked_summary = (TextView) findViewById(R.id.flash_locked_summary);
        roverifiedbootstate_summary = (TextView) findViewById(R.id.roverifiedbootstate_summary);
        roveritymode_summary = (TextView) findViewById(R.id.roveritymode_summary);
        robuildtype_summary = (TextView) findViewById(R.id.robuildtype_summary);
        robuildselinux_summary = (TextView) findViewById(R.id.robuildselinux_summary);
        robootselinux_summary = (TextView) findViewById(R.id.robootselinux_summary);

        rodebuggable_help = (TextView) findViewById(R.id.rodebuggable_help);
        rosecure_help = (TextView) findViewById(R.id.rosecure_help);
        robuildtags_help = (TextView) findViewById(R.id.robuildtags_help);
        bl_state_help = (TextView) findViewById(R.id.bl_state_help);
        flash_locked_help = (TextView) findViewById(R.id.flash_locked_help);
        roverifiedbootstate_help = (TextView) findViewById(R.id.roverifiedbootstate_help);
        roveritymode_help = (TextView) findViewById(R.id.roveritymode_help);
        robuildtype_help = (TextView) findViewById(R.id.robuildtype_help);
        robuildselinux_help = (TextView) findViewById(R.id.robuildselinux_help);
        robootselinux_help = (TextView) findViewById(R.id.robootselinux_help);

        String used_b_rb = getString(R.string.used_by) + "\n" + getString(R.string.banks) + "\n" + getString(R.string.root_beer);
        String safety = getString(R.string.used_by) + "\n" + getString(R.string.safety_net);

        rodebuggable_help.setText(used_b_rb);
        rosecure_help.setText(used_b_rb);
        robuildtags_help.setText(used_b_rb);
        bl_state_help.setText(safety);
        flash_locked_help.setText(safety);
        roverifiedbootstate_help.setText(safety);
        roveritymode_help.setText(safety);
        robuildtype_help.setText(safety);
        robuildselinux_help.setText(safety);
        robootselinux_help.setText(safety);

        rodebuggable_view = (View) findViewById(R.id.rodebuggable_view);
        rosecure_view = (View) findViewById(R.id.rosecure_view);
        robuildtags_view = (View) findViewById(R.id.robuildtags_view);
        bl_state_view = (View) findViewById(R.id.bl_state_view);
        flash_locked_view = (View) findViewById(R.id.flash_locked_view);
        roverifiedbootstate_view = (View) findViewById(R.id.roverifiedbootstate_view);
        roveritymode_view = (View) findViewById(R.id.roveritymode_view);
        robuildtype_view = (View) findViewById(R.id.robuildtype_view);
        robuildselinux_view = (View) findViewById(R.id.robuildselinux_view);
        robootselinux_view = (View) findViewById(R.id.robootselinux_view);

        Runnable runThread = new Runnable() {
            public void run() {
                if (Tools.SuVersionBool(Tools.SuVersion(PropActivityContext)))
                    Tools.stripsu(executableFilePath);
                Tools.saveprop(PropActivityContext);
            }
        };
        new Thread(runThread).start();

        rodebuggable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sdebuggable, VOKdebuggable, VNOKdebuggable);
            }
        });
        rodebuggable_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sdebuggable, VOKdebuggable, VNOKdebuggable);
            }
        });

        rosecure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Srosecure, VOKrosecure, VNOKrosecure);
            }
        });
        rosecure_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Srosecure, VOKrosecure, VNOKrosecure);
            }
        });

        robuildtags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sbuildtags, VOKbuildtags, VNOKbuildtags);
            }
        });
        robuildtags_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sbuildtags, VOKbuildtags, VNOKbuildtags);
            }
        });

        bl_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sbl_state, VOKbl_state, VNOKbl_state);
            }
        });
        bl_state_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sbl_state, VOKbl_state, VNOKbl_state);
            }
        });

        flash_locked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sflash_locked, VOKflash_locked, VNOKflash_locked);
            }
        });
        flash_locked_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sflash_locked, VOKflash_locked, VNOKflash_locked);
            }
        });

        roverifiedbootstate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sroverifiedbootstate, VOKroverifiedbootstate, VNOKroverifiedbootstate);
            }
        });
        roverifiedbootstate_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sroverifiedbootstate, VOKroverifiedbootstate, VNOKroverifiedbootstate);
            }
        });

        roveritymode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sroveritymode, VOKroveritymode, VNOKroveritymode);
            }
        });
        roveritymode_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Sroveritymode, VOKroveritymode, VNOKroveritymode);
            }
        });

        robuildtype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Srobuildtype, VOKrobuildtype, VNOKrobuildtype);
            }
        });
        robuildtype_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Srobuildtype, VOKrobuildtype, VNOKrobuildtype);
            }
        });

        robuildselinux.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Srobuildselinux, VOKrobuildselinux, VNOKrobuildselinux);
            }
        });
        robuildselinux_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Srobuildselinux, VOKrobuildselinux, VNOKrobuildselinux);
            }
        });

        robootselinux.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Srobootselinux, VOKrobootselinux, VNOKrobootselinux);
            }
        });
        robootselinux_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateprop(Srobootselinux, VOKrobootselinux, VNOKrobootselinux);
            }
        });

        ic_launcher = (ImageView) findViewById(R.id.ic_launcher_perapp);
        ic_launcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.DoAToast(getString(R.string.isu_by), PropActivityContext);
            }
        });

        set_all_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.resetallprop(executableFilePath, true, PropActivityContext);
                updateprop();
            }
        });

        set_all_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.resetallprop(executableFilePath, false, PropActivityContext);
                updateprop();
            }
        });
        updateprop();
    }

    public void updateprop(String prop, String defaultvalue, String newvalue) {
        boolean prop_value = Tools.getprop(prop).equals(defaultvalue);
        Tools.resetprop(executableFilePath, prop, prop_value ? newvalue : defaultvalue, PropActivityContext);
        updateprop();
    }

    public void updateprop() {
        String debuggable = Tools.getprop(Sdebuggable);
        String secure = Tools.getprop(Srosecure);
        String buildtags = Tools.getprop(Sbuildtags);
        String blstate = Tools.getprop(Sbl_state);
        String flashlocked = Tools.getprop(Sflash_locked);
        String verifiedbootstate = Tools.getprop(Sroverifiedbootstate);
        String veritymode = Tools.getprop(Sroveritymode);
        String buildtype = Tools.getprop(Srobuildtype);
        String buildselinux = Tools.getprop(Srobuildselinux);
        String bootselinux = Tools.getprop(Srobootselinux);

        rodebuggable_summary.setText(debuggable);
        rosecure_summary.setText(secure);
        robuildtags_summary.setText(buildtags);
        bl_state_summary.setText(blstate);
        flash_locked_summary.setText(flashlocked);
        roverifiedbootstate_summary.setText(verifiedbootstate);
        roveritymode_summary.setText(veritymode);
        robuildtype_summary.setText(buildtype);
        robuildselinux_summary.setText(buildselinux);
        robootselinux_summary.setText(bootselinux);

        // below are the "OK" values
        rodebuggable_summary.setTextColor(debuggable.equals(VOKdebuggable) ?
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen) :
            getColorWrapper(PropActivityContext, R.color.colorAccent));
        rosecure_summary.setTextColor(secure.equals(VOKrosecure) ?
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen) :
            getColorWrapper(PropActivityContext, R.color.colorAccent));
        robuildtags_summary.setTextColor(buildtags.equals(VOKbuildtags) ?
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen) :
            getColorWrapper(PropActivityContext, R.color.colorAccent));
        bl_state_summary.setTextColor(blstate.equals(VOKbl_state) ?
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen) :
            getColorWrapper(PropActivityContext, R.color.colorAccent));
        flash_locked_summary.setTextColor(flashlocked.equals(VOKflash_locked) ?
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen) :
            getColorWrapper(PropActivityContext, R.color.colorAccent));
        roverifiedbootstate_summary.setTextColor(verifiedbootstate.equals(VOKroverifiedbootstate) ?
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen) :
            getColorWrapper(PropActivityContext, R.color.colorAccent));
        roveritymode_summary.setTextColor(veritymode.equals(VOKroveritymode) ?
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen) :
            getColorWrapper(PropActivityContext, R.color.colorAccent));
        robuildtype_summary.setTextColor(buildtype.equals(VOKrobuildtype) ?
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen) :
            getColorWrapper(PropActivityContext, R.color.colorAccent));
        robuildselinux_summary.setTextColor(buildselinux.equals(VOKrobuildselinux) ?
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen) :
            getColorWrapper(PropActivityContext, R.color.colorAccent));
        robootselinux_summary.setTextColor(bootselinux.equals(VOKrobootselinux) ?
            getColorWrapper(PropActivityContext, R.color.colorButtonGreen) :
            getColorWrapper(PropActivityContext, R.color.colorAccent));

        if (secure == null || secure.isEmpty()) {
            rosecure.setVisibility(View.GONE);
            rosecure_summary.setVisibility(View.GONE);
            rosecure_help.setVisibility(View.GONE);
            rosecure_view.setVisibility(View.GONE);
        }
        if (buildtags == null || buildtags.isEmpty()) {
            robuildtags.setVisibility(View.GONE);
            robuildtags_summary.setVisibility(View.GONE);
            robuildtags_help.setVisibility(View.GONE);
            robuildtags_view.setVisibility(View.GONE);
        }
        if (debuggable == null || debuggable.isEmpty()) {
            rodebuggable.setVisibility(View.GONE);
            rodebuggable_summary.setVisibility(View.GONE);
            rodebuggable_help.setVisibility(View.GONE);
            rodebuggable_view.setVisibility(View.GONE);
        }
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
        if (veritymode == null || veritymode.isEmpty()) {
            roveritymode.setVisibility(View.GONE);
            roveritymode_summary.setVisibility(View.GONE);
            roveritymode_help.setVisibility(View.GONE);
            roveritymode_view.setVisibility(View.GONE);
        }
        if (buildtype == null || buildtype.isEmpty()) {
            robuildtype.setVisibility(View.GONE);
            robuildtype_summary.setVisibility(View.GONE);
            robuildtype_help.setVisibility(View.GONE);
            robuildtype_view.setVisibility(View.GONE);
        }
        if (buildselinux == null || buildselinux.isEmpty()) {
            robuildselinux.setVisibility(View.GONE);
            robuildselinux_summary.setVisibility(View.GONE);
            robuildselinux_help.setVisibility(View.GONE);
            robuildselinux_view.setVisibility(View.GONE);
        }
        if (bootselinux == null || bootselinux.isEmpty()) {
            robootselinux.setVisibility(View.GONE);
            robootselinux_summary.setVisibility(View.GONE);
            robootselinux_help.setVisibility(View.GONE);
            robootselinux_view.setVisibility(View.GONE);
        }
    }

    private static int getColorWrapper(Context context, int id) {
        return ContextCompat.getColor(context, id);
    }

}
