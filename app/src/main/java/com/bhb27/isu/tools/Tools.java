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

import android.content.Context;

import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;

import com.bhb27.isu.tools.RootFile;
import com.bhb27.isu.tools.RootUtils;

public class Tools implements Constants {

    private String kernel_support_rc = "";
    private String kernel_support_sh = "";

    public boolean KernelSupport() {
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

    // simple toast function to center the message Main.this
    public void DoAToast(String message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
        if (view != null) view.setGravity(Gravity.CENTER);
        toast.show();
    }

    public static boolean isSELinuxActive() {
        if (getSELinuxStatus().equals("Enforcing")) return true;
        return false;
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

    public static boolean existFile(String file, boolean asRoot) {
        if (asRoot) return new RootFile(file).exists();
        return new File(file).exists();
    }

    public static boolean IexistFile(String file, boolean asRoot) {
        if (asRoot) return new RootFile(file).Iexists();
        return new File(file).exists();
    }

    public static String readFile(String file, boolean asRoot) {
        if (asRoot) return new RootFile(file).readFile();

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
            new RootFile(path).write(text, append);
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
