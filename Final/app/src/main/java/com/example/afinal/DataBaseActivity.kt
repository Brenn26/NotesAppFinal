package com.example.afinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DatabaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            LaunchedEffect(context.dataStore) {
                context.dataStore.data.collect { preferences ->
                    isDarkMode = preferences[booleanPreferencesKey("dark_mode")] ?: false
                }
            }

            val colorScheme = if (isDarkMode) DarkColors else LightColors

            MaterialTheme(colorScheme = colorScheme) {
                DatabaseScreen(onBackPressed = { finish() })
            }
        }
    }
}

@Composable
fun DatabaseScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val noteDao = database.noteDao()
    val scope = rememberCoroutineScope()
    var notes by remember { mutableStateOf(listOf<Note>()) }
    var currentNote by remember { mutableStateOf(Note()) } // Initialize with default values

    // Collect notes
    LaunchedEffect(Unit) {
        scope.launch {
            noteDao.getAllNotes().collectLatest { noteList ->
                notes = noteList
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Apply the background color
            .padding(16.dp)
    ) {
        Text(text = "Database Activity", style = MaterialTheme.typography.headlineMedium)

        // Back Button
        Button(onClick = onBackPressed, modifier = Modifier.padding(bottom = 16.dp)) {
            Text(text = "Back")
        }

        // Form to create notes
        NoteForm(currentNote) { newNote ->
            scope.launch {
                if (newNote.id == 0) {
                    noteDao.insert(newNote)
                } else {
                    noteDao.update(newNote)
                }
                // Collect notes again after insert
                noteDao.getAllNotes().collectLatest { noteList ->
                    notes = noteList
                }
                currentNote = Note() // Reset to default after saving
            }
        }

        LazyColumn {
            items(notes) { note ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(text = note.title, style = MaterialTheme.typography.bodyLarge)
                    Text(text = note.content, style = MaterialTheme.typography.bodyMedium) // Display the full note content
                    Text(text = note.date, style = MaterialTheme.typography.bodySmall)
                    Text(text = note.category, style = MaterialTheme.typography.bodySmall) // Display the note category
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            // Populate fields with the selected notes detail
                            currentNote = note
                        }) {
                            Text(text = "Edit")
                        }
                        Button(onClick = {
                            scope.launch {
                                noteDao.delete(note)
                                // Collect notes again
                                noteDao.getAllNotes().collectLatest { noteList ->
                                    notes = noteList
                                }
                            }
                        }) {
                            Text(text = "Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteForm(note: Note, onSave: (Note) -> Unit) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    var date by remember { mutableStateOf(note.date) }
    val category = note.category

    // Split content into relevant fields based on its category
    val splitContent = content.split(" - ")
    var field1 by remember { mutableStateOf(if (splitContent.size > 0) splitContent[0] else "") }
    var field2 by remember { mutableStateOf(if (splitContent.size > 1) splitContent[1] else "") }

    // Update state when thje note changes
    LaunchedEffect(note) {
        title = note.title
        content = note.content
        date = note.date
        field1 = if (splitContent.size > 0) splitContent[0] else ""
        field2 = if (splitContent.size > 1) splitContent[1] else ""
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Title") }
        )
        if (category == CATEGORY_TV_SHOW) {
            TextField(
                value = field1,
                onValueChange = { field1 = it; content = "$field1 - $field2" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Episode") }
            )
            TextField(
                value = field2,
                onValueChange = { field2 = it; content = "$field1 - $field2" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Major Plot Point") }
            )
        } else if (category == CATEGORY_GAME) {
            TextField(
                value = field1,
                onValueChange = { field1 = it; content = "$field1 - $field2" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Goal") }
            )
            TextField(
                value = field2,
                onValueChange = { field2 = it; content = "$field1 - $field2" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Description") }
            )
        }
        TextField(
            value = date,
            onValueChange = { date = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Date") }
        )
        TextField(
            value = category,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Category") }
        )
        Button(
            onClick = {
                if (title.isNotEmpty() && content.isNotEmpty() && date.isNotEmpty()) {
                    onSave(note.copy(title = title, content = content, date = date, category = category))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(text = "Save Note")
        }
    }
}

//@Preview(showBackground = true)
@Composable
fun DatabaseScreenPreview() {
    DatabaseScreen(onBackPressed = {})
}
