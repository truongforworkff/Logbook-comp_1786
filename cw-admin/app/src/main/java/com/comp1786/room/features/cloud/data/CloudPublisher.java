package com.comp1786.room.features.cloud.data;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class CloudPublisher {
    private CloudPublisher() {}

    static DocumentReference catalog(FirebaseFirestore firestore) {
        return firestore.document("role_catalogues/main");
    }

    public static void publish(FirebaseFirestore firestore, CloudSnapshot snapshot,
            Map<String, Object> payload) throws Exception {
        DocumentReference reference = catalog(firestore);
        Tasks.await(firestore.runTransaction(transaction -> {
            DocumentSnapshot remote = transaction.get(reference);
            if (remote.exists()) {
                if (!Objects.equals(remote.getString("sourceId"), snapshot.state.sourceId))
                    throw new FirebaseFirestoreException("SOURCE_MISMATCH",
                            FirebaseFirestoreException.Code.FAILED_PRECONDITION);
                Long revision = remote.getLong("revision");
                if (revision == null) throw new FirebaseFirestoreException("INVALID_CATALOG",
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION);
                if (revision == snapshot.state.revision
                        && Objects.equals(remote.get("events"), payload.get("events"))) return null;
                if (revision != snapshot.state.uploadedRevision || revision >= snapshot.state.revision)
                    throw new FirebaseFirestoreException("LOCAL_BEHIND",
                            FirebaseFirestoreException.Code.FAILED_PRECONDITION);
            }
            transaction.set(reference, payload);
            return null;
        }), 45, TimeUnit.SECONDS);
    }
}
