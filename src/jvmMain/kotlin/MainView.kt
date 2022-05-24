import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

object MainView {
    @Composable
    @Preview
    fun App() {
        MaterialTheme {
            Column {
                Divider(thickness = 2.dp)

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val viewModel = remember { MainViewModel() }

                    MainViewWidgets.SpamOptionsSetter(
                        modifier = Modifier.weight(1f),
                        errors = viewModel.errors,
                        spamOptionsString = viewModel.spamOptionsString,
                        onSpamOptionsStringChange = { viewModel.spamOptionsString = it })
                    MainViewWidgets.SpamController(
                        isUnlimitedSpamming = viewModel.isUnlimitedSpamming,
                        uiStates = viewModel.uiStates,
                        getSpamOptions = viewModel::spamOptions,
                        completedTimes = viewModel.completedTimes,
                        onStartButtonClick = viewModel::start,
                        onCancelButtonClick = viewModel::cancel
                    )
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        alwaysOnTop = true,
        resizable = false,
        title = "自动刷屏",
        state = WindowState(height = 500.dp, width = 500.dp)
    ) {
        MainView.App()
    }
}