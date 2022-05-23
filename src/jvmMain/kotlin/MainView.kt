// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import WorkingStates.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        alwaysOnTop = true,
        resizable = false,
        title = "自动刷屏",
        state = WindowState(height = 500.dp, width = 500.dp)
    ) {
        App()
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val waitDelay = 5000L
            val completeDelay = 250L
            val cancelDelay = 250L

            val viewModel = remember { MainViewModel(waitDelay, completeDelay, cancelDelay) }
            var options by remember { mutableStateOf(InputSpamOptions()) }
            var warnings by remember {
                mutableStateOf(
                    InputSpamOptionsWarnings()
                )
            }
            val progress by animateFloatAsState(
                when (viewModel.workingState) {
                    Default, OnWaitedCompleted, OnSpammedCompleted, OnCancelling, OnCancellingByMousePosition -> 0f
                    OnWaiting, OnSpamming -> 1f
                },
                animationSpec = tween(
                    durationMillis = when (viewModel.workingState) {
                        Default, OnWaitedCompleted, OnSpammedCompleted -> completeDelay.toInt()
                        OnCancelling, OnCancellingByMousePosition -> cancelDelay.toInt()
                        OnWaiting -> waitDelay.toInt()
                        OnSpamming -> if (warnings.unlimitedSpamWarning) 0 else (options.interval.toFloat() * options.maxTimes.toFloat() * 1000).toInt()
                    },
                    easing = LinearEasing
                )
            )

            MainViewWidgets.spamOptions(
                modifier = Modifier.weight(1f),
                enabled = viewModel.workingState == Default,
                inputSpamOptions = options,
                warnings = warnings,
                onInputSpamOptionsChange = { options = it }
            )

            MainViewWidgets.spamController(
                Modifier,
                states = viewModel.workingState,
                progress = progress,
                finishTimes = viewModel.finishTimes,
                warnings = warnings,
                controllerButtonEnabled = viewModel.workingState != OnCancelling,
                onControllerButtonClick = {
                    warnings = viewModel.textInput(options)
                    if (warnings.intervalError || warnings.maxTimesError || warnings.spamTextError) {
                        return@spamController
                    }

                    if (viewModel.workingState == Default) {
                        viewModel.work(options)
                    } else {
                        viewModel.cancel()
                    }
                }
            )
        }
    }
}