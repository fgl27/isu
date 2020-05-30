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
package com.bhb27.isu.tools;

public interface Constants {

    String TAG = "iSu";
    String PREF_NAME = "pref";
    String bin_su = "/system/bin/su";
    String xbin_su = "/system/xbin/su";

    //TODO Always Update supersu init  when change below
    String patchversion = "71";

    String init_superuser = "superuser" + patchversion;
    String init_restart = "restart" + patchversion;
    String patchN = "isupatch" + patchversion;
    //M pathc is running from kernel_zip not from assets
    String patchM = "isupatch60";

    String GETENFORCE = "getenforce";
    String SETENFORCE = "setenforce";

    int NOTIFY_ID_SU = 100;
    int NOTIFY_ID_PROPS = 101;
    int NOTIFY_ID_BOOT = 102;

    //"allow #source-class #target-class permission-class #permission" 
    //Sections marked with '#' can be replaced with collections in curly brackets
    //e.g: allow { source1 source2 } { target1 target2 } permission-class { permission1 permission2 }
    //be aware if one thing from a bracket fail the line fail that is way is all separeted
    String[] MagiskPolicy = new String[] {
    };

    String YES_ACTION = "YES_ACTION";
    String DISSMISS_ACTION = "DISSMISS_ACTION";

    String SWITCH_DELAY = "monitor_delay";

    // tasker
    String TASKER_SU_ON = "isu.su.on";
    String TASKER_SU_OFF = "isu.su.off";
    String TASKER_SU_INV = "isu.su.inverse";
    String TASKER_SELINUX_ON = "isu.selinux.on";
    String TASKER_SELINUX_OFF = "isu.selinux.off";
    String TASKER_SELINUX_INV = "isu.selinux.inverse";
    String TASKER_DEBUG_ON = "isu.debug.on";
    String TASKER_DEBUG_OFF = "isu.debug.off";
    String TASKER_DEBUG_INV = "isu.debug.inverse";

    String BUILD_PROP = "system/build.prop";

    String PAY = "com.google.android.apps.walletnfcrel";

    String SAFEFINGERPRINT = "google/shamu/shamu:7.1.1/N6F26U/3687496:user/release-keys";
    String robuildfingerprint = "ro.build.fingerprint";
    String robootbuildfingerprint = "ro.bootimage.build.fingerprint";

    String[] props = new String[] {
        "ro.build.tags",
        "ro.debuggable",
        "ro.boot.bl_state",
        "ro.boot.flash.locked",
        "ro.boot.verifiedbootstate",
        "ro.secure",
        "ro.boot.veritymode",
        "ro.build.type",
        "ro.build.selinux",
        "ro.boot.selinux",
        "ro.boot.warranty_bit"
    };

    String[] props_OK = new String[] {
        "release-keys",
        "0",
        "0",
        "1",
        "green",
        "1",
        "enforcing",
        "user",
        "1",
        "enforcing",
        "0"
    };

    String[] props_NOK = new String[] {
        "test-keys",
        "1",
        "2",
        "0",
        "orange",
        "0",
        "logging",
        "userdebug",
        "0",
        "permissive",
        "1"
    };

    String[] props_fail_sf = new String[] {
        "ro.boot.flash.locked",
        "ro.boot.verifiedbootstate",
        "ro.secure"
    };

    String[] props_fail_sf_OK = new String[] {
        "1",
        "green",
        "1"
    };

}
