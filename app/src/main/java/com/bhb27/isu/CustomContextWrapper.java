package com.bhb27.isu;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

import com.bhb27.isu.tools.Tools;

public class CustomContextWrapper extends ContextWrapper {

    public CustomContextWrapper(Context base) {
        super(base);
    }

    @SuppressWarnings("deprecation")
    public static ContextWrapper wrap(Context context) {

        Locale newLocale;
        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();

        if (Tools.getBoolean("forceenglish", false, context))
            newLocale = new Locale("en_US");
        else
            newLocale = new Locale(Tools.sysLocale());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(newLocale);
            LocaleList localeList = new LocaleList(newLocale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            context = context.createConfigurationContext(configuration);
        } else { //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            configuration.setLocale(newLocale);
            context = context.createConfigurationContext(configuration);
        }

        return new ContextWrapper(context);
    }

}
