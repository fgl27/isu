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
import android.os.Build;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bhb27.isu.tools.Tools;
import com.bhb27.isu.BuildConfig;

public class AboutActivity extends Activity {
    // in order of appearance
    TextView version_number, email, email_summary, xda, git, git_summary, app_license, appcompat_v7, busybox,
    design, magiskpolicy, material_dialogs, okhttp, play_safetynet, preference_v14, resetprop, support_v4, support_v13, ztzip;
    ImageView ic_gmail, ic_xda, ic_git;
    private String SUBJECT;

    private Context AboutContext = null;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CustomContextWrapper.wrap(context));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_fragment);
        AboutContext = this;
        SUBJECT = getString(R.string.app_name) + " " +
            BuildConfig.VERSION_NAME + " (" + Build.MODEL + " " + Build.VERSION.RELEASE + ")";

        LinearLayout layout = (LinearLayout) findViewById(R.id.aboutLayout);
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setFillAfter(true);
        animation.setDuration(500);
        layout.startAnimation(animation);

        version_number = (TextView) findViewById(R.id.version_number);
        version_number.setText(BuildConfig.VERSION_NAME);

        email = (TextView) findViewById(R.id.email);
        email_summary = (TextView) findViewById(R.id.email_summary);

        xda = (TextView) findViewById(R.id.xda);

        git = (TextView) findViewById(R.id.git);
        git_summary = (TextView) findViewById(R.id.git_summary);

        app_license = (TextView) findViewById(R.id.app_license);
        appcompat_v7 = (TextView) findViewById(R.id.appcompat_v7);
        busybox = (TextView) findViewById(R.id.busybox);
        design = (TextView) findViewById(R.id.design);
        magiskpolicy = (TextView) findViewById(R.id.magiskpolicy);
        material_dialogs = (TextView) findViewById(R.id.material_dialogs);
        okhttp = (TextView) findViewById(R.id.okhttp);
        play_safetynet = (TextView) findViewById(R.id.play_safetynet);
        preference_v14 = (TextView) findViewById(R.id.preference_v14);
        resetprop = (TextView) findViewById(R.id.resetprop);
        support_v4 = (TextView) findViewById(R.id.support_v4);
        support_v13 = (TextView) findViewById(R.id.support_v13);
        ztzip = (TextView) findViewById(R.id.ztzip);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.email(AboutContext, SUBJECT);
            }
        });

        email_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.email(AboutContext, SUBJECT);
            }
        });

        xda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("http://forum.xda-developers.com/android/apps-games/isu-simple-app-to-deactivate-activate-t3478348", AboutContext);
            }
        });

        git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://github.com/bhb27/isu", AboutContext);
            }
        });

        git_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://github.com/bhb27/isu", AboutContext);
            }
        });

        //imageview
        ic_gmail = (ImageView) findViewById(R.id.ic_gmail);
        ic_xda = (ImageView) findViewById(R.id.ic_xda);
        ic_git = (ImageView) findViewById(R.id.ic_git);

        ic_gmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.email(AboutContext, SUBJECT);
            }
        });

        ic_xda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("http://forum.xda-developers.com/android/apps-games/isu-simple-app-to-deactivate-activate-t3478348", AboutContext);
            }
        });

        ic_git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://github.com/bhb27/isu", AboutContext);
            }
        });

        app_license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://github.com/bhb27/isu/blob/master/LICENSE", AboutContext);
            }
        });

        appcompat_v7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://developer.android.com/topic/libraries/support-library/packages.html#v7-appcompat", AboutContext);
            }
        });

        busybox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://forum.xda-developers.com/showthread.php?t=2239421", AboutContext);
            }
        });

        design.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://developer.android.com/topic/libraries/support-library/packages.html#design", AboutContext);
            }
        });

        magiskpolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://github.com/topjohnwu/Magisk#credits", AboutContext);
            }
        });

        material_dialogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://github.com/afollestad/material-dialogs", AboutContext);
            }
        });

        okhttp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://github.com/square/okhttp", AboutContext);
            }
        });

        play_safetynet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://developers.google.com/android/guides/setup#ensure_devices_have_the_google_play_services_apk", AboutContext);
            }
        });

        preference_v14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://developer.android.com/topic/libraries/support-library/packages.html#v14-preference", AboutContext);
            }
        });

        resetprop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://github.com/topjohnwu/Magisk#credits", AboutContext);
            }
        });

        support_v4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://developer.android.com/topic/libraries/support-library/packages.html#v4", AboutContext);
            }
        });

        support_v13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://developer.android.com/topic/libraries/support-library/packages.html#v13", AboutContext);
            }
        });

        ztzip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.browser("https://github.com/zeroturnaround/zt-zip", AboutContext);
            }
        });

    }

}
