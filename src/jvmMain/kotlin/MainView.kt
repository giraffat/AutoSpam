import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.*

// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

@OptIn(DelicateCoroutinesApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        Column {
            Divider()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(12.dp)) {
                val viewModel = remember { MainViewModel() }
                var workingJob: Job? = null

                val waitTime = 4000
                val resetProgressTime = 250
                val waitingText = "等待中"
                val cancelText = "取消"

                var progress: Float? by remember { mutableStateOf(0f) }
                var animateDuration by remember { mutableStateOf(0) }
                var state by remember { mutableStateOf("准备就绪") }
                var buttonText by remember { mutableStateOf("启动") }

                var isIntervalError by remember { mutableStateOf(false) }
                var isMaxTimesError by remember { mutableStateOf(false) }

                var intervalString by remember { mutableStateOf("") }
                var maxTimesString by remember { mutableStateOf("") }
                var spamText by remember { mutableStateOf("") }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        label = { Text("间隙（秒）") },
                        isError = isIntervalError,
                        value = intervalString,
                        onValueChange = { intervalString = it })
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        label = { Text("刷屏次数") },
                        isError = isMaxTimesError,
                        value = maxTimesString,
                        onValueChange = { maxTimesString = it })
                }
                OutlinedTextField(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    label = { Text("刷屏内容") },
                    value = spamText,
                    onValueChange = { spamText = it })
                if (progress == null) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    val animateProgress by animateFloatAsState(
                        progress!!,
                        animationSpec = tween(durationMillis = animateDuration, easing = LinearEasing)
                    )
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = animateProgress)
                }
                Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                    Text(modifier = Modifier.padding(8.dp), text = state)
                }
                Button(onClick = {
                    var interval = 0L
                    var maxTimes = 0L
                    try {
                        interval = viewModel.convertIntervalString(intervalString)
                        isIntervalError = false
                    } catch (e: NumberFormatException) {
                        isIntervalError = true
                    }
                    try {
                        maxTimes = viewModel.convertMaxTimesString(maxTimesString)
                        isMaxTimesError = false
                    } catch (e: NumberFormatException) {
                        isMaxTimesError = true
                    }
                    if (isIntervalError || isMaxTimesError) {
                        return@Button
                    }

                    if (workingJob == null) {
                        workingJob = GlobalScope.launch {
                            state = waitingText
                            buttonText = cancelText
                            progress = 0f
                            animateDuration = waitTime
                            progress = 1f
                            delay(waitTime.toLong())

                            animateDuration = resetProgressTime
                            progress = 0f
                            delay(resetProgressTime.toLong())

                            if (spamText != "") {
                                viewModel.copy(spamText)
                            }

                            if (maxTimes == 0L) {
                                state = "刷屏中（无限模式，第${viewModel.completedTimes}次）"
                                progress = null
                                viewModel.spam(interval)
                            } else {
                                state = "刷屏中（第${viewModel.completedTimes}次）"
                                animateDuration = (interval * maxTimes).toInt()
                                progress = 1f
                                viewModel.spam(interval, maxTimes)

                                workingJob = null
                                animateDuration = resetProgressTime
                                progress = 0f
                                delay(resetProgressTime.toLong())
                                state = "准备就绪"
                                buttonText = "启动"
                            }
                        }
                    } else {
                        MainScope.launch {
                            workingJob!!.cancel()
                            workingJob = null
                            animateDuration = resetProgressTime
                            progress = 0f
                            delay(resetProgressTime.toLong())
                            state = "准备就绪"
                            buttonText = "启动"
                        }
                    }
                }) {
                    Text(buttonText)
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
        App()
    }
}