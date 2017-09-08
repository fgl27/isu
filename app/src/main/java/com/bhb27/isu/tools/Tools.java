/*
 * Copyright (C) 2016-2017 Felipe de Leon <fglfgl27@gmail.com>
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
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
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
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.bhb27.isu.BuildConfig;
import com.bhb27.isu.Start;
import com.bhb27.isu.StartMasked;
import com.bhb27.isu.R;
import com.bhb27.isu.perapp.PerAppMonitor;
import com.bhb27.isu.perapp.Per_App;
import com.bhb27.isu.perapp.PropDB;
import com.bhb27.isu.tools.RootFile;
import com.bhb27.isu.tools.ZipUtils;
import com.bhb27.isu.widgetservice.Widgeth;
import com.bhb27.isu.widgetservice.Widgetv;
import com.bhb27.isu.widgetservice.Widgetsu;

import com.afollestad.materialdialogs.MaterialDialog;
import org.zeroturnaround.zip.ZipUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    public static void BPBackup(Context context) {
        boolean su = SuBinary();
        if (!NewexistFile("/system/build.prop.isu_backup", true, context)) {
            runCommand("mount -o rw,remount /system", su, context);
            runCommand("cp -f /system/build.prop /system/build.prop.isu_backup", su, context);
            runCommand("mount -o ro,remount /system", su, context);
        }
    }

    public static void updateMain(Context context, String toast) {
        closeSU();
        DoAToast(toast, context);
        SendBroadcast("updateMainReceiver", context);
        context.startActivity(new Intent(context, Start.class));
    }

    public static void closeSU() {
        RootUtils.closeSU();
        RootUtils.closeISU();
    }

    public static boolean appId(Context context) {
        return (BuildConfig.APPLICATION_ID).equals(context.getPackageName());
    }

    public static void HideiSu(Context context) {
        boolean su = SuBinary();
        runCommand("pm unhide " + BuildConfig.APPLICATION_ID, su, context);
        runCommand("am start -n " + BuildConfig.APPLICATION_ID + "/" + BuildConfig.APPLICATION_ID + ".Start", su, context);
        runCommand("pm uninstall " + context.getPackageName(), SuBinary(), context);
    }

    public static void SimpleHideDialog(String message, Context context) {
        new AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setCancelable(false)
            .setMessage(message)
            .setNegativeButton(context.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean su = SuBinary();
                        runCommand("am start -n " + readString("hide_app_name", null, context) + "/" + BuildConfig.APPLICATION_ID + ".StartMasked", su, context);
                        runCommand("pm hide " + BuildConfig.APPLICATION_ID, su, context);
                        return;
                    }
                }).show();
    }

    public static void HideDialog(Context context) {
        new AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setCancelable(false)
            .setTitle(context.getString(R.string.hide_title))
            .setMessage(context.getString(R.string.hide_summary) + context.getString(R.string.hide_isu))
            .setNeutralButton(context.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveInt("hide_app_count", 1, context);
                        context.startActivity(new Intent(context, StartMasked.class));
                    }
                })
            .setPositiveButton(context.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                }).show();
    }

    public static void UnHideDialog(Context context) {
        new AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setTitle(context.getString(R.string.unhide_title))
            .setMessage(context.getString(R.string.unhide_summary))
            .setNeutralButton(context.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Tools.HideiSu(context);
                    }
                })
            .setPositiveButton(context.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                }).show();
    }

    public static void UpHideDialog(Context context) {
        new AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setTitle(context.getString(R.string.need_update_title))
            .setMessage(context.getString(R.string.need_update_summary))
            .setNeutralButton(context.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Tools.UpHideiSu(context);
                    }
                })
            .setPositiveButton(context.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                }).show();
    }

    public static void UpHideiSu(Context context) {
        boolean su = SuBinary();
        runCommand("pm unhide " + BuildConfig.APPLICATION_ID, su, context);
        runCommand("am start -n " + BuildConfig.APPLICATION_ID + "/" + BuildConfig.APPLICATION_ID + ".StartMasked", su, context);
    }

    public static boolean isuInstaled(Context context) {
        boolean su = SuBinary();
        String app_folder = "" + runCommand("pm path " + context.getPackageName() + " | head -n1 | cut -d: -f2", su, context);
        String[] OriginaliSuApk = app_folder.split("com");
        return NewexistFile(OriginaliSuApk[0] + "com.bhb27.isu*/base.apk", true, context);
    }

    public static class HideTask extends AsyncTask < Void, Void, String > {
        private MaterialDialog progressDialog;
        private WeakReference < Context > contextRef;
        private boolean su = SuBinary();
        public HideTask(Context context) {
            contextRef = new WeakReference < > (context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Context mContext = contextRef.get();
            progressDialog = new MaterialDialog.Builder(mContext)
                .title(mContext.getString(R.string.hide_title))
                .content(String.format(mContext.getString(R.string.hide_generating), getInt("hide_app_count", 1, mContext)))
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .show();
        }

        @Override
        protected String doInBackground(Void...params) {
            Context mContext = contextRef.get();
            Tools.SwitchSu(true, false, mContext);
            boolean su = SuBinary();
            String hide_app = "hide.apk";
            File hideAPK = new File(mContext.getCacheDir(), hide_app);
            String pkg = ZipUtils.generateUnhide(mContext, hideAPK).replace("\0", "");
            runCommand("pm install -r /" + mContext.getCacheDir() + "/" + hide_app, su, mContext);
            saveString("hide_app_name", pkg, mContext);
            int tryes = getInt("hide_app_count", 1, mContext);
            saveInt("hide_app_count", (tryes + 1), mContext);
            return pkg;
        }

        @Override
        protected void onPostExecute(String pkg) {
            super.onPostExecute(pkg);
            Context mContext = contextRef.get();
            String app_instaled = ("" + runCommand("pm list packages | grep " + pkg + " | cut -d: -f2", su, mContext));
            progressDialog.dismiss();

            if (app_instaled.contains(pkg)) {
                saveInt("hide_app_count", 1, mContext);
                SimpleHideDialog(String.format(mContext.getString(R.string.hide_success), pkg), mContext);
            } else if (getInt("hide_app_count", 1, mContext) <= 3) new HideTask(mContext).execute();
            else SimpleDialogFail(mContext.getString(R.string.hide_fail), mContext);
        }
    }

    public static boolean appInstaled(Context context) {
        String pkg = readString("hide_app_name", "not", context);
        String app_instaled = ("" + runCommand("pm list packages | grep " + pkg + " | cut -d: -f2", SuBinary(), context));
        Log.d(TAG, "appInstaled pkg " + pkg + " app_instaled " + app_instaled + " state " + app_instaled.contains(pkg));
        if (app_instaled.contains(pkg)) return true;
        return false;
    }

    public static boolean NeedUpdate(Context context) {
        if (isuInstaled(context)) {
            boolean su = SuBinary();
            String sdcard = Environment.getExternalStorageDirectory().getPath();
            String temp_app = sdcard + "/temp.apk";
            String app_folder = "" + runCommand("pm path " + context.getPackageName() + " | head -n1 | cut -d: -f2", su, context);
            String[] OriginaliSuApk = app_folder.split("com");
            runCommand("cp -f " + OriginaliSuApk[0] + "com.bhb27.isu*/base.apk /" + temp_app, su, context);
            double this_versionApp = Float.valueOf(BuildConfig.VERSION_NAME);
            double versionApp = 0;
            if (NewexistFile(temp_app, true, context)) {
                try {
                    PackageManager pm = context.getPackageManager();
                    PackageInfo info = pm.getPackageArchiveInfo(temp_app, 0);
                    versionApp = Float.valueOf(info.versionName);
                } catch (NullPointerException ignored) {}
            } else
                versionApp = Float.valueOf(BuildConfig.VERSION_NAME);
            runCommand("rm -rf /" + temp_app, su, context);
            if (versionApp > this_versionApp) return true;
            if (versionApp <= this_versionApp) return false;
        }
        return false;
    }

    public static void SimpleDialogFail(String message, Context context) {
        new AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setMessage(message)
            .setNegativeButton(context.getString(R.string.dismiss),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.startActivity(new Intent(context, Start.class));
                        return;
                    }
                }).show();
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

    public static boolean PatchesDone(Context context) {
        if (NewexistFile("/psoko", true, context)) return true;
        boolean su = SuBinary();
        runCommand("mount -o rw,remount /", su, context);
        runCommand("echo OK > /psoko", su, context);
        runCommand("mount -o ro,remount /", su, context);
        return false;
    }

    public static void patches(String executableFilePath, Context context) {
        PatchSepolicy(executableFilePath, context);
        extractBusybox(executableFilePath, context);
        WriteSettings(context);
        BPBackup(context);
        blankJson(context);
        if (SuVersionBool(SuVersion(context))) {
            if (NewexistFile(bin_su, true, context))
                delbinsu(context);

            subackup(executableFilePath, context);
        }
    }

    public static void extractBusybox(String executableFilePath, Context context) {
        if (!Tools.NewexistFile(executableFilePath + "busybox", true, context)) {
            Tools.extractAssets(executableFilePath, "busybox" + Tools.abiX(), context);
            Tools.runCommand("mv -f " + executableFilePath + "busybox" + Tools.abiX() + " " + executableFilePath + "busybox", Tools.SuBinary(), context);
        }
    }

    public static void blankJson(Context context) {
        // Create a blank profiles.json to prevent logspam.
        String filesDir = context.getFilesDir().getPath();
        File file = new File(filesDir + "/per_app.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        file = new File(filesDir + "/prop.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void SystemPatch(String executableFilePath, Context context) {
        String seclabel = "";
        boolean su = SuBinary();
        seclabel = runCommand("cat system/etc/init/superuser.rc | grep seclabel | head -1", su, context);
        runCommand("mount -o rw,remount /system", su, context);
        runCommand("chmod 755" + executableFilePath + "restart", su, context);
        runCommand("cp -f " + executableFilePath + init_superuser + " /system/etc/init/superuser.rc", su, context);
        runCommand(executableFilePath + "busybox" + " sed -i '/seclabel/c\\    " + seclabel + "' system/etc/init/superuser.rc ", su, context);
        //            RootUtils.runCommand(executableFilePath + "busybox" + " sed -i 's/YYYY\\b/" + readString("cmiyc", null, context) + "/g' system/etc/init/superuser.rc ");
        runCommand("chmod 644" + " /system/etc/init/superuser.rc", su, context);
        if (NewexistFile("/system/xbin/isush", true, context))
            runCommand("rm -rf /system/xbin/isush", su, context);
        runCommand("mount -o ro,remount /system", su, context);
    }

    public static void PatchSepolicy(String executableFilePath, Context context) {
        boolean su = SuBinary();
        if (!NewexistFile(executableFilePath + "magisk", true, context)) {
            extractAssets(executableFilePath, "magisk" + abi(), context);
            runCommand("mv -f " + executableFilePath + "magisk" + abi() + " " + executableFilePath + "magisk", su, context);
        }
        runCommand("mount -o rw,remount /", su, context);
        for (int i = 0; i < MagiskPolicy.length; i++)
            Log.d(TAG, "PS " + i + " " + runCommand(executableFilePath + "magisk magiskpolicy " + MagiskPolicy[i], su, context));
        runCommand("mount -o ro,remount /", su, context);
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
            Log.d(TAG, "Failed to copy asset file: " + filename);
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
        Log.d(TAG, "backup_restart = " + runCommand(executableFilePath + "busybox" + " ls -l /system/xbin/restart", su, context));
        Log.d(TAG, "backup_isu = " + runCommand(executableFilePath + "busybox" + " ls -l /data/backup_isu", su, context));
        runCommand("mount -o ro,remount /system", su, context);
    }

    public static boolean rootAccess(Context context) {
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
        String UID = runCommand(executableFilePath + "busybox" + " ps | grep " +
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
                killapp(PAY, context);
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
                boolean su = SuBinary();
                runCommand("rm -rf " + executableFilePath + "superuser*", su, context);
                runCommand("rm -rf " + executableFilePath + "restart*", su, context);
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
        return (Settings.Global.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0) != 0);
    }

    public static boolean AndroidDebugRoot() {
        return getprop("service.adb.root").contains("1");
    }

    public static void SetAndroidDebugRoot(boolean value, Context context) {
        resetprop(context.getFilesDir().getPath() + "/", "service.adb.root", (value ? "1" : "0"), context, false);
    }

    public static void WriteSettings(Context context) {
        if (context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != PackageManager.PERMISSION_GRANTED)
            runCommand("pm grant " + context.getPackageName() + " android.permission.WRITE_SECURE_SETTINGS", SuBinary(), context);
    }

    public static void FakeSelinux(Context context) {
        String executableFilePath = context.getFilesDir().getPath() + "/";
        boolean su = SuBinary();
        runCommand("mount -o rw,remount /", su, context);
        runCommand(executableFilePath + "magisk magiskpolicy --live \"permissive *\"", su, context);
        runCommand("mount -o ro,remount /", su, context);
        closeSU();
    }

    public static void SwitchSelinux(boolean isChecked, Context context) {
        runCommand(SETENFORCE + (isChecked ? " 1" : " 0"), SuBinary(), context);
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
        String abi_version_2 = getprop("ro.product.cpu.abi2");
        String abi_version_l = getprop("ro.product.cpu.abi");
        String abi_result = ""; //arm default
        if (abi_version.contains("x86") || abi_version_2.contains("x86")) abi_result = "x86";
        else if (abi_version.contains("arm64") || abi_version_l.contains("arm64")) abi_result = "arm64";
        else if (abi_version.contains("x86_64") || abi_version_l.contains("x86_64")) abi_result = "x64";
        return abi_result;
    }

    public static String abiX() {
        String abi_result = "";
        if (getprop("ro.product.cpu.abi").contains("x86")) abi_result = "x86";
        return abi_result;
    }

    public static String[] getallprop(String path, Context context) {
        String GetProps = runCommand("getprop | grep 'ro\\.' | " + path + "busybox" + " sed 's/\\].*//' | " +
            path + "busybox" + " sed 's/\\[//'", SuBinary(), context);
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
            runCommand(path + "magisk resetprop --delete -n " + prop, su, context);
        else {
            String prop_cmd = prop + " " + value;
            String bp_prop_value = "";
            String bp_prop = "";
            runCommand(path + "magisk resetprop -v -n " + prop_cmd, su, context);
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
        String command = "old=" + oldvalue + " && " + "new=" + newvalue + " && " + path + "busybox" + " sed -i -r \"s%$old%$new%g\" " + BUILD_PROP;
        runCommand("mount -o rw,remount /system", su, context);
        runCommand(command, su, context);
        runCommand("mount -o ro,remount /system", su, context);
        Log.d(TAG, "overwritebp " + "old = " + oldvalue + " new = " + newvalue);
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

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "iSu_SU_state");
        notification.setSmallIcon(R.drawable.ic_notification);
        notification.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        notification.setContentTitle(context.getString(R.string.notification_title));
        notification.setOngoing(true);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        Intent yesReceiver = new Intent();
        yesReceiver.setAction(YES_ACTION);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 12345, yesReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionyes = new NotificationCompat.Action.Builder(R.drawable.yes, context.getString(R.string.yes), pendingIntentYes).build();
        notification.addAction(actionyes);

        Intent dismissReceiver = new Intent();
        dismissReceiver.setAction(DISSMISS_ACTION);
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

    public static boolean isSELinuxActiveNoROOT() {
        if (getSELinuxStatusNoRoot().equals("Enforcing")) return true;
        return false;
    }

    public static String getSELinuxStatusNoRoot() {
        String result = "";
        result = runShell(GETENFORCE);
        if (result != null) {
            if (result.equals("Enforcing")) return "Enforcing";
            else if (result.equals("Permissive")) return "Permissive";
        }
        return "Unknown Status";
    }

    public static void stripsu(String executableFilePath, Context context) {
        boolean su = SuBinary();
        String ro_tochange = "";
        String stripro = "ro.debuggable";
        String stripto = "no.debuggable";
        if (su)
            ro_tochange = ro_tochange + runCommand(executableFilePath + "busybox" + " strings " +
                xbin_su + " | grep " + stripro, su, context);
        else
            ro_tochange = ro_tochange + runCommand(executableFilePath + "busybox" + " strings system/xbin/" + readString("cmiyc", null, context) + " | grep " + stripro, su, context);
        if (ro_tochange.contains(stripro)) {
            runCommand("mount -o rw,remount /system", su, context);
            if (su)
                runCommand(executableFilePath + "busybox" + " sed -i 's/" + stripro +
                    "/" + stripto + "/g' " + xbin_su, su, context);
            else
                runCommand(executableFilePath + "busybox" + " sed -i 's/" + stripro +
                    "/" + stripto + "/g' system/xbin/" + readString("cmiyc", null, context), su, context);
            runCommand("mount -o ro,remount /system", su, context);
            Log.d(TAG, "stripsu ro_tochange = " + ro_tochange);
        } else Log.d(TAG, "not stripsu ro_tochange = " + ro_tochange);
        if (!getprop(stripto).equals("1"))
            resetprop(executableFilePath, stripto, "1", context, true);
    }

    public static void stripadb(String executableFilePath, Context context) {
        boolean su = SuBinary();
        String stripro = "ro.debuggable";
        String stripto = "no.debuggable";
        String ro_tochange = "" + runCommand(executableFilePath + "busybox" + " strings sbin/adbd | grep " + stripro, su, context);
        if (ro_tochange.contains(stripro)) {
            runCommand("mount -o rw,remount /", su, context);
            runCommand(executableFilePath + "busybox" + " sed -i 's/" + stripro +
                "/" + stripto + "/g' sbin/adbd", su, context);
            runCommand("mount -o ro,remount /", su, context);
            Log.d(TAG, "stripadb ro_tochange = " + ro_tochange);
        } else Log.d(TAG, "not stripadb ro_tochange = " + ro_tochange);
        if (!getprop(stripto).equals("1"))
            resetprop(executableFilePath, stripto, "1", context, true);
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
        runCommand(executableFilePath + "busybox" + " sed -i -r 's/" + strip_old + "/" + strip_new + "/g' " + app_path, su, context);
        Log.d(TAG, " sedstring app " + app_path);
    }

    public static char ChangeLetter(char letter) {
        String base = "abcdefghijklmnopqrstuvwyz";
        base = base + base.toUpperCase(Locale.US);
        char[] randon_char = base.toCharArray();
        char mod_string;
        while (true) {
            mod_string = randon_char[new SecureRandom().nextInt(randon_char.length)];
            if (!String.valueOf(mod_string).equals(String.valueOf(letter)))
                return mod_string;
        }
    }

    public static String random4() {
        String base = "abcdefghijklmnopqrtuvwxyz";
        base = base + base.toUpperCase(Locale.US);
        char[] randon_char = base.toCharArray();
        String mod_string;
        while (true) {
            mod_string = "";
            for (int i = 0; i < 5; i++) {
                mod_string = mod_string + String.valueOf(randon_char[new SecureRandom().nextInt(randon_char.length)]);
            }
            if (runShell("which " + mod_string).isEmpty())
                break;
        }
        return mod_string;
    }

    public static String appStringAddZeros(String app_com) {
        char[] change = app_com.toCharArray();
        String mod_string = "";
        for (int i = 0; i < change.length; ++i) {
            mod_string += change[i];
            mod_string += "\0";
        }
        return mod_string;
    }

    public static String appStringMod(String app_com, boolean zeros) {
        char[] change = app_com.toCharArray();
        String mod_string = "";
        for (int i = 0; i < change.length; ++i) {
            if (String.valueOf(change[i]).equals(".") || i < 4) // i < 4 preserve com. or org. from app name
                mod_string += change[i];
            else
                mod_string += ChangeLetter(change[i]);
            if (zeros)
                mod_string += "\0";
        }
        return mod_string;
    }

    public static int FindOffSet(byte xml[], byte[] COM_PKG_NAME) {
        int offset = -1;
        // Linear search pattern offset
        for (int i = 0; i < xml.length; ++i) {
            boolean match = true;
            for (int j = 0; j < COM_PKG_NAME.length; ++j) {
                if (xml[i + j] != COM_PKG_NAME[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                offset = i;
                break;
            }
        }
        if (offset < 0)
            Log.d(TAG, "offset < 0");
        return offset;
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

    public static void runShellCommand(String command) {
        try {
            StringBuffer output = new StringBuffer();
            Process p = Runtime.getRuntime().exec(new String[] {
                "bash",
                "-c",
                command
            });
            p.waitFor();
        } catch (InterruptedException | IOException e) {
            Log.d(TAG, "catch exception runShellCommand");
        }
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

    public static int getInt(String name, int defaults, Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(name, defaults);
    }

    public static void saveInt(String name, int value, Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(name, value).apply();
    }

    public static void logStatus(Context context) {
        for (int i = 0; i < props.length; i++) {
            String value = getprop(props[i]);
            Log.d(TAG, props[i] + " = " + value + (value.equals(props_OK[i]) ? (value.isEmpty() ? " Empty" : " Green") : (value.isEmpty() ? " Empty" : " Red")));
        }
        String bootdfgp = getprop(robootbuildfingerprint);
        String buildfgp = getprop(robuildfingerprint);
        Log.d(TAG, robootbuildfingerprint + " = " + bootdfgp);
        Log.d(TAG, robuildfingerprint + "           = " + buildfgp + (!bootdfgp.equals(buildfgp) ? " Green" : " Red"));
        Log.d(TAG, "SU " + (SuBinary() ? context.getString(R.string.activated) : context.getString(R.string.deactivated)));
        Log.d(TAG, "SELinux " + getSELinuxStatus(context));
        Log.d(TAG, "ADB " + (AndroidDebugState(context) ? context.getString(R.string.activated) : context.getString(R.string.deactivated)));
    }

    public static String redProps() {
        String red = "";
        for (int i = 0; i < props_fail_sf.length; i++) {
            String value = getprop(props_fail_sf[i]);
            if (!value.isEmpty() && !value.equals(props_fail_sf_OK[i])) {
                if (red.isEmpty())
                    red = props_fail_sf[i] + "=" + value;
                else
                    red += ", " + props_fail_sf[i] + "=" + value;
            }
        }
        String bootdfgp = getprop(robootbuildfingerprint);
        String buildfgp = getprop(robuildfingerprint);
        if (bootdfgp.equals(buildfgp)) {
            if (red.isEmpty())
                red = robootbuildfingerprint + "=" + robuildfingerprint;
            else
                red = ", " + robootbuildfingerprint + "=" + robuildfingerprint;
        }
        return red;
    }


    public static void browser(String site, Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(site)));
        } catch (ActivityNotFoundException ex) {
            DoAToast(context.getString(R.string.no_browser), context);
        }
    }

    public static void email(Context context, String SUBJECT) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:fglfgl27@gmail.com"))
                .putExtra(Intent.EXTRA_SUBJECT, SUBJECT));
        } catch (ActivityNotFoundException ex) {
            Tools.DoAToast(context.getString(R.string.no_email_client), context);
        }
    }

    public static void saveprop(Context context) {
        String value;
        for (int i = 0; i < props.length; i++) {
            value = getprop(props[i]);
            if (value != null && !value.isEmpty())
                saveString(props[i], getprop(props[i]), context);
        }
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
            Log.d(TAG, "File does not exist " + file);
        } catch (IOException e) {
            Log.d(TAG, "Failed to read " + file);
        } finally {
            try {
                if (fileReader != null) fileReader.close();
                if (buf != null) buf.close();
            } catch (IOException e) {
                Log.d(TAG, "IOException readFileN finally");
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
            Log.d(TAG, "File does not exist " + file);
        } catch (IOException e) {
            Log.d(TAG, "Failed to read " + file);
        } finally {
            try {
                if (fileReader != null) fileReader.close();
                if (buf != null) buf.close();
            } catch (IOException e) {
                Log.d(TAG, "IOException readFile finally");
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
            Log.d(TAG, "Failed to write " + path);
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                Log.d(TAG, "IOException writeFile finally");
            }
        }
    }

    public static class LogToZip extends AsyncTask < Void, Void, String > {
        private MaterialDialog progressDialog;
        private WeakReference < Context > contextRef;

        public LogToZip(Context context) {
            contextRef = new WeakReference < > (context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Context mContext = contextRef.get();
            progressDialog = new MaterialDialog.Builder(mContext)
                .title(mContext.getString(R.string.app_name))
                .content(mContext.getString(R.string.generating_log))
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .show();
        }

        @Override
        protected String doInBackground(Void...params) {
            boolean su = SuBinary();
            Context mContext = contextRef.get();
            boolean canSU = rootAccess(mContext);
            String executableFilePath = mContext.getFilesDir().getPath() + "/";
            String sdcard = Environment.getExternalStorageDirectory().getPath();
            String log_folder = sdcard + "/iSu_Logs/";
            String log_temp_folder = sdcard + "/iSu_Logs/tmpziplog/";
            String zip_file = sdcard + "/iSu_Logs/" + "iSu_log" + getDate() + ".zip";
            String logcat = log_temp_folder + "logcat.txt";
            String tmplogcat = log_temp_folder + "tmplogcat.txt";
            String logcatC = "logcat -d ";
            String dmesgC = "dmesg ";
            String getpropC = "getprop ";

            String isuconfig = log_temp_folder + "iSu_config.txt";
            String data_folder = mContext.getFilesDir().getParentFile().getAbsolutePath();
            String perappjson = "per_app.json Accessibility Enabled = " +
                Per_App.isAccessibilityEnabled(mContext, PerAppMonitor.accessibilityId) + "\n";
            String propjson = "\n\nprop.json\n";
            String prefs = "\n\nprefs\n";
            String paths = "\n\npaths\n";

            if (!NewexistFile(log_folder, true, mContext) || !NewexistFile(log_folder, false, mContext)) {
                File dir = new File(log_folder);
                dir.mkdir();
            }

            if (NewexistFile(log_temp_folder, true, mContext) || NewexistFile(log_temp_folder, false, mContext)) {
                if (canSU)
                    runCommand("rm -rf " + log_temp_folder, su, mContext);
                else
                    runShellCommand("rm -rf " + log_temp_folder);
                File dir = new File(log_temp_folder);
                dir.mkdir();
            } else {
                File dir = new File(log_temp_folder);
                dir.mkdir();
            }

            if (canSU) {
                runCommand(logcatC + " > " + logcat, su, mContext);
                runCommand(dmesgC + " > " + log_temp_folder + "dmesg.txt", su, mContext);
                runCommand(getpropC + " > " + log_temp_folder + "getprop.txt", su, mContext);
                runCommand("echo '" + perappjson + "' >> " + isuconfig, su, mContext);
                runCommand("cat " + executableFilePath + "per_app.json >> " + isuconfig, su, mContext);
                runCommand("echo '" + propjson + "' >> " + isuconfig, su, mContext);
                runCommand("cat " + executableFilePath + "prop.json >> " + isuconfig, su, mContext);
                runCommand("echo '" + prefs + "' >> " + isuconfig, su, mContext);
                runCommand("cat " + data_folder + "/shared_prefs/" + PREF_NAME + ".xml >> " + isuconfig, su, mContext);
                runCommand("echo '" + paths + "' >> " + isuconfig, su, mContext);
                runCommand("echo '" + log_folder + "' >> " + isuconfig, su, mContext);
                runCommand("echo '" + data_folder + "' >> " + isuconfig, su, mContext);
                runCommand((su ? "which su" : "which " + readString("cmiyc", null, mContext)) + " >> " + isuconfig, su, mContext);
                runCommand("echo 'iSu version " + BuildConfig.VERSION_NAME + "' >> " + isuconfig, su, mContext);
                runCommand("rm -rf " + log_temp_folder + "logcat_wile.txt", su, mContext);
            } else {
                runShellCommand(logcatC + " > " + logcat);
                if (runShell(dmesgC).isEmpty())
                    runShellCommand("echo 'for security, system is preventing non-root users from reading the kernel log or it is empty' > " + log_temp_folder + "dmesg.txt");
                else
                    runShellCommand(dmesgC + " > " + log_temp_folder + "dmesg.txt");
                runShellCommand(getpropC + " > " + log_temp_folder + "getprop.txt");
                runShellCommand("echo '" + perappjson + "' >> " + isuconfig);
                runShellCommand("cat " + executableFilePath + "per_app.json >> " + isuconfig);
                runShellCommand("echo '" + propjson + "' >> " + isuconfig);
                runShellCommand("cat " + executableFilePath + "prop.json >> " + isuconfig);
                runShellCommand("echo '" + prefs + "' >> " + isuconfig);
                runShellCommand("cat " + data_folder + "/shared_prefs/" + PREF_NAME + ".xml >> " + isuconfig);
                runShellCommand("echo '" + paths + "' >> " + isuconfig);
                runShellCommand("echo '" + log_folder + "' >> " + isuconfig);
                runShellCommand("echo '" + data_folder + "' >> " + isuconfig);
                runShellCommand((su ? "which su" : "which " + readString("cmiyc", null, mContext)) + " >> " + isuconfig);
                runShellCommand("echo 'iSu version " + BuildConfig.VERSION_NAME + "' >> " + isuconfig);
                runShellCommand("rm -rf " + log_temp_folder + "logcat_wile.txt");
            }
            // ZipUtil doesn’t understand folder name that end with /
            // Logcat some times is too long and the zip logcat.txt may be empty, do some check
            while (true) {
                ZipUtil.pack(new File(sdcard + "/iSu_Logs/tmpziplog"), new File(zip_file));
                ZipUtil.unpackEntry(new File(zip_file), "logcat.txt", new File(tmplogcat));
                if (compareFiles(logcat, tmplogcat, true, mContext)) {
                    Log.d(TAG, "ziped logcat.txt is ok");
                    if (canSU)
                        runCommand("rm -rf " + log_temp_folder, su, mContext);
                    else
                        runShellCommand("rm -rf " + log_temp_folder);
                    break;
                } else {
                    Log.d(TAG, "logcat.txt is nok");
                    if (canSU) {
                        runCommand("rm -rf " + zip_file, su, mContext);
                        runCommand("rm -rf " + tmplogcat, su, mContext);
                    } else {
                        runShellCommand("rm -rf " + zip_file);
                        runShellCommand("rm -rf " + tmplogcat);
                    }
                }
            }
            return zip_file;
        }

        @Override
        protected void onPostExecute(String zip) {
            super.onPostExecute(zip);
            Context mContext = contextRef.get();
            progressDialog.dismiss();
            SimpleDialog(String.format(mContext.getString(R.string.generating_log_move), zip), mContext);
        }
    }

    public static String getDate() {
        DateFormat dateformate = new SimpleDateFormat("MMM_dd_yyyy_HH_mm", Locale.US);
        Date date = new Date();
        String Final_Date = "_" + dateformate.format(date);
        return Final_Date;
    }

    public static class CheckUpdate extends AsyncTask < String, String, String > {
        private WeakReference < Context > contextRef;

        public CheckUpdate(Context context) {
            contextRef = new WeakReference < > (context);
        }

        @Override
        protected String doInBackground(String...site) {
            String webPage = site[0];
            try {
                OkHttpClient client = new OkHttpClient.Builder().build();
                Request request = new Request.Builder().url(webPage).build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Context mContext = contextRef.get();
            if (result != null && !result.isEmpty()) {
                String[] sitesplit = result.split(",");
                saveString("last_app_version", sitesplit[0], mContext);
                saveString("last_app_link", sitesplit[1], mContext);
            } else {
                saveString("last_app_version", "", mContext);
                saveString("last_app_link", "", mContext);
            }
            SendBroadcast("updateChecksReceiver", mContext);
        }

    }
}
