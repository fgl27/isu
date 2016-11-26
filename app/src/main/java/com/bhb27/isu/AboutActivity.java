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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bhb27.isu.Tools;

public class AboutActivity extends Activity {
    // in order of appearance
    TextView about, version, version_number, version_summary, dev_info, email, email_summary, xda, git, git_summary;
    ImageView ic_gmail, ic_xda, ic_git;

    private final Tools tools_class = new Tools();
    private Context AboutContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_fragment);
        AboutContext = this;

        View frameLayout = findViewById(R.id.aboutLayout);

        //textview
        about = (TextView) findViewById(R.id.about);
        about.setText(getString(R.string.about));

        version = (TextView) findViewById(R.id.version);
        version.setText(getString(R.string.version));

        version_number = (TextView) findViewById(R.id.version_number);
        version_number.setText(getString(R.string.version_number));

        version_summary = (TextView) findViewById(R.id.version_summary);
        version_summary.setText(getString(R.string.version_summary));

        dev_info = (TextView) findViewById(R.id.dev_info);
        dev_info.setText(getString(R.string.dev_info));

        email = (TextView) findViewById(R.id.email);
        email.setText(getString(R.string.email));

        email_summary = (TextView) findViewById(R.id.email_summary);
        email_summary.setText(getString(R.string.email_summary));

        xda = (TextView) findViewById(R.id.xda);
        xda.setText(getString(R.string.xda_summary));

        git = (TextView) findViewById(R.id.git);
        git.setText(getString(R.string.github));

        git_summary = (TextView) findViewById(R.id.git_summary);
        git_summary.setText(getString(R.string.git_summary));

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:fglfgl27@gmail.com")));
                } catch (ActivityNotFoundException ex) {
                    tools_class.DoAToast(getString(R.string.no_email_client), AboutContext);
                }
            }
        });

        email_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:fglfgl27@gmail.com")));
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
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:fglfgl27@gmail.com")));
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
