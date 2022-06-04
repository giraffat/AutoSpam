import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

sealed class UiState {
    object Default : UiState()
    data class Waiting(val waitTime: Long) : UiState()

    data class LimitedSpamming(val completeTimes: Long, val estimateTime: Long) : UiState()

    data class UnlimitedSpamming(val completeTimes: Long) : UiState()

    data class Resetting(val time: Long) : UiState()
}

class MainViewModel {
    var inputInterval: String by mutableStateOf("")
    var inputMaxTimes: String by mutableStateOf("")
    var inputSpamText: String by mutableStateOf("")

    var state: UiState by mutableStateOf(UiState.Default)
        private set

    var inputIntervalError: String? by mutableStateOf(null)
        private set
    var inputMaxTimesError: String? by mutableStateOf(null)
        private set

    private suspend fun resetProgress() {
        state = UiState.Resetting(250)
        delay(250)
    }

    private fun resetState() {
        state = UiState.Default
        println()
    }

    private suspend fun wait() {
        state = UiState.Waiting(4000)
        delay(4000)
    }

    data class SpamOptions(val interval: Long?, val maxTimes: Int?)

    private fun spamOptions(): SpamOptions {
        var interval: Long? = null
        var maxTimes: Int? = null

        try {
            interval = InputSpamOptionsConverter.interval(inputInterval)
        } catch (e: NumberFormatException) {
            inputIntervalError = e.message
        } catch (e: InputSpamOptionsConverter.InputIsEmptyException) {
            inputIntervalError = "输入不能为空"
        }

        try {
            maxTimes = InputSpamOptionsConverter.maxTimes(inputMaxTimes)
        } catch (e: NumberFormatException) {
            inputMaxTimesError = e.message
        } catch (e: InputSpamOptionsConverter.InputIsEmptyException) {
//            inputMaxTimesError = "输入不能为空"
        }

        return SpamOptions(interval, maxTimes)
    }

    private suspend fun spam(interval: Long, maxTimes: Int) {
        state = UiState.LimitedSpamming(0, interval * maxTimes)
        SpamHelper.spam(interval, maxTimes) {
            state = UiState.LimitedSpamming(it.toLong(), interval * maxTimes)
        }
    }

    private suspend fun spam(interval: Long) {
        state = UiState.UnlimitedSpamming(0)

        SpamHelper.spam(interval) {
            state = UiState.UnlimitedSpamming(it.toLong())
        }
    }

    suspend fun start() {
        fun resetErrors() {
            inputIntervalError = null
            inputMaxTimesError = null
        }

        fun cancelIfError() {
            if (inputIntervalError != null || inputMaxTimesError != null) {
                throw CancellationException()
            }
        }

        fun copyIfInputSpamTextIsNotEmpty() {
            try {
                SpamHelper.copy(InputSpamOptionsConverter.spamText(inputSpamText))
            } catch (_: InputSpamOptionsConverter.InputIsEmptyException) {
            }
        }

        resetErrors()
        val (interval, maxTimes) = spamOptions()
        cancelIfError()

        try {
            wait()
            resetProgress()
            copyIfInputSpamTextIsNotEmpty()
            if (maxTimes != null) {
                spam(interval!!, maxTimes)
            } else {
                spam(interval!!)
            }
            resetProgress()
            resetState()
        } catch (e: CancellationException) {
            withContext(NonCancellable) {
                resetProgress()
                resetState()
            }
        }
    }
}