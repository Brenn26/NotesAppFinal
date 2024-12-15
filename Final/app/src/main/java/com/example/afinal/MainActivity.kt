package com.example.afinal

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.runBlocking

// Add colors here for now
val LightColors = lightColorScheme(
    primary = Color(0xFF6200EE),
    primaryContainer = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

val DarkColors = darkColorScheme(
    primary = Color(0xFFBB86FC),
    primaryContainer = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    background = Color(0xEE252424),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(context.dataStore) {
                context.dataStore.data.collect { preferences ->
                    isDarkMode = preferences[booleanPreferencesKey("dark_mode")] ?: false
                }
            }

            val colorScheme = if (isDarkMode) DarkColors else LightColors

            MaterialTheme(colorScheme = colorScheme) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorScheme.background)
                        .padding(16.dp)
                ) {
                    NotesApp()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Button to navigate to DatabaseActivity where user will be able to delete/manage/update notes
                    Button(
                        onClick = {
                            val intent = Intent(this@MainActivity, DatabaseActivity::class.java)
                            startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Go to Database Activity")
                    }

                    // Button to navigate to HelpActivity
                    Button(
                        onClick = {
                            val intent = Intent(this@MainActivity, HelpActivity::class.java)
                            startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Help/About")
                    }

                    // Button to navigate to PreferencesActivity where dark mode will be
                    Button(
                        onClick = {
                            val intent = Intent(this@MainActivity, PreferencesActivity::class.java)
                            startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Preferences")
                    }
                }
            }
        }
    }
}

@Composable
fun NotesApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val noteDao = database.noteDao()
    val scope = rememberCoroutineScope()

    var isCategoryGame by remember { mutableStateOf(false) }
    val notesList = remember { mutableStateListOf<Pair<String, String>>() }

    Column(modifier = Modifier.padding(16.dp)) {
        // The switch for category selection
        CategorySwitch(
            isCategoryGame = isCategoryGame,
            onCategoryToggle = { isCategoryGame = it }
        )

        // If the user selects a TV series, they will state the series name, the episode and a short description of the major plot point, and the date
        if (isCategoryGame) {
            VideoGameNote { gameTitle, goal, description, date ->
                val note = Note(
                    title = gameTitle,
                    content = "$goal - $description",
                    date = date,
                    category = CATEGORY_GAME
                )
                scope.launch {
                    noteDao.insert(note)
                }
                notesList.add(Pair("$gameTitle - $goal - $description - $date", CATEGORY_GAME))
            }
        } else {
            TVSeriesNote { seriesName, episode, plotPoint, date ->
                val note = Note(
                    title = seriesName,
                    content = "$episode - $plotPoint",
                    date = date,
                    category = CATEGORY_TV_SHOW
                )
                scope.launch {
                    noteDao.insert(note)
                }
                notesList.add(Pair("$seriesName - $episode - $plotPoint - $date", CATEGORY_TV_SHOW))
            }
        }

        // List of notes
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            val categories = listOf(CATEGORY_TV_SHOW, CATEGORY_GAME)
            categories.forEach { category ->
                val filteredNotes = notesList.filter { it.second == category }
                if (filteredNotes.isNotEmpty()) {
                    item {
                        Text(
                            text = "$category Notes",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(filteredNotes) { note ->
                        Text(note.first, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TVSeriesNote(
    onSave: (String, String, String, String) -> Unit
) {
    var seriesName by remember { mutableStateOf("") }
    var episode by remember { mutableStateOf("") }
    var plotPoint by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = seriesName,
            onValueChange = { seriesName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Series Name") }
        )
        TextField(
            value = episode,
            onValueChange = { episode = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Episode") }
        )
        TextField(
            value = plotPoint,
            onValueChange = { plotPoint = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Major Plot Point") }
        )
        TextField(
            value = date,
            onValueChange = { date = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date") }
        )
        Button(
            onClick = {
                if (seriesName.isNotEmpty() && episode.isNotEmpty() && plotPoint.isNotEmpty() && date.isNotEmpty()) {
                    onSave(seriesName, episode, plotPoint, date)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Save TV Series Note")
        }
    }
}

@Composable
fun VideoGameNote(
    onSave: (String, String, String, String) -> Unit
) {
    var gameTitle by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = gameTitle,
            onValueChange = { gameTitle = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Game Title") }
        )
        TextField(
            value = goal,
            onValueChange = { goal = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Goal") }
        )
        TextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Description or Note") }
        )
        TextField(
            value = date,
            onValueChange = { date = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date") }
        )
        Button(
            onClick = {
                if (gameTitle.isNotEmpty() && goal.isNotEmpty() && description.isNotEmpty() && date.isNotEmpty()) {
                    onSave(gameTitle, goal, description, date)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Save Video Game Note")
        }
    }
}

@Composable
fun CategorySwitch(
    isCategoryGame: Boolean,
    onCategoryToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = if (isCategoryGame) CATEGORY_GAME else CATEGORY_TV_SHOW)
        Switch(
            checked = isCategoryGame,
            onCheckedChange = onCategoryToggle
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NotesApp()
}
