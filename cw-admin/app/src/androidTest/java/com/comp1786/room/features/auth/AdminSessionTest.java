package com.comp1786.room.features.auth;

import com.comp1786.room.features.auth.data.AdminAuth;
import com.comp1786.room.features.cloud.data.CloudSyncTest;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

public class AdminSessionTest {
    public static void signInAdmin() throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        AdminAuth.signOut();
        String email = "admin-" + UUID.randomUUID() + "@test.eventa";
        Tasks.await(auth.createUserWithEmailAndPassword(email, "EmulatorOnly123!"), 20, TimeUnit.SECONDS);
        assertFalse(Tasks.await(AdminAuth.authorize(), 20, TimeUnit.SECONDS));
        String path = "/roles/" + auth.getCurrentUser().getUid();
        CloudSyncTest.ownerRequest("PATCH", path, "{\"fields\":{\"role\":{\"stringValue\":\"admin\"}}}");
        assertTrue(Tasks.await(AdminAuth.authorize(), 20, TimeUnit.SECONDS));
    }

    @Test public void requiresAssignedRoleAndRejectsRevocation() throws Exception {
        try {
            signInAdmin();
            assertTrue(AdminAuth.hasSession());
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            CloudSyncTest.ownerRequest("DELETE", "/roles/" + uid, null);
            assertFalse(Tasks.await(AdminAuth.authorize(), 20, TimeUnit.SECONDS));
            assertFalse(AdminAuth.hasSession());
        } finally {
            AdminAuth.signOut();
        }
    }
}
