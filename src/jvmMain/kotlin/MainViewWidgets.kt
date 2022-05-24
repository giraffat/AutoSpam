import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dataClasses.*
import dataClasses.AnimationStates.*
import dataClasses.States.Default
import dataClasses.States.UnlimitedSpamming

object MainViewWidgets {
    @Composable
    fun SpamOptionsSetter(
        modifier: Modifier = Modifier,
        errors: Errors,
        spamOptionsString: SpamOptionsString,
        onSpamOptionsStringChange: (SpamOptionsString) -> Unit
    ) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(modifier = Modifier.weight(1f),
                    label = { Text("间隙（秒）") },
                    isError = errors.isIntervalError,
                    value = spamOptionsString.intervalString,
                    onValueChange = { onSpamOptionsStringChange(spamOptionsString.copy(intervalString = it)) })
                OutlinedTextField(modifier = Modifier.weight(1f),
                    label = { Text("刷屏次数") },
                    isError = errors.isMaxTimesError,
                    value = spamOptionsString.maxTimesString,
                    onValueChange = { onSpamOptionsStringChange(spamOptionsString.copy(maxTimesString = it)) })
            }
            OutlinedTextField(modifier = Modifier.Companion.weight(1f).fillMaxWidth(),
                label = { Text("刷屏内容") },
                value = spamOptionsString.spamText,
                onValueChange = { onSpamOptionsStringChange(spamOptionsString.copy(spamText = it)) })
        }
    }

    @Composable
    fun SpamController(
        modifier: Modifier = Modifier,
        isUnlimitedSpamming: Boolean,
        uiStates: UiStates,
        getSpamOptions: () -> SpamOptions,
        completedTimes: Long,
        onStartButtonClick: () -> Unit,
        onCancelButtonClick: () -> Unit,
    ) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (isUnlimitedSpamming) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                val animateProgress by animateFloatAsState(
                    if (uiStates.isProgressFull) 1f else 0f, animationSpec = tween(
                        durationMillis = when (uiStates.animationState) {
                            Waiting -> MainResources.waitingDuration
                            ResettingProgress -> MainResources.resettingProgressDuration
                            Spamming -> {
                                val spamOptions = getSpamOptions()
                                (spamOptions.interval * spamOptions.maxTimes).toInt()
                            }
                        }, easing = LinearEasing
                    )
                )
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = animateProgress)
            }
            Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                Crossfade(uiStates.state) {
                    Text(
                        modifier = Modifier.padding(8.dp), text = when (uiStates.state) {
                            Default -> "准备就绪"
                            States.Waiting -> "等待中"
                            States.Spamming -> "刷屏中（第${completedTimes}次）"
                            UnlimitedSpamming -> "刷屏中（第${completedTimes}次，无限模式）"
                        }
                    )
                }
            }


            Button(onClick = if (uiStates.isCanceledButton) onCancelButtonClick else onStartButtonClick) {
                Crossfade(uiStates.isCanceledButton) {
                    if (uiStates.isCanceledButton) {
                        Text("取消")
                    } else {
                        Text("启动")
                    }
                }
            }
        }
    }
}