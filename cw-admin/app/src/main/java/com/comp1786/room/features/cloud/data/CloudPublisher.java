package com.comp1786.room.features.cloud.data;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class CloudPublisher {
    private CloudPublisher() {}

    public static void publish(FirebaseFirestore firestore, CloudSnapshot snapshot,
            Map<String, Object> payload, String writeKey) throws Exception {
        if (!writeKey.matches("[0-9a-f]{64}"))
            throw new IllegalArgumentException("Bản Admin chưa có cấu hình kết nối Cloud.");
        DocumentReference reference = firestore.document("catalogues/main");
        Tasks.await(firestore.runTransaction(transaction -> {
            DocumentSnapshot remote = transaction.get(reference);
            if (remote.exists()) {
                if (!Objects.equals(remote.getString("sourceId"), snapshot.state.sourceId))
                    throw new FirebaseFirestoreException("SOURCE_MISMATCH",
                            FirebaseFirestoreException.Code.FAILED_PRECONDITION);
                Long revision = remote.getLong("revision");
                if (revision == null) throw new FirebaseFirestoreException("INVALID_CATALOG",
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION);
                if (revision > snapshot.state.revision)
                    throw new FirebaseFirestoreException("LOCAL_BEHIND",
                            FirebaseFirestoreException.Code.FAILED_PRECONDITION);
                if (revision == snapshot.state.revision) return null;
            }
            Map<String, Object> receipt = new LinkedHashMap<>();
            receipt.put("writeKey", writeKey);
            receipt.put("sourceId", snapshot.state.sourceId);
            receipt.put("revision", snapshot.state.revision);
            receipt.put("updatedAt", FieldValue.serverTimestamp());
            transaction.set(firestore.document("sync_writes/current"), receipt);
            transaction.set(reference, payload);
            return null;
        }), 45, TimeUnit.SECONDS);
    }
}
