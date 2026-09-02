package com.github.e227474.androidtoolbox

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.text.htmlEncode
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HtmlViewerActivity : ComponentActivity() {

    private var htmlContent: String = ""
    private var pendingSaveContent: String? = null

    private val createDocumentLauncher =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument("*/*")
        ) { uri: Uri? ->
            if (uri != null) {
                val content = pendingSaveContent ?: return@registerForActivityResult

                lifecycleScope.launch(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri)?.bufferedWriter()
                        .use { writer ->
                            writer?.write(content)
                        }
                }
            }
            pendingSaveContent = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        htmlContent = intent.getStringExtra(EXTRA_HTML_CONTENT) ?: ""

        val isDarkTheme =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES

        val colorScheme = getMaterialColorScheme(isDarkTheme)
        val escapedHtml = htmlContent.htmlEncode()

        val sourcePage = createSourcePage(
            htmlContent = escapedHtml,
            colorScheme = colorScheme
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val webView = WebView(this).apply {
            setBackgroundColor(colorScheme.background.toArgb())
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = false
            settings.domStorageEnabled = false
            loadDataWithBaseURL(null, sourcePage, "text/html", "UTF-8", null)
        }

        val composeOverlay = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(colorScheme = colorScheme) {
                    HtmlViewerFabMenu(
                        onSaveAsTxt = { saveAsTxt() },
                        onSaveAsHtml = { saveAsHtml() },
                        onShare = { shareHtml() }
                    )
                }
            }
        }

        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(colorScheme.background.toArgb())
            addView(webView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            addView(composeOverlay, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        }

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme

        setContentView(rootLayout)
        ViewCompat.requestApplyInsets(rootLayout)
    }

    private fun saveAsTxt() {
        pendingSaveContent = htmlContent
        createDocumentLauncher.launch("html-source.txt")
    }

    private fun saveAsHtml() {
        pendingSaveContent = htmlContent
        createDocumentLauncher.launch("html-source.html")
    }

    private fun shareHtml() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_SUBJECT, "HTML source")
            putExtra(Intent.EXTRA_TEXT, htmlContent)
        }
        startActivity(Intent.createChooser(shareIntent, "Share HTML source"))
    }

    private fun getMaterialColorScheme(isDarkTheme: Boolean): ColorScheme {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDarkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
        } else {
            if (isDarkTheme) darkColorScheme() else lightColorScheme()
        }
    }

    private fun createSourcePage(htmlContent: String, colorScheme: ColorScheme): String {
        val background = colorScheme.background.toCssColor()
        val surface = colorScheme.surface.toCssColor()
        val surfaceContainer = colorScheme.surfaceContainer.toCssColor()
        val primary = colorScheme.primary.toCssColor()
        val onBackground = colorScheme.onBackground.toCssColor()
        val onSurfaceVariant = colorScheme.onSurfaceVariant.toCssColor()
        val outline = colorScheme.outline.toCssColor()

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * { box-sizing: border-box; }
                    html, body { margin: 0; padding: 0; background: $background; color: $onBackground; }
                    body { padding: 20px 16px; font-family: sans-serif; }
                    h1 { margin: 0 0 16px 4px; color: $onBackground; font-size: 22px; font-weight: 600; }
                    .source-card { background: $surface; border: 1px solid $outline; border-radius: 20px; padding: 16px; }
                    .source-label { margin-bottom: 12px; color: $primary; font-size: 13px; font-weight: 600; letter-spacing: 0.5px; }
                    pre { margin: 0; padding: 16px; background: $surfaceContainer; border-radius: 12px; color: $onSurfaceVariant; white-space: pre-wrap; overflow-wrap: break-word; font-family: monospace; font-size: 13px; line-height: 1.5; }
                    code { font-family: monospace; }
                </style>
            </head>
            <body>
                <h1>HTML Source</h1>
                <div class="source-card">
                    <div class="source-label">SOURCE CODE</div>
                    <pre><code>$htmlContent</code></pre>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun Color.toCssColor(): String {
        val argb = toArgb()
        return String.format("#%02X%02X%02X", AndroidColor.red(argb), AndroidColor.green(argb), AndroidColor.blue(argb))
    }

    companion object {
        const val EXTRA_HTML_CONTENT = "com.github.e227474.androidtoolbox.EXTRA_HTML_CONTENT"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HtmlViewerFabMenu(
    onSaveAsTxt: () -> Unit,
    onSaveAsHtml: () -> Unit,
    onShare: () -> Unit
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    val cornerRadius by animateDpAsState(
        targetValue = if (isMenuOpen) 28.dp else 16.dp,
        label = "FAB corner"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Dismiss Layer: Only active when menu is open
        if (isMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        // removes the ripple effect from the background click
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) {
                        isMenuOpen = false
                    }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // ... keep the rest of your AnimatedVisibility and FloatingActionButton code exactly the same

            AnimatedVisibility(
                visible = isMenuOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Column {
                        MenuOptionItem(Icons.AutoMirrored.Filled.TextSnippet, "Save as Plaintext") {
                            isMenuOpen = false
                            onSaveAsTxt()
                        }
                        MenuOptionItem(Icons.Default.Html, "Save as HTML") {
                            isMenuOpen = false
                            onSaveAsHtml()
                        }
                        MenuOptionItem(Icons.Default.Share, "Share") {
                            isMenuOpen = false
                            onShare()
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { isMenuOpen = !isMenuOpen },
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(cornerRadius)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = if (isMenuOpen) "Close menu" else "Open menu"
                )
            }
        }
    }
}

@Composable
private fun MenuOptionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// the code below is AI generated, I had no idea how I could make the preview work while not glitching since the WebView content is non-existent in the compose preview in Android Studio.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun HtmlViewerFabMenuPreview() {
    MaterialTheme {
        Box(modifier = Modifier.size(360.dp, 640.dp)) {
            HtmlViewerFabMenu(
                onSaveAsTxt = {},
                onSaveAsHtml = {},
                onShare = {}
            )
        }
    }
}
