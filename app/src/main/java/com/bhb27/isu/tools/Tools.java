/*
 * Copyright (C) 2015-2016 Willi Ye <williye97@gmail.com>
 *
 * This file is part of Kernel Adiutor.
 *
 * Kernel Adiutor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kernel Adiutor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kernel Adiutor.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.bhb27.isu.tools;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import android.preference.Preference;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.annotation.TargetApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.bhb27.isu.Main;
import com.bhb27.isu.R;
import com.bhb27.isu.perapp.PropDB;
import com.bhb27.isu.tools.RootFile;
import com.bhb27.isu.tools.RootUtils;
import com.bhb27.isu.widgetservice.Widgeth;
import com.bhb27.isu.widgetservice.Widgetv;
import com.bhb27.isu.widgetservice.Widgetsu;

public class Tools implements Constants {

    public static String runCommand(String command, boolean su, Context context) {
        if (su)
            return RootUtils.runCommand(command);
        else
            return RootUtils.runICommand(command, context);
    }

    public static boolean KernelSupport(Context context) {
        String kernel_support_rc = runCommand("grep -r -i " + patchM + " *.rc ", SuBinary(), context) + "";
        if (kernel_support_rc.contains(patchM))
            return true;
        else
            return false;
    }

    public static void updateMain(Context context, String toast) {
        closeSU();
        DoAToast(toast, context);
        SendBroadcast("updateMainReceiver", context);
        context.startActivity(new Intent(context, Main.class));
    }

    public static void closeSU() {
        RootUtils.closeSU();
        RootUtils.closeISU();
    }

    @SuppressWarnings("deprecation")
    public static String sysLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
        else
            return Resources.getSystem().getConfiguration().locale.getLanguage();
    }

    public static boolean ReadSystemPatch(Context context) {
        String reboot_support_rc = runCommand("grep -i " + patchN + " system/etc/init/superuser.rc", SuBinary(), context) + "";
        if (reboot_support_rc.contains(patchN))
            return true;
        return false;
    }

    public static void SystemPatch(String executableFilePath, Context context) {
        String seclabel = "";
        boolean su = SuBinary();
        seclabel = runCommand("cat system/etc/init/superuser.rc | grep seclabel | head -1", su, context);
        runCommand("mount -o rw,remount /system", su, context);
        runCommand("chmod 755" + executableFilePath + "restart", su, context);
        runCommand("cp -f " + executableFilePath + init_superuser + " /system/etc/init/superuser.rc", su, context);
        runCommand(executableFilePath + "busybox sed -i '/seclabel/c\\    " + seclabel + "' system/etc/init/superuser.rc ", su, context);
        //            RootUtils.runCommand(executableFilePath + "busybox sed -i 's/YYYY\\b/" + readString("cmiyc", null, context) + "/g' system/etc/init/superuser.rc ");
        runCommand("chmod 644" + " /system/etc/init/superuser.rc", su, context);
        if (NewexistFile("/system/xbin/isush", true, context))
            runCommand("rm -rf /system/xbin/isush", su, context);
        runCommand("mount -o ro,remount /system", su, context);
    }

    public static void PatchSepolicy(String executableFilePath, Context context) {
        runCommand("LD_LIBRARY_PATH=" + executableFilePath + " " + executableFilePath + sepolicy, SuBinary(), context);
    }

    public static void updateAllWidgetsLayouts(Context context) {
        updateAllWidgets(true, context, R.layout.widget_layouth, Widgeth.class);
        updateAllWidgets(true, context, R.layout.widget_layoutv, Widgetv.class);
        updateAllWidgets(false, context, R.layout.widget_layoutsu, Widgetsu.class);
    }

    public static void updateAllWidgets(boolean SU_SEL, final Context context,
        final int layoutResourceId,
        final Class < ? extends AppWidgetProvider > appWidgetClass) {
        boolean su = SuBinary();
        boolean selinux = isSELinuxActive(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutResourceId);
        remoteViews.setTextViewText(R.id.iSuMain, "SU" + "\n" + (su ?
            context.getString(R.string.activated) : context.getString(R.string.deactivated)));
        remoteViews.setInt(R.id.iSuMain, "setBackgroundResource", (su ? R.drawable.button :
            R.drawable.buttong));
        if (SU_SEL) {
            remoteViews.setTextViewText(R.id.iSuMonitor, context.getString(R.string.selinux) + "\n" + (selinux ? context.getString(R.string.enforcing) :
                context.getString(R.string.permissive)));
            remoteViews.setInt(R.id.iSuMonitor, "setBackgroundResource", (selinux ? R.drawable.buttong :
                R.drawable.button));
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, appWidgetClass));
        appWidgetManager.partiallyUpdateAppWidget(appWidgetIds, remoteViews);
    }

    public static void SendBroadcast(String action, Context context) {
        final Intent NewIntent = new Intent();
        NewIntent.setAction(action);
        context.sendBroadcast(NewIntent);
    }

    public static void extractAssets(String executableFilePath, String filename, Context context) {
        String InerexecutableFilePath = executableFilePath + filename;
        AssetManager assetManager = context.getAssets();
        InputStream inStream = null;
        OutputStream outStream = null;

        try {

            inStream = assetManager.open(filename);
            outStream = new FileOutputStream(InerexecutableFilePath); // for override file content
            //outStream = new FileOutputStream(out,true); // for append file content

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();

        } catch (IOException e) {
            Log.e(TAG, "Failed to copy asset file: " + filename, e);
        }
        File execFile = new File(InerexecutableFilePath);
        execFile.setExecutable(true);
        Log.d(TAG, "Copy success: " + filename);
        if (filename.contains("restart") || filename.contains("superuser")) {
            boolean su = SuBinary();
            if (filename.contains("superuser")) {
                Log.d(TAG, "Copy success: supersu if");
                runCommand("mv -f " + executableFilePath + filename + " " + executableFilePath + init_superuser, su, context);
            } else {
                runCommand("mount -o rw,remount /system", su, context);
                runCommand("cp -f " + executableFilePath + filename + " /system/xbin/" + filename, su, context);
                runCommand("mv -f " + executableFilePath + filename + " " + executableFilePath + init_restart, su, context);
                runCommand("chmod 755 /system/xbin/restart", su, context);
                runCommand("mount -o ro,remount /system", su, context);
            }
        }

    }

    public static void subackup(String executableFilePath, Context context) {
        boolean su = SuBinary();
        runCommand("mount -o rw,remount /system", su, context);
        if (!NewexistFile("/data/backup_isu", true, context))
            runCommand("cp -f " + xbin_su + " /data/backup_isu", su, context);
        if (!NewexistFile("/system/xbin/restart", true, context)) {
            runCommand("cp -f " + executableFilePath + init_restart + " /system/xbin/restart", su, context);
        }
        runCommand("chmod 755 /system/xbin/restart", su, context);
        Log.d(TAG, "backup_restart = " + runCommand(executableFilePath + "busybox ls -l /system/xbin/restart", su, context));
        Log.d(TAG, "backup_isu = " + runCommand(executableFilePath + "busybox ls -l /data/backup_isu", su, context));
        runCommand("mount -o ro,remount /system", su, context);
    }

    public static boolean rootAccess(Context context) {
        if (runShell("getprop persist.sys.root_access").equals("0"))
            return false;
        if (RootUtils.rootAccess())
            return true;
        if (RootUtils.rootAccessiSu(context))
            return true;
        return false;
    }

    public static void SimpleDialog(String message, Context context) {
        new AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setMessage(message)
            .setNegativeButton(context.getString(R.string.dismiss),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                }).show();
    }

    public static void killapp(String app, Context context) {
        boolean su = SuBinary();
        String executableFilePath = context.getFilesDir().getPath() + "/";
        String appRunning = runCommand("ps | grep " + app, su, context);
        String UID = runCommand(executableFilePath + "busybox ps | grep " +
            app + " | head -1  | cut -d' ' -f1 ", su, context);
        if (appRunning.contains(app))
            runCommand("kill " + UID, su, context);
    }

    public static void delbinsu(Context context) {
        boolean su = SuBinary();
        runCommand("mount -o rw,remount /system", su, context);
        runCommand("rm -rf " + bin_su, su, context);
        runCommand("mount -o ro,remount /system", su, context);
    }

    public static void SwitchSu(boolean isChecked, boolean AppMonitor, Context context) {
        runCommand("mount -o rw,remount /system", !isChecked, context);
        if (isChecked) {
            RootUtils.runICommand("mv -f " + "/system/xbin/" + readString("cmiyc", null, context) + " " + xbin_su, context);
            ClearAllNotification(context);
        } else {
            if (!AppMonitor)
                killapp(Constants.PAY, context);
            RootUtils.runCommand("mv -f " + xbin_su + " " + "/system/xbin/" + readString("cmiyc", null, context));
            if (getBoolean("isu_notification", false, context))
                DoNotification(context);
        }
        runCommand("mount -o ro,remount /system", isChecked, context);
        if (!AppMonitor)
            saveLong(SWICH_DELAY, System.currentTimeMillis(), context);
        ChangeSUToast(isChecked, context, (isChecked ? context.getString(R.string.per_app_active) : context.getString(R.string.per_app_deactive)));
        Log.d(TAG, "Change SU isChecked = " + isChecked + " SU path " +
            runCommand(isChecked ? "which su" : "which " + readString("cmiyc", null, context), isChecked, context));
        updateAllWidgetsLayouts(context);
        closeSU();
    }

    public static void ChangeSUToast(boolean isChecked, Context context, String Toast) {
        if (getBoolean("selinux_settings_switch", false, context)) {
            String selinux_su_off = readString("selinux_su_off", null, context);
            String selinux_su_on = readString("selinux_su_on", null, context);
            boolean selinux = isSELinuxActive(context);
            if (isChecked) {
                if (!selinux && selinux_su_on.equals("0")) {
                    SwitchSelinux(true, context);
                    Toast = Toast + "\n" + context.getString(R.string.activate_selinux);
                } else if (selinux && selinux_su_on.equals("1")) {
                    SwitchSelinux(false, context);
                    Toast = Toast + "\n" + context.getString(R.string.deactivate_selinux);
                }
            } else {
                if (!selinux && selinux_su_off.equals("0")) {
                    SwitchSelinux(true, context);
                    Toast = Toast + "\n" + context.getString(R.string.activate_selinux);
                } else if (selinux && selinux_su_off.equals("1")) {
                    SwitchSelinux(false, context);
                    Toast = Toast + "\n" + context.getString(R.string.deactivate_selinux);
                }
            }
        }
        if (getBoolean("anddebug_settings", false, context)) {
            String anddebug_su_off = readString("anddebug_su_off", null, context);
            String anddebug_su_on = readString("anddebug_su_on", null, context);
            boolean anddebug = AndroidDebugState(context);
            if (isChecked) {
                if (!anddebug && anddebug_su_on.equals("1")) {
                    AndroidDebugSet(true, context);
                    Toast = Toast + "\n" + context.getString(R.string.activate_anddebug);
                } else if (anddebug && anddebug_su_on.equals("0")) {
                    AndroidDebugSet(false, context);
                    Toast = Toast + "\n" + context.getString(R.string.deactivate_anddebug);
                }
            } else {
                if (!anddebug && anddebug_su_off.equals("1")) {
                    AndroidDebugSet(true, context);
                    Toast = Toast + "\n" + context.getString(R.string.activate_anddebug);
                } else if (anddebug && anddebug_su_off.equals("0")) {
                    AndroidDebugSet(false, context);
                    Toast = Toast + "\n" + context.getString(R.string.deactivate_anddebug);
                }
            }
        }
        if (!Toast.isEmpty() && getBoolean("toast_notifications", true, context))
            DoAToast("iSu " + Toast + "!", context);
        SendBroadcast("updateControlsReceiver", context);
    }

    public static boolean RebootSupport(String executableFilePath, Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            if (ReadSystemPatch(context))
                return true;
            if (!NewexistFile(executableFilePath + init_superuser, true, context) ||
                !NewexistFile(executableFilePath + init_restart, true, context)) {
                extractAssets(executableFilePath, "superuser", context);
                extractAssets(executableFilePath, "restart", context);
            }
            SystemPatch(executableFilePath, context);
            if (ReadSystemPatch(context))
                return true;
        }
        return false;
    }

    public static void setSummaryColor(Preference preference, String summary, int color, Context context) {
        String final_color = "#" + Integer.toHexString(ContextCompat.getColor(context, color) & 0x00ffffff);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            setSummaryColorN(preference, summary, final_color);
        } else {
            setSummaryColorOlder(preference, summary, final_color);
        }
    }

    @TargetApi(24)
    public static void setSummaryColorN(Preference preference, String summary, String color) {
        preference.setSummary(Html.fromHtml("<font color='" + color + "'>" +
            summary + "</font>", Html.FROM_HTML_MODE_LEGACY));
    }

    @TargetApi(21 | 22 | 23)
    @SuppressWarnings("deprecation")
    public static void setSummaryColorOlder(Preference preference, String summary, String color) {
        preference.setSummary(Html.fromHtml("<font color='" + color + "'>" +
            summary + "</font>"));
    }

    public static void AndroidDebugSet(Boolean isChecked, Context context) {
        if (context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED)
            Settings.Global.putInt(context.getContentResolver(),
                Settings.Global.ADB_ENABLED, isChecked ? 1 : 0);
        Log.d(TAG, "Change ADB isChecked = " + isChecked + " ADB = " + AndroidDebugState(context));
    }

    public static boolean AndroidDebugState(Context context) {
        if (context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED)
            return (Settings.Global.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0) != 0);
        return false;
    }

    public static void WriteSettings(Context context) {
        if (context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != PackageManager.PERMISSION_GRANTED)
            runCommand("pm grant com.bhb27.isu android.permission.WRITE_SECURE_SETTINGS", SuBinary(), context);
    }

    public static void SwitchSelinux(boolean isChecked, Context context) {
        runCommand(Constants.SETENFORCE + (isChecked ? " 1" : " 0"), SuBinary(), context);
        Log.d(TAG, "Change SELinux isChecked = " + isChecked + " State = " + getSELinuxStatus(context));
        updateAllWidgetsLayouts(context);
        closeSU();
    }

    public static boolean SuVersionBool(String suVersion) {
        if ((suVersion.contains("cm-su") || suVersion.contains("mk-su") ||
                suVersion.contains("16 com.android.settings") || suVersion.contains("los-su")) && !suVersion.contains("phh"))
            return true;
        else return false;
    }

    public static String SuVersion(Context context) {
        String su_bin_version = "";
        boolean su = SuBinary();
        // Check if is CM-SU
        if (su) {
            su_bin_version = RootUtils.runCommand("su --version") + "";
        } else if (!su)
            su_bin_version = RootUtils.runICommand(readString("cmiyc", null, context) + " --version", context) + "";
        else
            su_bin_version = RootUtils.runCommand("su --version") + "";

        if (su_bin_version.contains("null"))
            su_bin_version = context.getString(R.string.device_not_root);

        return su_bin_version;
    }

    public static String abi() {
        String abi_version = getprop("ro.product.cpu.abi");
        String abi_result = "";
        if (abi_version.contains("x86")) abi_result = "x86";
        if (abi_version.contains("arm64")) abi_result = "arm64";
        return abi_result;
    }

    public static String[] getallprop(String path, Context context) {
        String GetProps = runCommand("getprop | grep 'ro\\.' | " + path + "busybox  sed 's/\\].*//' | " +
            path + "busybox  sed 's/\\[//'", SuBinary(), context);
        String[] result = GetProps.split("\n");
        Log.d(TAG, "all props = " + result);
        Log.d(TAG, "all props = " + GetProps);
        return result;
    }


    public static String getprop(String prop) {
        return runShell("getprop " + prop);
    }

    public static void resetprop(String path, String prop, String value, Context context, boolean force) {
        boolean su = SuBinary();
        if (value.isEmpty())
            runCommand(path + "resetprop" + abi() + " --delete -n " + prop, su, context);
        else {
            String prop_cmd = prop + " " + value;
            String bp_prop_value = "";
            String bp_prop = "";
            runCommand(path + "resetprop" + abi() + " -v -n " + prop_cmd, su, context);
            bp_prop_value = bp_prop_value + runCommand("cat system/build.prop | grep " + prop +
                " | head -1 | cut -d= -f2", su, context);
            bp_prop = bp_prop + runCommand("cat system/build.prop | grep " + prop + " | head -1 | cut -d= -f1", su, context);
            if (bp_prop.contains(prop) && !bp_prop_value.equals(value))
                overwritebp(prop, bp_prop_value, prop, value, path, context);
            else if (force)
                forcewritebp(prop + "=" + value, context);
            Log.d(TAG, "prop = " + prop + " bp_prop_value = " + " value = " + value);
        }
    }

    public static boolean PropIsinbp(String prop, Context context) {
        String bp_prop = "";
        bp_prop = bp_prop + runCommand("cat system/build.prop | grep " + prop + " | head -1 | cut -d= -f1", SuBinary(), context);
        if (bp_prop.contains(prop))
            return true;
        return false;
    }

    public static void overwritebp(String oldKey, String oldValue, String newKey, String newValue, String path, Context context) {
        boolean su = SuBinary();
        String oldvalue = oldKey + "=" + oldValue;
        String newvalue = newKey + "=" + newValue;
        String command = "old=" + oldvalue + " && " + "new=" + newvalue + " && " + path + "busybox sed -i -r \"s%$old%$new%g\" " + BUILD_PROP;
        runCommand("mount -o rw,remount /system", su, context);
        runCommand(command, su, context);
        runCommand("mount -o ro,remount /system", su, context);
        Log.d(TAG, "overwritebp " + command);
    }

    public static void forcewritebp(String propvalue, Context context) {
        boolean su = SuBinary();
        runCommand("mount -o rw,remount /system", su, context);
        runCommand("echo '\n' >> " + BUILD_PROP, su, context);
        runCommand("echo " + propvalue + " >> " + BUILD_PROP, su, context);
        runCommand("mount -o ro,remount /system", su, context);
        Log.d(TAG, "forcewritebp " + propvalue);
    }

    public static void resetallprop(String path, boolean green, Context context) {
        for (int i = 0; i < props.length; i++) {
            resetprop(path, props[i], (green ? props_OK[i] : props_NOK[i]), context, false);
            Log.d(TAG, "Set " + props[i] + " = " + (green ? props_OK[i] : props_NOK[i]));
        }
    }

    public static void DoNotification(Context context) {

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
        notification.setSmallIcon(R.drawable.ic_notification);
        notification.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        notification.setContentTitle(context.getString(R.string.notification_title));
        notification.setOngoing(true);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        Intent yesReceiver = new Intent();
        yesReceiver.setAction(Constants.YES_ACTION);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 12345, yesReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionyes = new NotificationCompat.Action.Builder(R.drawable.yes, context.getString(R.string.yes), pendingIntentYes).build();
        notification.addAction(actionyes);

        Intent dismissReceiver = new Intent();
        dismissReceiver.setAction(Constants.DISSMISS_ACTION);
        PendingIntent pendingIntentno = PendingIntent.getBroadcast(context, 12345, dismissReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionno = new NotificationCompat.Action.Builder(R.drawable.dismiss, context.getString(R.string.dismiss), pendingIntentno).build();
        notification.addAction(actionno);

        notificationManager.notify(10, notification.build());
    }

    public static void ClearAllNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
    // simple toast function to center the message Main.this
    public static void DoAToast(String message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
        if (view != null) view.setGravity(Gravity.CENTER);
        toast.show();
    }

    public static boolean isSELinuxActive(Context context) {
        if (getSELinuxStatus(context).equals("Enforcing")) return true;
        return false;
    }

    public static String getSELinuxStatus(Context context) {
        String result = "";
        result = runCommand(GETENFORCE, SuBinary(), context);
        if (result != null) {
            if (result.equals("Enforcing")) return "Enforcing";
            else if (result.equals("Permissive")) return "Permissive";
        }
        return "Unknown Status";
    }

    public static void stripsu(String executableFilePath, Context context) {
        boolean su = SuBinary();
        String ro_tochange = "";
        String stripro = "ro.cm.version";
        String stripto = "no.cm.version";
        if (su)
            ro_tochange = ro_tochange + runCommand(executableFilePath + "busybox strings " +
                xbin_su + " | grep " + stripro, su, context);
        else
            ro_tochange = ro_tochange + runCommand(executableFilePath + "busybox strings system/xbin/" + readString("cmiyc", null, context) + " | grep " + stripro, su, context);
        if (ro_tochange.contains(stripro)) {
            runCommand("mount -o rw,remount /system", su, context);
            if (su)
                runCommand(executableFilePath + "busybox sed -i 's/" + stripro +
                    "/" + stripto + "/g' " + xbin_su, su, context);
            else
                runCommand(executableFilePath + "busybox sed -i 's/" + stripro +
                    "/" + stripto + "/g' system/xbin/" + readString("cmiyc", null, context), su, context);
            runCommand("mount -o ro,remount /system", su, context);
            Log.d(TAG, "stripsu ro_tochange = " + ro_tochange);
        } else Log.d(TAG, "not stripsu ro_tochange = " + ro_tochange);
    }

    public static void stripapp(String executableFilePath, String app, String[] strip_old, Context context) {
        boolean su = SuBinary();
        String app_path = "";
        app_path = app_path + runCommand("pm path " + app + "| head -n1 | cut -d: -f2", su, context);
        runCommand("pm install -r " + app_path, su, context);
        for (int i = 0; i < strip_old.length; i++)
            sedstring(executableFilePath, app, String.valueOf(strip_old[i]), context);
    }

    public static void sedstring(String executableFilePath, String app, String strip_old, Context context) {
        boolean su = SuBinary();
        String odex = "";
        String strip_new;
        String app_path = "";

        char[] strip_char = strip_old.toCharArray();
        strip_char[0] = ChangeLetter(strip_char[0]);
        strip_char[strip_char.length - 1] = ChangeLetter(strip_char[strip_char.length - 1]);
        strip_new = String.valueOf(strip_char);

        app_path = "" + runCommand("pm path " + app + "| head -n1 | cut -d: -f2", su, context);
        app_path = app_path.substring(0, app_path.length() - 8) + "oat/*/base.odex";
        runCommand(executableFilePath + "busybox sed -i -r 's/" + strip_old + "/" + strip_new + "/g' " + app_path, su, context);
        Log.d(TAG, " sedstring app " + app_path);
    }

    public static char ChangeLetter(char letter) {
        char[] randon_char = ("abcdefghijklmnopqrstuvw‌​xyz").toCharArray();
        char mod_string;
        while (true) {
            mod_string = randon_char[new Random().nextInt(randon_char.length)];
            if (!String.valueOf(mod_string).equals(String.valueOf(letter)))
                return mod_string;
        }
    }

    public static String random4() {
        char[] randon_char = ("abcdefghijklmnopqrtvw‌​xyz").toCharArray();
        String mod_string;
        while (true) {
            mod_string = "";
            for (int i = 0; i < 5; i++) {
                mod_string = mod_string + String.valueOf(randon_char[new Random().nextInt(randon_char.length)]);
            }
            if (runShell("which " + mod_string).isEmpty())
                break;
        }
        return mod_string;
    }

    public static boolean SuBinary() {
        return runShell("which su").contains("/su");
    }

    public static String runShell(String command) {
        try {
            StringBuffer output = new StringBuffer();
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
            reader.close();
            p.waitFor();
            return output.toString().trim();
        } catch (InterruptedException | IOException e) {
            Log.d(TAG, "catch exception runShell");
        }
        return "";
    }

    public static boolean getBoolean(String name, boolean defaults, Context context) {
        try {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(name, defaults);
        } catch (Exception ignored) {
            return false;
        }
    }

    public static void saveBoolean(String name, boolean value, Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(name, value).apply();
    }

    public static String readString(String name, String defaults, Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(name, defaults);
    }

    public static void saveString(String name, String value, Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(name, value).apply();
    }
    
     public static long getLong(String name, long defaults, Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getLong(name, defaults);
    }

    public static void saveLong(String name, long value, Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putLong(name, value).apply();
    }

    public static void saveprop(Context context) {
        String value;
        for (int i = 0; i < props.length; i++) {
            value = getprop(props[i]);
            if (value != null && !value.isEmpty())
                saveString(props[i], getprop(props[i]), context);
        }
        saveBoolean("prop_run", true, context);
    }

    public static void applyprop(Context context, String path) {
        String newvalue = "", originalvalue;
        for (int i = 0; i < props.length; i++) {
            newvalue = readString(props[i], null, context);
            if (newvalue != null && !newvalue.isEmpty()) {
                resetprop(path, props[i], newvalue, context, false);
            }
            Log.d(TAG, "ap Set " + props[i] + " = " + newvalue);
        }
    }

    public static void applyDbProp(Context context, String path) {
        PropDB propDB = new PropDB(context);
        List < PropDB.PerAppItem > PropItem = propDB.getAllProps();
        final String[] props = new String[PropItem.size()];
        for (int i = 0; i < PropItem.size(); i++) {
            String prop = PropItem.get(i).getApp();
            String value = PropItem.get(i).getID();
            if (prop != null && value != null)
                resetprop(path, prop, value, context, false);
            Log.d(TAG, "ap DB Set " + prop + " = " + value);
        }
    }

    public static String StringreadFileN(String file) {
        return readFileN(file, true);
    }

    public static String readFileN(String file, boolean asRoot) {

        StringBuilder s = null;
        FileReader fileReader = null;
        BufferedReader buf = null;
        try {
            fileReader = new FileReader(file);
            buf = new BufferedReader(fileReader);

            String line;
            s = new StringBuilder();
            while ((line = buf.readLine()) != null) s.append(line).append("\n");
        } catch (FileNotFoundException ignored) {
            Log.e(TAG, "File does not exist " + file);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read " + file);
        } finally {
            try {
                if (fileReader != null) fileReader.close();
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return s == null ? null : s.toString().trim();
    }

    /**
     * as Root
     */
    public static String StringreadFile(String file, Context context) {
        return readFile(file, true, context);
    }

    public static boolean NewexistFile(String file, boolean asRoot, Context context) {
        if (existFile(file, asRoot))
            return true;
        else if (IexistFile(file, asRoot, context))
            return true;
        return false;
    }

    public static boolean existFile(String file, boolean asRoot) {
        if (asRoot) return new RootFile(file).exists();
        return new File(file).exists();
    }

    public static boolean IexistFile(String file, boolean asRoot, Context context) {
        if (asRoot) return new RootFile(file).Iexists(context);
        return new File(file).exists();
    }

    public static boolean compareFiles(String file, String file2, boolean asRoot, Context context) {
        Log.i("Kernel adiutor", "compareFiles " + file + " size is " + new RootFile(file).length(context) + " and " +
            file2 + " size is " + new RootFile(file2).length(context));
        if (asRoot) return new RootFile(file).length(context) == new RootFile(file2).length(context);
        return new File(file).length() == new RootFile(file2).length(context);
    }

    public static String readFile(String file, boolean asRoot, Context context) {
        if (asRoot) {
            if (SuBinary())
                return new RootFile(file).readFile();
            else
                return new RootFile(file).IreadFile(context);
        }

        StringBuilder s = null;
        FileReader fileReader = null;
        BufferedReader buf = null;
        try {
            fileReader = new FileReader(file);
            buf = new BufferedReader(fileReader);

            String line;
            s = new StringBuilder();
            while ((line = buf.readLine()) != null) s.append(line).append("\n");
        } catch (FileNotFoundException ignored) {
            Log.e(TAG, "File does not exist " + file);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read " + file);
        } finally {
            try {
                if (fileReader != null) fileReader.close();
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return s == null ? null : s.toString().trim();
    }

    /**
     * Write a string to any file
     *
     * @param path   path to file
     * @param text   your text
     * @param append append your text to file
     * @param asRoot write as root
     */
    public static void writeFile(String path, String text, boolean append, boolean asRoot, Context context) {
        if (asRoot) {
            if (SuBinary())
                new RootFile(path).write(text, append);
            else
                new RootFile(path).Iwrite(text, append, context);
            return;
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(path, append);
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, "Failed to write " + path);
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
