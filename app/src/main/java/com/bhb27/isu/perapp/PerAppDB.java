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
package com.bhb27.isu.perapp;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 15.04.15.
 */
public class PerAppDB extends JsonDB {

    public PerAppDB(Context context) {
        super(context.getFilesDir() + "/per_app.json", 1, context);
    }

    @Override
    public DBJsonItem getItem(JSONObject item) {
        return new PerAppItem(item);
    }

    public boolean containsApp(String app) {
        List < PerAppItem > profiles = getAllApps();

        for (PerAppItem profile: profiles) {
            if (profile.getApp().equals(app)) {
                return true;
            }
        }

        return false;
    }

    public ArrayList < String > get_info(String app) {
        List < PerAppItem > profiles = getAllApps();
        ArrayList < String > list = new ArrayList < String > ();

        for (PerAppItem profile: profiles) {
            if (profile.getApp().equals(app)) {
                list.add(0, profile.getApp());
                list.add(1, profile.getID());
                return (list);
            }
        }

        return null;
    }


    public void putApp(String app, String id) {
        try {
            JSONObject items = new JSONObject();
            items.put("name", app);
            items.put("id", id);

            putItem(items);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void delApp(int index) {
        delete(index);
    }

    public List < PerAppItem > getAllApps() {
        List < PerAppItem > items = new ArrayList < > ();
        for (DBJsonItem jsonItem: getAllItems())
            items.add((PerAppItem) jsonItem);
        return items;
    }

    public static class PerAppItem extends DBJsonItem {

        public PerAppItem(JSONObject object) {
            item = object;
        }

        public String getApp() {
            return getString("name");
        }

        public String getID() {
            return getString("id");
        }

        private String getString(String name) {
            try {
                return getItem().getString(name);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

}
