package com.comp1786.room.features.cloud.logic;

import com.google.firebase.firestore.FirebaseFirestoreException;
import org.junit.Test;
import static org.junit.Assert.*;

public class CloudSyncWorkerTest {
    @Test public void retryTransientFailuresButExplainPermissionAndSourceErrors() {
        for (FirebaseFirestoreException.Code code : new FirebaseFirestoreException.Code[]{
                FirebaseFirestoreException.Code.PERMISSION_DENIED,
                FirebaseFirestoreException.Code.INVALID_ARGUMENT,
                FirebaseFirestoreException.Code.FAILED_PRECONDITION,
                FirebaseFirestoreException.Code.UNAUTHENTICATED}) {
            assertFalse(CloudSyncWorker.retryable(new FirebaseFirestoreException("blocked", code)));
        }
        assertTrue(CloudSyncWorker.retryable(new FirebaseFirestoreException("offline",
                FirebaseFirestoreException.Code.UNAVAILABLE)));
        assertTrue(CloudSyncWorker.message(new FirebaseFirestoreException("blocked",
                FirebaseFirestoreException.Code.PERMISSION_DENIED)).contains("quyền"));
        assertTrue(CloudSyncWorker.message(new FirebaseFirestoreException("SOURCE_MISMATCH",
                FirebaseFirestoreException.Code.FAILED_PRECONDITION)).contains("bộ dữ liệu"));
    }
}
