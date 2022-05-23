import WorkingStates.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object MainViewWidgets {
    @Composable
    fun spamOptions(
        modifier: Modifier = Modifier,
        enabled: Boolean,
        inputSpamOptions: InputSpamOptions,
        warnings: InputSpamOptionsWarnings,
        onInputSpamOptionsChange: (InputSpamOptions) -> Unit
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = inputSpamOptions.interval,
                    enabled = enabled,
                    isError = warnings.intervalError,
                    onValueChange = {
                        onInputSpamOptionsChange(inputSpamOptions.copy(interval = it))
                    },
                    label = { Text("间隔") })

                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    isError = warnings.maxTimesError,
                    value = inputSpamOptions.maxTimes,
                    onValueChange = {
                        onInputSpamOptionsChange(inputSpamOptions.copy(maxTimes = it))
                    },
                    label = { Text("次数") })
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxSize().weight(1f),
                enabled = enabled,
                isError = warnings.spamTextError,
                value = inputSpamOptions.spamText,
                onValueChange = {
                    onInputSpamOptionsChange(inputSpamOptions.copy(spamText = it))
                },
                label = { Text("内容") })
        }
    }

    @Composable
    fun spamController(
        modifier: Modifier = Modifier,
        states: WorkingStates,
        finishTimes: Int,
        progress: Float,
        warnings: InputSpamOptionsWarnings,
        controllerButtonEnabled: Boolean,
        onControllerButtonClick: () -> Unit
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (warnings.unlimitedSpamWarning || states == OnSpamming){
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            else{
                LinearProgressIndicator(progress, modifier = Modifier.fillMaxWidth())
            }
        }

        val spamOptions = mutableListOf<String>()
        if (warnings.emptySpamContentWarning) {
            spamOptions.add("剪贴板模式")
        }
        if (warnings.unlimitedSpamWarning) {
            spamOptions.add("无限模式")
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
            Text(
                text = when (states) {
                    Default -> "准备就绪"
                    OnWaiting, OnWaitedCompleted -> "等待中"
                    OnSpamming, OnSpammedCompleted -> "刷屏中（第${finishTimes}次${
                        if (spamOptions.isNotEmpty()) "，" + spamOptions.joinToString(
                            "，"
                        ) else ""
                    }）"
                    OnCancelling -> "取消中"
                    OnCancellingByMousePosition -> "鼠标位置取消中"
                },
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.subtitle2
            )
        }

        Button(
            onClick = onControllerButtonClick,
            enabled = controllerButtonEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (states == Default) "开始刷屏" else "停止")
        }
    }
}