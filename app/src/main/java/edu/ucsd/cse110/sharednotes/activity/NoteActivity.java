package edu.ucsd.cse110.sharednotes.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import edu.ucsd.cse110.sharednotes.R;
import edu.ucsd.cse110.sharednotes.model.Note;
import edu.ucsd.cse110.sharednotes.model.NoteAPI;
import edu.ucsd.cse110.sharednotes.model.NoteDao;
import edu.ucsd.cse110.sharednotes.model.NoteDatabase;
import edu.ucsd.cse110.sharednotes.viewmodel.ListViewModel;
import edu.ucsd.cse110.sharednotes.viewmodel.NoteViewModel;

public class NoteActivity extends AppCompatActivity {

    private LiveData<Note> note;
    private NoteDao dao;
    private EditText contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("NoteActivity: ", "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        contentView = findViewById(R.id.edittext_note_contents);

        var intent = getIntent();
        var title = intent.getStringExtra("note_title");

        var viewModel = setupViewModel();
        note = viewModel.getNote(title);
        if (note.getValue() == null) {
            Log.d("WHAT THE FUCk", "hi");
        }
        
        // Set up the toolbar.
        setupToolbar(title);

        // Set up button.
        setupSaveButton(viewModel);

        // Set up the contents to update.
        note.observe(this, this::onNoteChanged);
    }

    private NoteViewModel setupViewModel() {
        Log.d("NoteActivity: ", "setupViewModel()");
        return new ViewModelProvider(this).get(NoteViewModel.class);
    }

    private void setupToolbar(String title) {
        Log.d("NoteActivity: ", "setupToolbar()");
        // Get the toolbar from the layout and set it as the action bar.
        var toolbar = (Toolbar) findViewById(R.id.toolbar_note);
        setSupportActionBar(toolbar);

        // Get the action bar (note this is type ActionBar, not Toolbar).
        var actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(title);

        // Enable the home button, and set it to be an "up" (back) button.
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setupSaveButton(NoteViewModel viewModel) {
        Log.d("NoteActivity: ", "setupSaveButton()");
        var saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener((View v) -> {
            var updatedNote = note.getValue();
            var updatedContent = contentView.getText().toString();
            assert updatedNote != null;

            updatedNote.content = updatedContent;

            Log.d("Updated note content:", updatedNote.content);
            viewModel.saveAsync(updatedNote);
        });
    }

    private void onNoteChanged(Note note) {
        Log.d("NoteActivity: ", "onNoteChanged()");
        contentView.setText(note.content);
    }

    /** Utility method to create an intent for this activity. */
    public static Intent intentFor(Context context, Note note) {
        Log.d("NoteActivity: ", "intentFor()");
        var intent = new Intent(context, NoteActivity.class);
        intent.putExtra("note_title", note.title);
        return intent;
    }
}