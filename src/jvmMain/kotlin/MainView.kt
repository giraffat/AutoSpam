import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

class MainView {
    private val viewModel = MainViewModel()
    private val statesConverter = MainViewStatesConverter()

    @Composable
    private fun SpamOptionsTextFields() {
        @Composable
        fun RowScope.IntervalTextField() {
            Column(modifier = Modifier.weight(1f)) {
                TextField(
                    label = { Text("间隙（秒）*") },
                    isError = viewModel.inputIntervalError != null,
                    enabled = statesConverter.isTextFieldsEnabled(viewModel.state),
                    maxLines = 1,
                    value = viewModel.inputInterval,
                    onValueChange = { viewModel.inputInterval = it })
                Text(
                    modifier = Modifier.padding(16.dp, 4.dp, 16.dp, 0.dp),
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    text = viewModel.inputIntervalError ?: ""
                )
            }
        }


        @Composable
        fun RowScope.MaxTimesTextField() {
            Column(modifier = Modifier.weight(1f)) {
                TextField(
                    label = { Text("刷屏次数") },
                    isError = viewModel.inputMaxTimesError != null,
                    enabled = statesConverter.isTextFieldsEnabled(viewModel.state),
                    maxLines = 1,
                    value = viewModel.inputMaxTimes,
                    onValueChange = { viewModel.inputMaxTimes = it })
                Text(
                    modifier = Modifier.padding(16.dp, 4.dp, 16.dp, 0.dp),
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    text = viewModel.inputMaxTimesError ?: ""
                )
            }

        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp))
        {
            IntervalTextField()
            MaxTimesTextField()
        }
    }

    @Composable
    private fun ColumnScope.SpamTextTextField() {
        TextField(modifier = Modifier.weight(1f).fillMaxWidth(),
            label = { Text("刷屏内容") },
            enabled = statesConverter.isTextFieldsEnabled(viewModel.state),
            value = viewModel.inputSpamText,
            onValueChange = { viewModel.inputSpamText = it })
    }

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
        var workingJob: Job? by remember { mutableStateOf(null) }

        LaunchedEffect(0) {
            MousePositionChecker.checkMousePosition {
                workingJob?.cancel()
            }
        }
        Button(
            onClick = statesConverter.buttonAction(viewModel.state,
                { workingJob = scope.launch { viewModel.start() } },
                { workingJob!!.cancel() })
        ) {
            Crossfade(statesConverter.buttonText(viewModel.state)) {
                Text(statesConverter.buttonText(viewModel.state))
            }
        }
    }

    @Composable
    @Preview
    fun App() {
        val colors = MaterialTheme.colors.copy(
            primary = Color(0xff0d47a1),
            primaryVariant = Color(0xff002171),
            secondary = Color(0xff004d40),
            secondaryVariant = Color(0xff39796b),
            background = Color(0xffeceff1)
        )

        MaterialTheme(colors) {
            Column(modifier = Modifier.background(MaterialTheme.colors.background)) {
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