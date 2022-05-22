// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

@OptIn(ExperimentalAnimationApi::class, DelicateCoroutinesApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var spamText by remember { mutableStateOf("") }
            var maxTimes by remember { mutableStateOf(0) }
            var interval by remember { mutableStateOf(0) }
            var status by remember { mutableStateOf(Status.Default) }
            var spamCoroutine: Job? = null;

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    modifier = Modifier.Companion.weight(1f),
                    value = if (interval == 0) "" else interval.toString(),
                    onValueChange = {
                        interval = if (it == "") 0 else try {
                            it.toInt()
                        } catch (_: NumberFormatException) {
                            interval
                        }
                    },
                    label = { Text("间隔") },
                )

                TextField(
                    modifier = Modifier.Companion.weight(1f),
                    value = if (maxTimes == 0) "" else maxTimes.toString(),
                    onValueChange = {
                        maxTimes = if (it == "") 0 else try {
                            it.toInt()
                        } catch (_: NumberFormatException) {
                            maxTimes
                        }
                    },
                    label = { Text("次数") })
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().weight(1f),
                value = spamText,
                onValueChange = { spamText = it },
                label = { Text("内容") })

            val progress by animateFloatAsState(
                if (status != Status.Default) 1f else 0f,
                animationSpec = TweenSpec(durationMillis = 5000)
            )
            LinearProgressIndicator(progress, modifier = Modifier.fillMaxWidth())
            Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                Text("准备就绪", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.subtitle2)
            }

            Button(
                onClick = {
                    if (status == Status.Default) {
                        spamCoroutine = GlobalScope.launch {
                            status = Status.Waiting
                            delay(5000)
                            if (!isActive){
                                return@launch
                            }
                            status = Status.Spamming
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(spamText), null)
                            val robot = Robot()
                            while (status == Status.Spamming && isActive) {
                                if (!isActive){
                                    return@launch
                                }
                                robot.keyPress(KeyEvent.VK_CONTROL)
                                robot.keyPress(KeyEvent.VK_V)
                                robot.keyRelease(KeyEvent.VK_V)
                                robot.keyRelease(KeyEvent.VK_CONTROL)
                                robot.keyPress(KeyEvent.VK_ENTER)
                                robot.keyRelease(KeyEvent.VK_ENTER)
                                delay((interval * 1000).toLong())
                            }
                        }
                    } else {
                        spamCoroutine?.cancel()
                        status = Status.Default
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(status) {
                    Text("启动")
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        alwaysOnTop = true,
//        resizable = false,
        title = "自动刷屏",
        state = WindowState(height = 500.dp, width = 500.dp)
    ) {
        App()
    }
}
