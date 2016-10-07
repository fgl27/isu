/*
 * Copyright (C) 2015 Willi Ye
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

import android.content.Context;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;

import com.bhb27.isu.root.RootFile;
import com.bhb27.isu.root.RootUtils;

public class Tools {

    /**
     * Debugging TAG
     */
    public final static String TAG = "iSu";

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
}
