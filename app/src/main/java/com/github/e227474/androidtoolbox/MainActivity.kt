package com.github.e227474.androidtoolbox

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.e227474.androidtoolbox.ui.theme.AndroidToolboxTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import androidx.compose.ui.platform.LocalLocale

// Define Nav routes
sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Curl : Screen(
        route = "curl",
        label = "curl",
        icon = { Icon(Icons.Default.Download, contentDescription = null) }
    )
    object Calculator : Screen(
        route = "calculator",
        label = "Calculator",
        icon = { Icon(Icons.Default.DateRange, contentDescription = null) }
    )
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AndroidToolboxTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                val navController = rememberNavController()

                AdaptiveAppContainer(
                    windowSizeClass = windowSizeClass,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun AdaptiveAppContainer(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController
) {
    val items = listOf(Screen.Curl, Screen.Calculator)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val useRail = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    if (useRail) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(modifier = Modifier.fillMaxHeight()) {
                items.forEach { screen ->
                    NavigationRailItem(
                        selected = currentRoute == screen.route,
                        onClick = { navController.navigate(screen.route) { launchSingleTop = true } },
                        icon = screen.icon,
                        label = { Text(screen.label) }
                    )
                }
            }
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                NavigationGraph(navController, Modifier.padding(innerPadding))
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = { navController.navigate(screen.route) { launchSingleTop = true } },
                            icon = screen.icon,
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavigationGraph(navController, Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Curl.route,
        modifier = modifier
    ) {
        composable(Screen.Curl.route) {
            val context = LocalContext.current
            HtmlFetcherScreen(
                onHtmlFetched = { htmlContent ->
                    val intent = Intent(context, HtmlViewerActivity::class.java).apply {
                        putExtra(HtmlViewerActivity.EXTRA_HTML_CONTENT, htmlContent)
                    }
                    context.startActivity(intent)
                }
            )
        }
        composable(Screen.Calculator.route) {
            DateTimeCalculatorScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeCalculatorScreen() {
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val startDateState = rememberDatePickerState()
    val endDateState = rememberDatePickerState()

    val daysDifference = remember(startDate, endDate) {
        if (startDate != null && endDate != null) {
            val start = Instant.ofEpochMilli(startDate!!).atZone(ZoneId.systemDefault()).toLocalDate()
            val end = Instant.ofEpochMilli(endDate!!).atZone(ZoneId.systemDefault()).toLocalDate()
            ChronoUnit.DAYS.between(start, end).absoluteValue
        } else {
            null
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Date Difference Calculator",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            DateInputRow(
                label = "Start Date",
                selectedDate = startDate,
                onPickerRequest = { showStartPicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DateInputRow(
                label = "End Date",
                selectedDate = endDate,
                onPickerRequest = { showEndPicker = true }
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (daysDifference != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Difference",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$daysDifference Days",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Text(
                    text = "Select both dates to see the difference",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = startDateState.selectedDateMillis
                    showStartPicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = startDateState)
        }
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDate = endDateState.selectedDateMillis
                    showEndPicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = endDateState)
        }
    }
}

@Composable
fun DateInputRow(
    label: String,
    selectedDate: Long?,
    onPickerRequest: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = if (selectedDate == null) "" else {
                java.text.SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale)
                    .format(java.util.Date(selectedDate))
            },
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPickerRequest() },
            trailingIcon = {
                IconButton(onClick = onPickerRequest) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Open Calendar")
                }
            },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun HtmlFetcherScreen(
    modifier: Modifier = Modifier,
    onHtmlFetched: (String) -> Unit = {}
) {
    var url by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var fetching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column {
            Row(modifier = Modifier.padding(24.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Get HTML from source URL specified below.")
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
                            result.onSuccess { htmlContent ->
                                onHtmlFetched(htmlContent)
                            }.onFailure { exception ->
                                errorMessage = exception.message ?: "Unable to fetch HTML content."
                            }
                        }
                    }
                ) {
                    Text(text = if (fetching) "Loading..." else "Get HTML")
                }
            }

            Row(modifier = Modifier.padding(24.dp)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("https://example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("URL") },
                    singleLine = true,
                    trailingIcon = {
                        if (url.isNotEmpty()) {
                            IconButton(onClick = { url = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear text")
                            }
                        }
                    }
                )
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp)
                )
            }
        }
    }
}

private suspend fun fetchHtmlContent(urlString: String): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
        val normalizedUrl = if (urlString.startsWith("http://") || urlString.startsWith("https://")) {
            urlString
        } else {
            "https://$urlString"
        }

        val parsedUrl = URL(normalizedUrl)
        require(parsedUrl.protocol == "http" || parsedUrl.protocol == "https") {
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
            require(responseCode in 200..299) { "HTTP request failed with status $responseCode" }

            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
