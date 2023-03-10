package edu.ucsd.cse110.sharednotes.viewmodel;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import edu.ucsd.cse110.sharednotes.model.Note;
import edu.ucsd.cse110.sharednotes.model.NoteAPI;
import edu.ucsd.cse110.sharednotes.model.NoteDatabase;
import edu.ucsd.cse110.sharednotes.model.NoteRepository;

public class ListViewModel extends AndroidViewModel {
    private LiveData<List<Note>> notes;
    private final NoteRepository repo;

    public ListViewModel(@NonNull Application application) {
        super(application);
        var context = application.getApplicationContext();
        var db = NoteDatabase.provide(context);
        var dao = db.getDao();
        this.repo = new NoteRepository(dao);
    }

    /**
     * Load all notes from the database.
     * @return a LiveData object that will be updated when any notes change.
     */
    public LiveData<List<Note>> getNotes() {
        if (notes == null) {
            notes = repo.getAllLocal();
        }
        return notes;
    }

    /**
     * Open a note in the database. If the note does not exist, create it.
     * @param title the title of the note
     * @return a LiveData object that will be updated when this note changes.
     */
    public LiveData<Note> getOrCreateNote(String title) {
        if (!repo.existsLocal(title)) { //This method works
            Log.d("ListViewModel: ", "Note does not already exist in local database");

            //Check if it exists on the server
            NoteAPI API = new NoteAPI();
            Future<Note> future = API.getNoteAsync(title);
            Note existing_note = null;
            try {existing_note = future.get(1, SECONDS);} catch (Exception e) {
                Log.d("ListViewModel:", "Exception occurred.");
            }

            if (existing_note.content != null) {
                Log.d("ListViewModel:", "Note exists on server.");
            } else {
                Log.d("ListViewModel:", "Note does not exist on server");
                existing_note = new Note(title, "");
            }
            repo.upsertLocal(existing_note);
        }
        return repo.getLocal(title);
    }

    public void delete(Note note) {
        repo.deleteLocal(note);
    }
}
