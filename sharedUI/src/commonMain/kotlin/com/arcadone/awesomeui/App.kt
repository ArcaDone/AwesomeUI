package com.arcadone.awesomeui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import awesome_ui.sharedui.generated.resources.IndieFlower_Regular
import awesome_ui.sharedui.generated.resources.Res
import awesome_ui.sharedui.generated.resources.awesome_ui
import awesome_ui.sharedui.generated.resources.ic_dark_mode
import awesome_ui.sharedui.generated.resources.ic_light_mode
import awesome_ui.sharedui.generated.resources.theme
import com.arcadone.awesomeui.theme.AppTheme
import com.arcadone.awesomeui.theme.LocalThemeIsDark
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun App(onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}) = AppTheme(onThemeChanged) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Text(
                        text = stringResource(Res.string.awesome_ui),
                        fontFamily = FontFamily(Font(Res.font.IndieFlower_Regular)),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                },
                actions = {
                    var isDark by LocalThemeIsDark.current
                    val icon = remember(isDark) {
                        if (isDark) {
                            Res.drawable.ic_light_mode
                        } else {
                            Res.drawable.ic_dark_mode
                        }
                    }

                    ElevatedButton(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            .widthIn(min = 200.dp),
                        onClick = { isDark = !isDark },
                        content = {
                            Icon(vectorResource(icon), contentDescription = null)
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(Res.string.theme))
                        },
                    )
                },
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            }
        },
    )
}
