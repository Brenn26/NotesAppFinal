package com.example.afinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class DatabaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DatabaseScreen()
        }
    }
}

@Composable
fun DatabaseScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Database Activity", style = MaterialTheme.typography.headlineMedium)

        // Placeholder for future database operations
        Text("This is where Ill handle saving and loading notes.")

        Button(
            onClick = { /* TODO: Implement database save/load operations */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Placeholder Save Button")
        }
    }
}
