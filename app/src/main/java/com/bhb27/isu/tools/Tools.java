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
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Random;

import com.bhb27.isu.R;
import com.bhb27.isu.tools.RootFile;
import com.bhb27.isu.tools.RootUtils;
import com.bhb27.isu.widgetservice.Widgeth;
import com.bhb27.isu.widgetservice.Widgetv;
import com.bhb27.isu.widgetservice.Widgetsu;

public class Tools implements Constants {

    public static boolean KernelSupport() {
        String kernel_support_rc, kernel_support_sh;
        if (existFile(xbin_su, true)) {
            kernel_support_rc = RootUtils.runCommand("grep -r -i isu_daemon *.rc ") + "";
            kernel_support_sh = RootUtils.runCommand("grep -r -i isu_daemon *.sh ") + "" +
                RootUtils.runCommand("grep -r -i /system/xbin/isu /sbin/*.sh ");
            if (kernel_support_rc.contains("isu_daemon") && kernel_support_sh.contains("/system/xbin/isu")) {
                return true;
            } else
                return false;
        } else {
            kernel_support_rc = RootUtils.runICommand("grep -r -i isu_daemon *.rc ") + "";
            kernel_support_sh = RootUtils.runICommand("grep -r -i isu_daemon *.sh ") + "" +
                RootUtils.runICommand("grep -r -i /system/xbin/isu /sbin/*.sh ");
            if (kernel_support_rc.contains("isu_daemon") && kernel_support_sh.contains("/system/xbin/isu")) {
                return true;
            } else
                return false;
        }
    }

    public static boolean ReadSystemPatch() {
        String reboot_support_rc, reboot_support_sh;
        if (SuBinary(xbin_su)) {
            reboot_support_rc = RootUtils.runCommand("grep -i isu_daemon system/etc/init/superuser.rc") + "";
            reboot_support_sh = RootUtils.runCommand("grep -i /system/xbin/isu system/xbin/isush") + "";
            if (reboot_support_rc.contains("isu_daemon") && reboot_support_sh.contains("/system/xbin/isu"))
                return true;
        } else if (SuBinary(xbin_isu)) {
            reboot_support_rc = RootUtils.runICommand("grep -i isu_daemon system/etc/init/superuser.rc") + "";
            reboot_support_sh = RootUtils.runICommand("grep -i /system/xbin/isu system/xbin/isush") + "";
            if (reboot_support_rc.contains("isu_daemon") && reboot_support_sh.contains("/system/xbin/isu"))
                return true;
        }
        return false;
    }

    public static void SystemPatch(String executableFilePath) {
        if (SuBinary(xbin_su)) {
            RootUtils.runCommand("mount -o rw,remount /system");
            RootUtils.runCommand("cp -f " + executableFilePath + "isush" + " /system/xbin/");
            RootUtils.runCommand("chmod 0755" + " /system/xbin/isush");
            RootUtils.runCommand("cp -f " + executableFilePath + "superuser.rc" + " /system/etc/init/");
            RootUtils.runCommand("chmod 0644" + " /system/etc/init/superuser.rc");
            RootUtils.runCommand("mount -o ro,remount /system");
        } else if (SuBinary(xbin_isu)) {
            RootUtils.runICommand("mount -o rw,remount /system");
            RootUtils.runICommand("cp -f " + executableFilePath + "isush" + " /system/xbin/");
            RootUtils.runICommand("chmod 0755" + " /system/xbin/isush");
            RootUtils.runICommand("cp -f " + executableFilePath + "superuser.rc" + " /system/etc/init/");
            RootUtils.runICommand("chmod 0644" + " /system/etc/init/superuser.rc");
            RootUtils.runICommand("mount -o ro,remount /system");
        }
    }

    public static void PatchSepolicy(String executableFilePath) {
        if (SuBinary(xbin_su))
            RootUtils.runCommand("LD_LIBRARY_PATH=" + executableFilePath + " " + executableFilePath + sepolicy);
        else if (SuBinary(xbin_isu))
            RootUtils.runICommand("LD_LIBRARY_PATH=" + executableFilePath + " " + executableFilePath + sepolicy);
    }

    public static void updateAllWidgets(boolean SU_SEL, final Context context,
        final int layoutResourceId,
        final Class < ? extends AppWidgetProvider > appWidgetClass) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutResourceId);
        remoteViews.setTextViewText(R.id.iSuMain, "SU" + "\n" + (SuBinary(Constants.xbin_su) ?
            context.getString(R.string.activated) : context.getString(R.string.deactivated)));
        if (SuBinary(Constants.xbin_su))
            remoteViews.setInt(R.id.iSuMain, "setBackgroundResource", R.drawable.button);
        else if (SuBinary(Constants.xbin_isu))
            remoteViews.setInt(R.id.iSuMain, "setBackgroundResource", R.drawable.buttong);
        if (SU_SEL) {
            remoteViews.setTextViewText(R.id.iSuMonitor, "SELinux" + "\n" + Tools.getSELinuxStatus());
            if (isSELinuxActive())
                remoteViews.setInt(R.id.iSuMonitor, "setBackgroundResource", R.drawable.buttong);
            else
                remoteViews.setInt(R.id.iSuMonitor, "setBackgroundResource", R.drawable.button);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, appWidgetClass));
        appWidgetManager.partiallyUpdateAppWidget(appWidgetIds, remoteViews);
    }

    public static void UpMain(Context context) {
            final Intent MainIntent = new Intent();
            MainIntent.setAction("updateMainReceiver");
            context.sendBroadcast(MainIntent);
    }

    public static void SwitchSu(boolean isChecked, boolean AppMonitor, Context context) {
        if (isChecked) {
            // Mount rw to change mount ro after
            RootUtils.runICommand("mount -o rw,remount /system");
            RootUtils.runICommand("mv " + xbin_isu + " " + xbin_su);
            RootUtils.runCommand("mv " + bin_temp_su + " " + bin_su);
            RootUtils.runCommand("mount -o ro,remount /system");
            ActiveSUToast(context);
            ClearAllNotification(context);
        } else {
            if (!AppMonitor) {
                String androidPay = RootUtils.runCommand("ps | grep " + Constants.PAY);
                if (androidPay.contains(Constants.PAY))
                    RootUtils.runCommand("am force-stop " + Constants.PAY);
            }
            // Make a link to isu so all root tool work
            RootUtils.runCommand("mount -o rw,remount /system");
            RootUtils.runCommand("ln -s -f " + xbin_isu + " " + bin_isu);
            RootUtils.runCommand("mv " + xbin_su + " " + xbin_isu);
            RootUtils.runICommand("mv " + bin_su + " " + bin_temp_su);
            RootUtils.runICommand("mount -o ro,remount /system");
            if (getBoolean("isu_notification", false, context))
                DoNotification(context);
            String Toast = context.getString(R.string.per_app_deactive);
            if (!isSELinuxActive()) {
                SwitchSelinux(true, context);
                Toast = Toast + "\n" + context.getString(R.string.activate_selinux);
            }
            if (getBoolean("adb_change", false, context) && AndroidDebugState(context)) {
                AndroidDebugSet(isChecked, context);
                Toast = Toast + "\n" + context.getString(R.string.deactivate_anddebug);
            }
            Tools.DoAToast("iSu " + Toast + "!", context);
        }
        updateAllWidgets(true, context, R.layout.widget_layouth, Widgeth.class);
        updateAllWidgets(true, context, R.layout.widget_layoutv, Widgetv.class);
        updateAllWidgets(false, context, R.layout.widget_layoutsu, Widgetsu.class);
    }

    public static void ActiveSUToast(Context context) {
        String Toast = context.getString(R.string.per_app_active);
        boolean restart_selinux = getBoolean("restart_selinux", false, context);
        boolean selinux = isSELinuxActive();
        if (restart_selinux && !selinux) {
            SwitchSelinux(true, context);
            Toast = Toast + "\n" + context.getString(R.string.activate_selinux);
        } else if (!restart_selinux && selinux) {
            SwitchSelinux(false, context);
            Toast = Toast + "\n" + context.getString(R.string.deactivate_selinux);
        }
        if (getBoolean("adb_change", false, context) && SuBinary(xbin_su) && !AndroidDebugState(context)) {
            AndroidDebugSet(SuBinary(xbin_su), context);
            Toast = Toast + "\n" + context.getString(R.string.activate_anddebug);
        }
        DoAToast("iSu " + Toast + "!", context);
    }

    public static void WriteSettings(Context context) {
        if (context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            if (SuBinary(xbin_su))
                RootUtils.runCommand("pm grant com.bhb27.isu android.permission.WRITE_SECURE_SETTINGS");
            else if (SuBinary(xbin_isu))
                RootUtils.runICommand("pm grant com.bhb27.isu android.permission.WRITE_SECURE_SETTINGS");
        }
    }

    public static void AndroidDebugSet(Boolean isChecked, Context context) {
        if (context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == 0)
           Settings.Global.putInt(context.getContentResolver(),
                Settings.Global.ADB_ENABLED, isChecked ? 1 : 0);
    }

    public static boolean AndroidDebugState(Context context) {
        if (context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == 0)
           return (Settings.Global.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0) != 0);
        return false;
    }

    public static void SwitchSelinux(boolean isChecked, Context context) {
        if (SuBinary(xbin_su))
            RootUtils.runCommand(Constants.SETENFORCE + (isChecked ? " 1" : " 0"));
        else if (SuBinary(xbin_isu))
            RootUtils.runCommand(Constants.SETENFORCE + (isChecked ? " 1" : " 0"));
        updateAllWidgets(true, context, R.layout.widget_layouth, Widgeth.class);
        updateAllWidgets(true, context, R.layout.widget_layoutv, Widgetv.class);
        updateAllWidgets(false, context, R.layout.widget_layoutsu, Widgetsu.class);
    }

    public static boolean SuVersionBool(String suVersion) {
        if (suVersion.contains("cm-su") || suVersion.contains("mk-su") ||
            suVersion.contains("16 com.android.settings"))
            return true;
        else return false;
    }

    public static String SuVersion(Context context) {
        String su_bin_version = "";
        // Check if is CM-SU
        if (SuBinary(xbin_su)) {
            su_bin_version = RootUtils.runCommand("su --version") + "";
        } else if (SuBinary(xbin_isu))
            su_bin_version = RootUtils.runICommand("isu --version") + "";
        else
            su_bin_version = RootUtils.runCommand("su --version") + "";

        if (su_bin_version.contains("null"))
            su_bin_version = context.getString(R.string.device_not_root);

        return su_bin_version;
    }

    public static void DoNotification(Context context) {

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
        notification.setSmallIcon(R.drawable.ic_notification);
        notification.setContentTitle(context.getString(R.string.notification_title));
        notification.setOngoing(true);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        Intent yesReceiver = new Intent();
        yesReceiver.setAction(Constants.YES_ACTION);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 12345, yesReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actiony = new NotificationCompat.Action.Builder(R.drawable.ic_notification, context.getString(R.string.yes), pendingIntentYes).build();
        notification.addAction(actiony);

        Intent dismissReceiver = new Intent();
        dismissReceiver.setAction(Constants.DISSMISS_ACTION);
        PendingIntent pendingIntentYes2 = PendingIntent.getBroadcast(context, 12345, dismissReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionn = new NotificationCompat.Action.Builder(R.drawable.ic_notification, context.getString(R.string.dismiss), pendingIntentYes2).build();
        notification.addAction(actionn);

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

    // Random int generated base on a String[] length to get a Random String
    public static String RandomString(Context context) {
        String[] pokemonstrings = new String[] {
            context.getString(R.string.pokemongo_1), context.getString(R.string.pokemongo_2), context.getString(R.string.pokemongo_3),
                context.getString(R.string.pokemongo_4), context.getString(R.string.pokemongo_5), context.getString(R.string.pokemongo_6),
                context.getString(R.string.pokemongo_7), context.getString(R.string.pokemongo_8), context.getString(R.string.pokemongo_9),
                context.getString(R.string.isu_by), context.getString(R.string.pokemongo_start)
        };
        Random rand = new Random();
        int generate = 0;
        generate = rand.nextInt(pokemonstrings.length);
        return pokemonstrings[generate];
    }

    public static boolean isSELinuxActive() {
        if (getSELinuxStatus().equals("Enforcing")) return true;
        return false;
    }

    public static void RestartApp(String packageName) {
        RootUtils.runCommand("am force-stop " + packageName);
        RootUtils.runCommand("am start " + packageName);
    }

    public static String getSELinuxStatus() {
        String result = "";
        if (existFile(xbin_su, true))
            result = RootUtils.runCommand(GETENFORCE);
        else
            result = RootUtils.runICommand(GETENFORCE);
        if (result != null) {
            if (result.equals("Enforcing")) return "Enforcing";
            else if (result.equals("Permissive")) return "Permissive";
        }
        return "Unknown Status";
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
    public static String StringreadFile(String file) {
        return readFile(file, true);
    }

    public static boolean NewexistFile(String file, boolean asRoot) {
        if (existFile(file, asRoot))
            return true;
        else if (IexistFile(file, asRoot))
            return true;
        return false;
    }

    public static boolean existFile(String file, boolean asRoot) {
        if (asRoot) return new RootFile(file).exists();
        return new File(file).exists();
    }

    public static boolean IexistFile(String file, boolean asRoot) {
        if (asRoot) return new RootFile(file).Iexists();
        return new File(file).exists();
    }

    public static String readFile(String file, boolean asRoot) {
        if (asRoot) {
            if (SuBinary(xbin_su))
                return new RootFile(file).readFile();
            else if (SuBinary(xbin_isu))
                return new RootFile(file).IreadFile();
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

    public static boolean SuBinary(String binary) {
        if (binary.equals(xbin_su)) {
            if (existFile(binary, true))
                return true;
        }
        if (binary.equals(xbin_isu)) {
            if (IexistFile(binary, true))
                return true;
        }
        return false;
    }

    /**
     * Write a string to any file
     *
     * @param path   path to file
     * @param text   your text
     * @param append append your text to file
     * @param asRoot write as root
     */
    public static void writeFile(String path, String text, boolean append, boolean asRoot) {
        if (asRoot) {
            if (SuBinary(xbin_su))
                new RootFile(path).write(text, append);
            else if (SuBinary(xbin_isu))
                new RootFile(path).Iwrite(text, append);
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
