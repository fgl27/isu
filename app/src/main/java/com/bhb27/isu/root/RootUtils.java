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
package com.bhb27.isu.root;

import android.util.Log;

import com.bhb27.isu.Tools;
import com.bhb27.isu.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by willi on 14.12.14.
 */

/**
 * Here you have different functions which will help you with root commands.
 * I think they are self explained and do no need any further descriptions.
 */
public class RootUtils {

    private static SU su;
    private static ISU isu;

    public static boolean rooted() {
        return existBinary("su");
    }

    public static boolean rootAccess() {
        SU su = getSU();
        su.runCommand("echo /testRoot/");
        return !su.denied;
    }

    public static boolean busyboxInstalled() {
        return existBinary("busybox") || existBinary("toybox");
    }

    private static boolean existBinary(String binary) {
        for (String path : System.getenv("PATH").split(":")) {
            if (!path.endsWith("/")) path += "/";
            if (new File(path + binary).exists() || Tools.existFile(path + binary, true))
                return true;
        }
        return false;
    }

    public static String getKernelVersion() {
        return runCommand("uname -r");
    }

    public static void mount(boolean writeable, String mountpoint) {
        runCommand(writeable ? "mount -o remount,rw " + mountpoint + " " + mountpoint :
                "mount -o remount,ro " + mountpoint + " " + mountpoint);
        runCommand(writeable ? "mount -o remount,rw " + mountpoint :
                "mount -o remount,ro " + mountpoint);
    }

    public static void closeSU() {
        if (su != null) su.close();
        su = null;
    }

    public static String runCommand(String command) {
        return getSU().runCommand(command);
    }

    private static SU getSU() {
        if (su == null || su.closed || su.denied) {
            if (su != null && !su.closed) {
                su.close();
            }
            su = new SU();
        }
        return su;
    }

    /*
     * Based on AndreiLux's SU code in Synapse
     * https://github.com/AndreiLux/Synapse/blob/master/src/main/java/com/af/synapse/utils/Utils.java#L238
     */
    public static class SU {

        private Process mProcess;
        private BufferedWriter mWriter;
        private BufferedReader mReader;
        private final boolean mRoot;
        private final String mTag;
        private boolean closed;
        private boolean denied;
        private boolean firstTry;

        public SU() {
            this(true, null);
        }

        public SU(boolean root, String tag) {
            mRoot = root;
            mTag = tag;
            try {
                if (mTag != null) {
                    Log.i(mTag, String.format("%s initialized", root ? "SU" : "SH"));
                }
                firstTry = true;
                mProcess = Runtime.getRuntime().exec(root ? "su" : "sh");
                mWriter = new BufferedWriter(new OutputStreamWriter(mProcess.getOutputStream()));
                mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            } catch (IOException e) {
                if (mTag != null) {
                    Log.e(mTag, root ? "Failed to run shell as su" : "Failed to run shell as sh");
                }
                denied = true;
                closed = true;
            }
        }

        public synchronized String runCommand(final String command) {
            synchronized (this) {
                try {
                    StringBuilder sb = new StringBuilder();
                    String callback = "/shellCallback/";
                    mWriter.write(command + "\necho " + callback + "\n");
                    mWriter.flush();

                    int i;
                    char[] buffer = new char[256];
                    while (true) {
                        sb.append(buffer, 0, mReader.read(buffer));
                        if ((i = sb.indexOf(callback)) > -1) {
                            sb.delete(i, i + callback.length());
                            break;
                        }
                    }
                    firstTry = false;
                    if (mTag != null) {
                        Log.i(mTag, "run: " + command + " output: " + sb.toString().trim());
                    }
                    return sb.toString().trim();
                } catch (IOException e) {
                    closed = true;
                    e.printStackTrace();
                    if (firstTry) denied = true;
                } catch (ArrayIndexOutOfBoundsException e) {
                    denied = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    denied = true;
                }
                return null;
            }
        }

        public void close() {
            try {
                if (mWriter != null) {
                    mWriter.write("exit\n");
                    mWriter.flush();

                    mWriter.close();
                }
                mReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                mProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mProcess.destroy();
            if (mTag != null) {
                Log.i(mTag, String.format("%s closed: %d", mRoot ? "SU" : "SH", mProcess.exitValue()));
            }
            closed = true;
        }

    }

    //ISU
    public static String runICommand(String command) {
        return getISU().runCommand(command);
    }

    private static ISU getISU() {
        if (isu == null || isu.closed || isu.denied) {
            if (isu != null && !isu.closed) {
                isu.close();
            }
            isu = new ISU();
        }
        return isu;
    }

    public static class ISU {

        private Process mProcess;
        private BufferedWriter mWriter;
        private BufferedReader mReader;
        private final boolean mRoot;
        private final String mTag;
        private boolean closed;
        private boolean denied;
        private boolean firstTry;

        public ISU() {
            this(true, null);
        }

        public ISU(boolean root, String tag) {
            mRoot = root;
            mTag = tag;
            try {
                if (mTag != null) {
                    Log.i(mTag, String.format("%s initialized", root ? "ISU" : "SH"));
                }
                firstTry = true;
                mProcess = Runtime.getRuntime().exec(root ? "isu" : "sh");
                mWriter = new BufferedWriter(new OutputStreamWriter(mProcess.getOutputStream()));
                mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            } catch (IOException e) {
                if (mTag != null) {
                    Log.e(mTag, root ? "Failed to run shell as isu" : "Failed to run shell as sh");
                }
                denied = true;
                closed = true;
            }
        }

        public synchronized String runCommand(final String command) {
            synchronized (this) {
                try {
                    StringBuilder sb = new StringBuilder();
                    String callback = "/shellCallback/";
                    mWriter.write(command + "\necho " + callback + "\n");
                    mWriter.flush();

                    int i;
                    char[] buffer = new char[256];
                    while (true) {
                        sb.append(buffer, 0, mReader.read(buffer));
                        if ((i = sb.indexOf(callback)) > -1) {
                            sb.delete(i, i + callback.length());
                            break;
                        }
                    }
                    firstTry = false;
                    if (mTag != null) {
                        Log.i(mTag, "run: " + command + " output: " + sb.toString().trim());
                    }
                    return sb.toString().trim();
                } catch (IOException e) {
                    closed = true;
                    e.printStackTrace();
                    if (firstTry) denied = true;
                } catch (ArrayIndexOutOfBoundsException e) {
                    denied = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    denied = true;
                }
                return null;
            }
        }

        public void close() {
            try {
                if (mWriter != null) {
                    mWriter.write("exit\n");
                    mWriter.flush();

                    mWriter.close();
                }
                mReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                mProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mProcess.destroy();
            if (mTag != null) {
                Log.i(mTag, String.format("%s closed: %d", mRoot ? "ISU" : "SH", mProcess.exitValue()));
            }
            closed = true;
        }

    }

}
