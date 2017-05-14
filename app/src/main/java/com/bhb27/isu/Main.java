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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;

import com.bhb27.isu.AboutActivity;
import com.bhb27.isu.Checks;
import com.bhb27.isu.Controls;
import com.bhb27.isu.Monitor;
import com.bhb27.isu.Props;
import com.bhb27.isu.Settings;
import com.bhb27.isu.tools.Tools;

public class Main extends AppCompatActivity {

    private TextView mAbout;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CustomContextWrapper.wrap(context));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        String cmiyc = Tools.readString("cmiyc", null, this);
        if (cmiyc == null || cmiyc.isEmpty())
            Tools.saveString("cmiyc", Tools.random4(), this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new TabsPagerAdapter(getFragmentManager()));
        viewPager.setOffscreenPageLimit(getTitles().length);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        mAbout = (TextView) findViewById(R.id.about);
        mAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(myIntent);
            }
        });
        check_writeexternalstorage();
        try {
            this.registerReceiver(updateMainReceiver, new IntentFilter("updateMainReceiver"));
        } catch (NullPointerException ignored) {}
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            this.registerReceiver(updateMainReceiver, new IntentFilter("updateMainReceiver"));
        } catch (NullPointerException ignored) {}
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            this.unregisterReceiver(updateMainReceiver);
        } catch (IllegalArgumentException ignored) {}
    }

    public class TabsPagerAdapter extends FragmentPagerAdapter {

        String titles[] = getTitles();
        private Fragment frags[] = new Fragment[titles.length];

        public TabsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            frags[0] = new Checks();
            frags[1] = new Controls();
            frags[2] = new Monitor();
            frags[3] = new Props();
            frags[4] = new Settings();
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return frags.length;

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    private String[] getTitles() {
        String titleString[];
        titleString = new String[] {
            getString(R.string.checks),
                getString(R.string.controls),
                getString(R.string.monitor),
                getString(R.string.props),
                getString(R.string.settings)
        };
        return titleString;
    }

    @TargetApi(23 | 24 | 25)
    private void check_writeexternalstorage() {
        if (Build.VERSION.SDK_INT >= 23) {
            int hasWriteExternalPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteExternalPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }
        return;
    }

    private final BroadcastReceiver updateMainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
}
