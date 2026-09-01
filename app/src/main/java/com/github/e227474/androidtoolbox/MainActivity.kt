package com.github.e227474.androidtoolbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row // add necessary dependencies *
import androidx.compose.material3.ElevatedButton // *
import androidx.compose.ui.unit.dp // *
import androidx.compose.foundation.layout.Column //*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme // *
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface // *
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.e227474.androidtoolbox.ui.theme.AndroidToolboxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidToolboxTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column() {
            Row(modifier = Modifier.padding(24.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Get Html from source url specified below.")
                }
                ElevatedButton(
                    colors = ButtonDefaults.buttonColors(),
                    onClick = { /* TODO */ }
                ) {
                    Text("Get HTML")
                }
            }
            Row(modifier = Modifier.padding(24.dp)) {
                OutlinedTextField(
                    state = rememberTextFieldState(),
                    label = { Text("Url") }
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidToolboxTheme {
        Greeting()
    }
}