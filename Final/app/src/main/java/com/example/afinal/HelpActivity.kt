package com.example.afinal

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.compose.ui.graphics.Color

class HelpActivity : ComponentActivity() {
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
                HelpScreen()
            }
        }
    }
}

@Composable
fun HelpScreen() {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(16.dp)
    ) {
        Text("About", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Create a note to keep your place in your recent play through or watch through of a series. See preferences for a dark mode!")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { (context as? Activity)?.finish() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
