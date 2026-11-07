package com.comp1786.room.features.cloud.data;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class CloudPublisher {
    private CloudPublisher() {}

    public static void publish(FirebaseFirestore firestore, CloudSnapshot snapshot,
            Map<String, Object> payload, String uid) throws Exception {
        DocumentReference reference = firestore.document("catalogues/main");
        Tasks.await(firestore.runTransaction(transaction -> {
            DocumentSnapshot remote = transaction.get(reference);
            if (remote.exists()) {
                if (!Objects.equals(remote.getString("sourceId"), snapshot.state.sourceId)
                        || !Objects.equals(remote.getString("publisherUid"), uid))
                    throw new FirebaseFirestoreException("SOURCE_MISMATCH",
                            FirebaseFirestoreException.Code.FAILED_PRECONDITION);
                Long revision = remote.getLong("revision");
                if (revision == null) throw new FirebaseFirestoreException("INVALID_CATALOG",
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION);
                if (revision >= snapshot.state.revision) return null;
            }
            transaction.set(reference, payload);
            return null;
        }), 45, TimeUnit.SECONDS);
    }
}
