package com.comp1786.room;

import android.app.Application;
import com.comp1786.room.features.cloud.logic.CloudSync;

public final class EventaApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        CloudSync.start(this);
    }
}
