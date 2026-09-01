package com.github.e227474.androidtoolbox

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.widget.FrameLayout

class HtmlViewerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isDarkTheme =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES

        val colorScheme = getMaterialColorScheme(isDarkTheme)

        val htmlContent = intent.getStringExtra(EXTRA_HTML_CONTENT)
            ?: ""

        val escapedHtml = TextUtils.htmlEncode(htmlContent)

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

            loadDataWithBaseURL(
                null,
                sourcePage,
                "text/html",
                "UTF-8",
                null
            )
        }

        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(colorScheme.background.toArgb())

            addView(
                webView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                0,
                systemBars.top,
                0,
                systemBars.bottom
            )

            insets
        }

        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = colorScheme.background.toArgb()

        val insetsController = WindowInsetsControllerCompat(
            window,
            window.decorView
        )

        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme

        setContentView(rootLayout)

        ViewCompat.requestApplyInsets(rootLayout)

    }

    private fun getMaterialColorScheme(
        isDarkTheme: Boolean
    ): ColorScheme {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDarkTheme) {
                dynamicDarkColorScheme(this)
            } else {
                dynamicLightColorScheme(this)
            }
        } else {
            if (isDarkTheme) {
                darkColorScheme()
            } else {
                lightColorScheme()
            }
        }
    }

    private fun createSourcePage(
        htmlContent: String,
        colorScheme: ColorScheme
    ): String {
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
                <meta name="viewport"
                      content="width=device-width, initial-scale=1.0">

                <style>
                    * {
                        box-sizing: border-box;
                    }

                    html, body {
                        margin: 0;
                        padding: 0;
                        background: $background;
                        color: $onBackground;
                    }

                    body {
                        padding: 20px 16px;
                        font-family: sans-serif;
                    }

                    h1 {
                        margin: 0 0 16px 4px;
                        color: $onBackground;
                        font-size: 22px;
                        font-weight: 600;
                    }

                    .source-card {
                        background: $surface;
                        border: 1px solid $outline;
                        border-radius: 20px;
                        padding: 16px;
                    }

                    .source-label {
                        margin-bottom: 12px;
                        color: $primary;
                        font-size: 13px;
                        font-weight: 600;
                        letter-spacing: 0.5px;
                    }

                    pre {
                        margin: 0;
                        padding: 16px;
                        background: $surfaceContainer;
                        border-radius: 12px;
                        color: $onSurfaceVariant;
                        white-space: pre-wrap;
                        overflow-wrap: break-word;
                        font-family: monospace;
                        font-size: 13px;
                        line-height: 1.5;
                    }

                    code {
                        font-family: monospace;
                    }
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

        val red = AndroidColor.red(argb)
        val green = AndroidColor.green(argb)
        val blue = AndroidColor.blue(argb)

        return String.format("#%02X%02X%02X", red, green, blue)
    }

    companion object {
        const val EXTRA_HTML_CONTENT =
            "com.github.e227474.androidtoolbox.EXTRA_HTML_CONTENT"
    }
}