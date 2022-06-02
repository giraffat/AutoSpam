import androidx.compose.animation.Crossfade
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

class MainView {
    private val viewModel = MainViewModel()
    private var isWorking by mutableStateOf(false)

    private var isIntervalError by mutableStateOf(false)
    private var isMaxTimesError by mutableStateOf(false)

    @Composable
    private fun SpamOptionsTextFields() {
        @Composable
        fun RowScope.IntervalTextField() = TextField(modifier = Modifier.weight(1f),
            label = { Text("间隙（秒）*") },
            isError = isIntervalError,
            enabled = viewModel.isTextFieldsEnabled,
            maxLines = 1,
            value = viewModel.inputInterval,
            onValueChange = { viewModel.inputInterval = it })

        @Composable
        fun RowScope.MaxTimesTextField() = TextField(modifier = Modifier.weight(1f),
            label = { Text("刷屏次数") },
            isError = isMaxTimesError,
            enabled = viewModel.isTextFieldsEnabled,
            maxLines = 1,
            value = viewModel.inputMaxTimes,
            onValueChange = { viewModel.inputMaxTimes = it })

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IntervalTextField()
            MaxTimesTextField()
        }
    }

    @Composable
    private fun ColumnScope.SpamTextTextField() = TextField(modifier = Modifier.weight(1f).fillMaxWidth(),
        label = { Text("刷屏内容") },
        enabled = viewModel.isTextFieldsEnabled,
        value = viewModel.inputSpamText,
        onValueChange = { viewModel.inputSpamText = it })

    @Composable
    private fun StateCard() = Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
        @Composable
        fun StateText() = Text(
            modifier = Modifier.padding(8.dp),
            text = viewModel.stateText
        )

        Column {
            StateText()
            if (viewModel.isLimitedSpamming == true || viewModel.isLimitedSpamming == null) {
                LinearProgressIndicator(progress = viewModel.progress, modifier = Modifier.fillMaxWidth())
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }

    @Composable
    private fun ControllerButtons() {
        @Composable
        fun RowScope.ControlButton() = Button(
            modifier = Modifier.weight(1f),
            onClick = { isWorking = !isWorking }
        ) {
            Crossfade(isWorking) {
                Text(if (isWorking) "取消" else "启动")
            }
        }

        @Composable
        fun OpenSummaryButton() = Button(onClick = viewModel::openSummaryInBrowser) {
            Text("介绍")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ControlButton()
            OpenSummaryButton()
        }
    }

    @Composable
    @Preview
    fun App() {
        if (isWorking) {
            LaunchedEffect(isWorking) {
                viewModel.start()
            }
        }

        MaterialTheme {
            Column(modifier = Modifier.background(Color(0xffeceff1))) {
                Divider(modifier = Modifier.shadow(2.dp))

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SpamOptionsTextFields()
                    SpamTextTextField()
                    StateCard()
                    ControllerButtons()
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
        icon = painterResource("icon.svg"),
        state = WindowState(height = 600.dp, width = 500.dp)
    ) {
        MainView().App()
    }
}