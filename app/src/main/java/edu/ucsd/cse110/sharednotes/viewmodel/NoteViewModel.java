package edu.ucsd.cse110.sharednotes.viewmodel;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.app.Application;
import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import edu.ucsd.cse110.sharednotes.model.Note;
import edu.ucsd.cse110.sharednotes.model.NoteDatabase;
import edu.ucsd.cse110.sharednotes.model.NoteRepository;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteViewModel extends AndroidViewModel {
    private LiveData<Note> note;
    private final NoteRepository repo;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        var context = application.getApplicationContext();
        var db = NoteDatabase.provide(context);
        var dao = db.getDao();
        this.repo = new NoteRepository(dao);
    }

    public LiveData<Note> getNote(String title) {
        note = repo.getSynced(title);

        // The returned live data should update whenever there is a change in
        // the database, or when the server returns a newer version of the note.
        // Polling interval: 3s.
        if (note == null) {
            note = repo.getLocal(title);
        }
        return note; //Server is same or newer
    }

    public void save(Note note) {
        // TODO: try to upload the note to the server.
        Log.d("NoteAPI: ", "save()");
        note.version++;

        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), note.toJSON());
        Request req = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + note.title)
                .put(body)
                .build();
        Log.d("req", note.title);
        try (Response response = client.newCall(req).execute()) {
            Log.d("NoteViewModel:", "response.body.string()");
        } catch (Exception e) {}

        repo.upsertLocal(note);
        repo.upsertRemote(note);
        repo.upsertSynced(note);
    }

    @AnyThread
    public void saveAsync(Note note) {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> save(note));

    }
}
