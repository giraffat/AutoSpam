class MainViewStatesConverter {
    fun isTextFieldsEnabled(state: UiState) = when (state) {
        UiState.Default -> true
        is UiState.LimitedSpamming -> false
        is UiState.Resetting -> false
        is UiState.UnlimitedSpamming -> false
        is UiState.Waiting -> false
    }

    private var lastStateText = ""
    fun stateText(state: UiState): String {
        lastStateText = when (state) {
            UiState.Default -> "准备就绪"
            is UiState.LimitedSpamming -> "刷屏中（已刷屏${state.completeTimes}次）"
            is UiState.Resetting -> lastStateText
            is UiState.UnlimitedSpamming -> "无限刷屏中（已刷屏${state.completeTimes}次）"
            is UiState.Waiting -> "等待中"
        }

        return lastStateText
    }

    fun targetProgress(state: UiState) = when (state) {
        UiState.Default -> 0f
        is UiState.LimitedSpamming -> 1f
        is UiState.Resetting -> 0f
        is UiState.UnlimitedSpamming -> null
        is UiState.Waiting -> 1f
    }

    fun progressAnimationDuration(state: UiState): Int? = when (state) {
        UiState.Default -> null
        is UiState.LimitedSpamming -> state.estimateTime.toInt()
        is UiState.Resetting -> state.time.toInt()
        is UiState.UnlimitedSpamming -> null
        is UiState.Waiting -> state.waitTime.toInt()
    }

    fun isUnlimitedSpamming(state: UiState) = state is UiState.UnlimitedSpamming

    private fun isWorking(state: UiState): Boolean = when (state) {
        UiState.Default -> false
        is UiState.LimitedSpamming -> true
        is UiState.Resetting -> true
        is UiState.UnlimitedSpamming -> true
        is UiState.Waiting -> true
    }

    fun buttonAction(state: UiState,start: () -> Unit, cancel: () -> Unit) = if (isWorking(state)) cancel else start

    fun buttonText(state: UiState) = if (isWorking(state)) "取消" else "启动"
}