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

package com.bhb27.isu.root;

import android.util.Log;

import com.bhb27.isu.Tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 08.02.15.
 */

/**
 * This class is kinda similar to {@link java.io.File}.
 * Only difference is that this class runs as Root.
 */
public class IRootFile {

    private final String file;

    public IRootFile(String file) {
        this.file = file;
    }

    public String getName() {
        return RootUtils.runICommand("basename '" + file + "'");
    }

    public void mkdir() {
        RootUtils.runICommand("mkdir -p -m777 '" + file + "'");
    }

    public void mv(String newPath) {
        RootUtils.runICommand("mv -f '" + file + "' '" + newPath + "'");
    }

    public void write(String text, boolean append) {
        String[] textarray = text.split("\\r?\\n");
        RootUtils.runICommand(append ? "echo '" + textarray[0] + "' >> " + file : "echo '" + textarray[0] + "' > " + file);
        if (textarray.length > 1) for (int i = 1; i < textarray.length; i++)
            RootUtils.runICommand("echo '" + textarray[i] + "' >> " + file);
    }

    public void delete() {
        RootUtils.runICommand("rm -r '" + file + "'");
    }

    public List<String> list() {
        List<String> list = new ArrayList<>();
        String files = RootUtils.runICommand("ls '" + file + "'");
        if (files != null)
            // Make sure the file exists
            for (String file : files.split("\\r?\\n"))
                if (file != null && !file.isEmpty() && Tools.existFile(this.file + "/" + file, true))
                    list.add(file);
        return list;
    }

    public List<RootFile> listFiles() {
        List<RootFile> list = new ArrayList<>();
        String files = RootUtils.runICommand("ls '" + file + "'");
        if (files != null)
            // Make sure the file exists
            for (String file : files.split("\\r?\\n"))
                if (file != null && !file.isEmpty() && Tools.existFile(this.file + "/" + file, true))
                    list.add(new RootFile(this.file + "/" + file));
        return list;
    }

    public float length() {
        try {
            return Float.parseFloat(RootUtils.runICommand("du '" + file + "'").split(file)[0].trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    public String getParent() {
        return RootUtils.runICommand("dirname '" + file + "'");
    }

    public boolean isEmpty() {
        return RootUtils.runICommand("find '" + file + "' -mindepth 1 | read || echo false").equals("false");
    }

    public boolean exists() {
        String output = RootUtils.runICommand("[ -e '" + file + "' ] && echo true");
        return output != null && output.contains("true");
    }

    public String readFile() {
        return RootUtils.runICommand("cat '" + file + "'");
    }

    public String toString() {
        return file;
    }

}
