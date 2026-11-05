package com.comp1786.room.features.sessions.logic;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.*;
import com.comp1786.room.features.events.data.local.EventDatabase;
import com.comp1786.room.features.sessions.data.local.SessionDao;
import com.comp1786.room.features.sessions.data.model.Session;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SessionViewModel extends AndroidViewModel {
    public enum Result { IDLE, SAVED, DELETED, ERROR }
    private final SessionDao dao;
    private final com.comp1786.room.features.events.data.local.EventDao events;
    private final MutableLiveData<Result> result = new MutableLiveData<>(Result.IDLE);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean busy;

    public SessionViewModel(Application application) {
        super(application);
        dao = EventDatabase.get(application).sessions();
        events = EventDatabase.get(application).events();
    }

    public LiveData<List<Session>> getSessions(long eventId, boolean unassigned) {
        return unassigned ? dao.observeUnassigned() : dao.observeForEvent(eventId);
    }
    public LiveData<Session> getSession(long id) { return dao.observeById(id); }
    public LiveData<com.comp1786.room.features.events.data.model.Event> getEvent(long id) {
        return events.observeById(id);
    }
    public LiveData<Integer> getUnassignedCount() { return dao.observeUnassignedCount(); }
    public LiveData<Result> getResult() { return result; }
    public boolean isBusy() { return busy; }
    public void consumeResult() { result.setValue(Result.IDLE); }

    public void save(Session session) {
        if (!SessionValidation.valid(session)) { result.setValue(Result.ERROR); return; }
        change(() -> {
            if (session.id == 0) dao.insert(session);
            else if (dao.update(session) != 1) throw new IllegalStateException("Session no longer exists");
        }, Result.SAVED);
    }

    public void delete(Session session) {
        change(() -> {
            if (dao.delete(session) != 1) throw new IllegalStateException("Session no longer exists");
        }, Result.DELETED);
    }

    private void change(Runnable action, Result success) {
        if (busy) return;
        busy = true;
        executor.execute(() -> {
            Result outcome;
            try { action.run(); outcome = success; }
            catch (RuntimeException error) { outcome = Result.ERROR; }
            Result completed = outcome;
            main.post(() -> { busy = false; result.setValue(completed); });
        });
    }

    @Override protected void onCleared() { executor.shutdown(); }
}
