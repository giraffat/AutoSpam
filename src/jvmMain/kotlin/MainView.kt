import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

class MainView {
    private val viewModel = MainViewModel()
    private val statesConverter = MainViewStatesConverter()

    @Composable
    private fun SpamOptionsTextFields() {
        @Composable
        fun RowScope.IntervalTextField() = TextField(
            modifier = Modifier.weight(1f),
            label = { Text("间隙（秒）*") },
            isError = viewModel.inputIntervalError != null,
            enabled = statesConverter.isTextFieldsEnabled(viewModel.state),
            maxLines = 1,
            value = viewModel.inputInterval,
            onValueChange = { viewModel.inputInterval = it })

        @Composable
        fun RowScope.MaxTimesTextField() = TextField(modifier = Modifier.weight(1f),
            label = { Text("刷屏次数") },
            isError = viewModel.inputMaxTimesError != null,
            enabled = statesConverter.isTextFieldsEnabled(viewModel.state),
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
        enabled = statesConverter.isTextFieldsEnabled(viewModel.state),
        value = viewModel.inputSpamText,
        onValueChange = { viewModel.inputSpamText = it })

    @Composable
    private fun StateCard() = Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
        @Composable
        fun StateText() = Text(
            modifier = Modifier.padding(8.dp),
            text = statesConverter.stateText(viewModel.state)
        )

        Column {
            StateText()
            if (statesConverter.isUnlimitedSpamming(viewModel.state)) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                val progress by animateFloatAsState(
                    statesConverter.targetProgress(viewModel.state)!!,
                    animationSpec = tween(
                        statesConverter.progressAnimationDuration(viewModel.state) ?: 0,
                        easing = LinearEasing
                    )
                )
                LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    @Composable
    private fun ControllerButtons() {
        val scope = rememberCoroutineScope()
        var workingJob: Job? = null

        @Composable
        fun RowScope.ControlButton() = Button(
            modifier = Modifier.weight(1f),
            onClick = statesConverter.buttonAction(viewModel.state,
                { workingJob = scope.launch { viewModel.start() } },
                { workingJob!!.cancel() })
        ) {
            Crossfade(statesConverter.buttonText(viewModel.state)) {
                Text(statesConverter.buttonText(viewModel.state))
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