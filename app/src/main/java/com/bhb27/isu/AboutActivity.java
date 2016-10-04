/*
 * Copyright (C) 2016 Felipe de Leon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bhb27.isu;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity {
    // in order of appearance
    TextView about, version, version_number, version_summary, dev_info, email, email_summary, xda, git, git_summary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_fragment);

        View frameLayout = findViewById(R.id.aboutLayout);

        about = (TextView) findViewById(R.id.about);
        about.setText(R.string.about);

        version = (TextView) findViewById(R.id.version);
        version.setText(R.string.version);

        version_number = (TextView) findViewById(R.id.version_number);
        version_number.setText(R.string.version_number);

        version_summary = (TextView) findViewById(R.id.version_summary);
        version_summary.setText(R.string.version_summary);

        dev_info = (TextView) findViewById(R.id.dev_info);
        dev_info.setText(R.string.dev_info);

        email = (TextView) findViewById(R.id.email);
        email.setText("Email");

        email_summary = (TextView) findViewById(R.id.email_summary);
        email_summary.setText("fglfgl27@gmail.com");

        xda = (TextView) findViewById(R.id.xda);
        xda.setText(R.string.xda_summary);

        git = (TextView) findViewById(R.id.git);
        git.setText("GitHub");

        git_summary = (TextView) findViewById(R.id.git_summary);
        git_summary.setText(R.string.git_summary);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:fglfgl27@gmail.com")));
            }
        });

        email_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:fglfgl27@gmail.com")));
            }
        });

        xda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/member.php?u=5747496")));
            }
        });

        git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bhb27/turbotoast")));
            }
        });

        git_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bhb27/turbotoast")));
            }
        });
    }
}
