package com.comp1786.room.features.cloud.logic;

import android.app.Application;
import androidx.lifecycle.*;
import androidx.work.*;
import com.comp1786.room.features.cloud.data.SyncState;
import com.comp1786.room.features.events.data.local.EventDatabase;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CloudViewModel extends AndroidViewModel {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final MutableLiveData<FirebaseUser> user = new MutableLiveData<>(auth.getCurrentUser());
    private final MutableLiveData<Boolean> signingIn = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>("");
    private final ExecutorService databaseWorker = Executors.newSingleThreadExecutor();
    private final FirebaseAuth.AuthStateListener listener = value -> user.setValue(value.getCurrentUser());

    public CloudViewModel(Application application) {
        super(application);
        auth.addAuthStateListener(listener);
    }

    public LiveData<FirebaseUser> user() { return user; }
    public LiveData<Boolean> signingIn() { return signingIn; }
    public LiveData<String> error() { return error; }
    public LiveData<SyncState> state() { return EventDatabase.get(getApplication()).sync().observe(); }
    public LiveData<List<WorkInfo>> work() {
        return WorkManager.getInstance(getApplication()).getWorkInfosForUniqueWorkLiveData(CloudSync.IMMEDIATE);
    }

    public void signIn(String email, String password) { authenticate(email, password, false); }
    public void createAccount(String email, String password) { authenticate(email, password, true); }

    private void authenticate(String email, String password, boolean create) {
        if (Boolean.TRUE.equals(signingIn.getValue())) return;
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.isEmpty()) {
            error.setValue("Nhập email và mật khẩu tài khoản quản trị.");
            return;
        }
        error.setValue("");
        signingIn.setValue(true);
        com.google.android.gms.tasks.Task<AuthResult> request = create
                ? auth.createUserWithEmailAndPassword(email, password)
                : auth.signInWithEmailAndPassword(email, password);
        request.addOnCompleteListener(result -> {
            signingIn.setValue(false);
            if (!result.isSuccessful()) error.setValue(
                    result.getException() instanceof FirebaseNetworkException
                    ? "Chưa có kết nối Internet để đăng nhập."
                    : create ? "Không tạo được tài khoản. Kiểm tra độ dài mật khẩu; nếu email đã có tài khoản, hãy đăng nhập."
                    : "Không đăng nhập được. Kiểm tra email, mật khẩu và trạng thái tài khoản.");
        });
    }

    public void uploadAll() {
        if (auth.getCurrentUser() == null) return;
        databaseWorker.execute(() -> {
            EventDatabase.get(getApplication()).sync().requestUpload();
            CloudSync.enqueue(getApplication());
        });
    }

    public void signOut() { auth.signOut(); }
    @Override protected void onCleared() {
        auth.removeAuthStateListener(listener);
        databaseWorker.shutdown();
    }
}
