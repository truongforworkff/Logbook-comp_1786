package com.comp1786.room;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/** Database writes run off the UI thread; LiveData refreshes the list automatically. */
public class EventViewModel extends AndroidViewModel {
    public enum Operation { IDLE, SUCCESS, ERROR }
    private final EventDao dao;
    private final LiveData<List<Event>> events;
    private final MutableLiveData<Operation> operation = new MutableLiveData<>(Operation.IDLE);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean busy;
    public EventViewModel(Application application) {
        super(application);
        dao = EventDatabase.get(application).events();
        events = dao.observeAll();
    }
    public LiveData<List<Event>> getEvents() { return events; }
    public LiveData<Operation> getOperation() { return operation; }
    public boolean isBusy() { return busy; }
    public void consumeOperation() { operation.setValue(Operation.IDLE); }
    public void save(Event event) { change(() -> { if (event.id == 0) dao.insert(event); else dao.update(event); }); }
    public void delete(Event event) { change(() -> dao.delete(event)); }
    private void change(Runnable action) {
        if (busy) return;
        busy = true;
        executor.execute(() -> {
            Operation result;
            try { action.run(); result = Operation.SUCCESS; }
            catch (RuntimeException error) { result = Operation.ERROR; }
            Operation outcome = result;
            main.post(() -> { busy = false; operation.setValue(outcome); });
        });
    }
    @Override protected void onCleared() { executor.shutdown(); }
}
