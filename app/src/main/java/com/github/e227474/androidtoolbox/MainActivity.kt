package com.github.e227474.androidtoolbox
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.e227474.androidtoolbox.ui.theme.AndroidToolboxTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AndroidToolboxTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    HtmlFetcherScreen(
                        modifier = Modifier.padding(innerPadding),
                        onHtmlFetched = { htmlContent ->
                            openHtmlViewer(htmlContent)
                        }
                    )
                }
            }
        }
    }

    private fun openHtmlViewer(htmlContent: String) {
        val intent = Intent(this, HtmlViewerActivity::class.java).apply {
            putExtra(HtmlViewerActivity.EXTRA_HTML_CONTENT, htmlContent)
        }

        startActivity(intent)
    }
}

@Composable
fun HtmlFetcherScreen(
    modifier: Modifier = Modifier,
    onHtmlFetched: (String) -> Unit = {}
) {
    var url by remember {
        mutableStateOf("")
    }

    val coroutineScope = rememberCoroutineScope()

    var fetching by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.padding(
            vertical = 4.dp,
            horizontal = 8.dp
        )
    ) {
        Column {
            Row(
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Get Html from source url specified below."
                    )
                }

                ElevatedButton(
                    colors = ButtonDefaults.buttonColors(),
                    enabled = !fetching && url.isNotBlank(),
                    onClick = {
                        val enteredUrl = url.trim()

                        fetching = true
                        errorMessage = null

                        coroutineScope.launch {
                            val result = fetchHtmlContent(enteredUrl)

                            fetching = false

                            result
                                .onSuccess { htmlContent ->
                                    onHtmlFetched(htmlContent)
                                }
                                .onFailure { exception ->
                                    errorMessage = exception.message
                                        ?: "Unable to fetch HTML content."
                                }
                        }
                    }
                ) {
                    Text(
                        text = if (fetching) {
                            "Loading..."
                        } else {
                            "Get HTML"
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.padding(24.dp)
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { newValue ->
                        url = newValue
                    },
                    placeholder = {
                        Text("https://example.com")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Url")
                    },
                    singleLine = true
                )
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp
                    )
                )
            }
        }
    }
}


private suspend fun fetchHtmlContent(
    urlString: String
): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
        val normalizedUrl = if (
            urlString.startsWith("http://") ||
            urlString.startsWith("https://")
        ) {
            urlString
        } else {
            "https://$urlString"
        }

        val parsedUrl = URL(normalizedUrl)

        require(
            parsedUrl.protocol == "http" ||
                    parsedUrl.protocol == "https"
        ) {
            "Only HTTP and HTTPS URLs are supported."
        }

        val connection = parsedUrl.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = true

            connection.connect()

            val responseCode = connection.responseCode

            require(responseCode in 200..299) {
                "HTTP request failed with status $responseCode"
            }

            connection.inputStream
                .bufferedReader()
                .use { reader ->
                    reader.readText()
                }
        } finally {
            connection.disconnect()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HtmlFetcherScreenPreview() {
    AndroidToolboxTheme {
        HtmlFetcherScreen()
    }
}
