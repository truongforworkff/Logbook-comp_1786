package com.comp1786.room.features.cloud.data;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Source;
import org.junit.Test;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

public class FirebaseSetupTest {
    @Test public void connectsWithoutGrantingUnapprovedAccess() throws Exception {
        assertEquals("eventa-coursework", FirebaseApp.getInstance().getOptions().getProjectId());
        try {
            Tasks.await(FirebaseFirestore.getInstance().collection("setup_checks")
                    .document("probe").get(Source.SERVER), 25, TimeUnit.SECONDS);
            fail("Unapproved reads must be rejected by the live server");
        } catch (ExecutionException error) {
            assertTrue(error.getCause() instanceof FirebaseFirestoreException);
            FirebaseFirestoreException denied = (FirebaseFirestoreException) error.getCause();
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, denied.getCode());
        }
    }
}
