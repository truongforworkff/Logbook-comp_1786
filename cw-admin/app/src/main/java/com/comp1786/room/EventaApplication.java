package com.comp1786.room;

import android.app.Application;
import android.app.LocaleManager;
import android.os.Build;
import android.os.LocaleList;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import com.comp1786.room.features.cloud.logic.CloudSync;

public final class EventaApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 33) {
            LocaleManager locales = getSystemService(LocaleManager.class);
            LocaleList english = LocaleList.forLanguageTags("en-GB");
            if (!english.equals(locales.getApplicationLocales())) locales.setApplicationLocales(english);
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en-GB"));
        }
        CloudSync.start(this);
    }
}
