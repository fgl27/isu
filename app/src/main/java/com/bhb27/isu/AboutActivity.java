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
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
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
    TextView version_number, email, email_summary, xda, git, git_summary;
    ImageView ic_gmail, ic_xda, ic_git;

    private final Tools tools_class = new Tools();
    private Context AboutContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_fragment);
        AboutContext = this;
        final String SUBJECT = getString(R.string.app_name) + " " +
            BuildConfig.VERSION_NAME + " (" + Build.MODEL + " " + Build.VERSION.RELEASE + ")";

        View frameLayout = findViewById(R.id.aboutLayout);

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

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:fglfgl27@gmail.com"))
                        .putExtra(Intent.EXTRA_SUBJECT, SUBJECT));
                } catch (ActivityNotFoundException ex) {
                    tools_class.DoAToast(getString(R.string.no_email_client), AboutContext);
                }
            }
        });

        email_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:fglfgl27@gmail.com"))
                        .putExtra(Intent.EXTRA_SUBJECT, SUBJECT));
                } catch (ActivityNotFoundException ex) {
                    tools_class.DoAToast(getString(R.string.no_email_client), AboutContext);
                }
            }
        });

        xda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/android/apps-games/isu-simple-app-to-deactivate-activate-t3478348")));
                } catch (ActivityNotFoundException ex) {
                    tools_class.DoAToast(getString(R.string.no_browser), AboutContext);
                }
            }
        });

        git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bhb27/isu")));
                } catch (ActivityNotFoundException ex) {
                    tools_class.DoAToast(getString(R.string.no_browser), AboutContext);
                }
            }
        });

        git_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bhb27/isu")));
                } catch (ActivityNotFoundException ex) {
                    tools_class.DoAToast(getString(R.string.no_browser), AboutContext);
                }
            }
        });

        //imageview
        ic_gmail = (ImageView) findViewById(R.id.ic_gmail);
        ic_xda = (ImageView) findViewById(R.id.ic_xda);
        ic_git = (ImageView) findViewById(R.id.ic_git);

        ic_gmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:fglfgl27@gmail.com"))
                        .putExtra(Intent.EXTRA_SUBJECT, SUBJECT));
                } catch (ActivityNotFoundException ex) {
                    tools_class.DoAToast(getString(R.string.no_email_client), AboutContext);
                }
            }
        });

        ic_xda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/android/apps-games/isu-simple-app-to-deactivate-activate-t3478348")));
                } catch (ActivityNotFoundException ex) {
                    tools_class.DoAToast(getString(R.string.no_browser), AboutContext);
                }
            }
        });

        ic_git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bhb27/isu")));
                } catch (ActivityNotFoundException ex) {
                    tools_class.DoAToast(getString(R.string.no_browser), AboutContext);
                }
            }
        });
    }

}
