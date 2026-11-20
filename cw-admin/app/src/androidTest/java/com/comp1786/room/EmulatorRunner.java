package com.comp1786.room;

import android.app.Application;
import androidx.test.runner.AndroidJUnitRunner;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public final class EmulatorRunner extends AndroidJUnitRunner {
    @Override public void callApplicationOnCreate(Application app) {
        // Isolate WorkManager and screens before EventaApplication starts observing Room.
        if (!FirebaseApp.getApps(app).isEmpty()) FirebaseApp.getInstance().delete();
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId("demo-eventa").setApplicationId("1:123:android:test")
                .setApiKey("fake-emulator-api-key").build();
        FirebaseApp.initializeApp(app, options);
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9098);
        FirebaseAuth.getInstance().signOut();
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8086);
        super.callApplicationOnCreate(app);
    }
}
