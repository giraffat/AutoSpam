import AnimationStates.*
import States.Default
import States.UnlimitedSpamming
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

class MainView {
    @Composable
    @Preview
    fun App() {
        MaterialTheme {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val viewModel = remember { MainViewModel() }

                Divider()

                Column(modifier = Modifier.padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(modifier = Modifier.weight(1f),
                            label = { Text("间隙（秒）") },
                            isError = viewModel.isIntervalError,
                            value = viewModel.intervalString,
                            onValueChange = { viewModel.intervalString = it })
                        OutlinedTextField(modifier = Modifier.weight(1f),
                            label = { Text("刷屏次数") },
                            isError = viewModel.isMaxTimesError,
                            value = viewModel.maxTimesString,
                            onValueChange = { viewModel.maxTimesString = it })
                    }
                    OutlinedTextField(modifier = Modifier.weight(1f).fillMaxWidth(),
                        label = { Text("刷屏内容") },
                        value = viewModel.spamText,
                        onValueChange = { viewModel.spamText = it })
                    if (viewModel.isUnlimitedSpamming) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else {
                        val animateProgress by animateFloatAsState(
                            if (viewModel.isProgressFull) 1f else 0f, animationSpec = tween(
                                durationMillis = when (viewModel.animationState) {
                                    Waiting -> MainResources.waitingDuration
                                    ResettingProgress -> MainResources.resettingProgressDuration
                                    Spamming -> {
                                        val interval = viewModel.interval()
                                        val maxTimes = viewModel.maxTimes()
                                        (interval * maxTimes).toInt()
                                    }
                                }, easing = LinearEasing
                            )
                        )
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = animateProgress)
                    }
                    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                        Text(
                            modifier = Modifier.padding(8.dp), text = when (viewModel.state) {
                                Default -> "准备就绪"
                                States.Waiting -> "等待中"
                                States.Spamming -> "刷屏中（第${viewModel.completedTimes}次）"
                                UnlimitedSpamming -> "刷屏中（第${viewModel.completedTimes}次，无限模式"
                            }
                        )
                    }
                    if (viewModel.isCanceledButton) {
                        Button(onClick = viewModel::cancel) {
                            Text("取消")
                        }
                    } else {
                        Button(onClick = viewModel::start) {
                            Text("启动")
                        }
                    }
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
        MainView().App()
    }
}
