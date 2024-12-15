package com.example.afinal

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import android.app.Activity

val Context.dataStore by preferencesDataStore("settings")

class PreferencesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PreferencesScreen()
        }
    }
}

@Composable
fun PreferencesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = context.dataStore

    var isDarkMode by remember { mutableStateOf(false) }

    LaunchedEffect(dataStore) {
        dataStore.data.collect { preferences ->
            isDarkMode = preferences[booleanPreferencesKey("dark_mode")] ?: false
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Preferences", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Dark Mode")
            Switch(
                checked = isDarkMode,
                onCheckedChange = { isChecked ->
                    isDarkMode = isChecked
                    scope.launch {
                        dataStore.edit { preferences ->
                            preferences[booleanPreferencesKey("dark_mode")] = isChecked
                        }
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { (context as? Activity)?.finish() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
