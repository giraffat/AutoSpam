import AnimationStates.*
import States.Default
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel {
    private val model = MainModel()
    private lateinit var workingJob: Job
    private val mainScope = MainScope()

    var state by mutableStateOf(Default)
    var animationState by mutableStateOf(Waiting)
    var isCanceledButton by mutableStateOf(false)
    var isUnlimitedSpamming by mutableStateOf(false)
    var isProgressFull by mutableStateOf(false)

    var intervalString by mutableStateOf("")
    var maxTimesString by mutableStateOf("")
    var spamText by mutableStateOf("")

    var isIntervalError by mutableStateOf(false)
    var isMaxTimesError by mutableStateOf(false)

    val completedTimes get() = model.completedTimes

    private fun textInput(): Boolean {
        isIntervalError = try {
            false
        } catch (e: NumberFormatException) {
            true
        }
        isMaxTimesError = try {
            false
        } catch (e: NumberFormatException) {
            true
        }

        return !(isIntervalError || isMaxTimesError)
    }

    private suspend fun resetProgress() {
        animationState = ResettingProgress
        isProgressFull = false
        delay(MainResources.resettingProgressDuration.toLong())
    }

    fun cancel() {
        mainScope.launch {
            workingJob.cancel()
            resetProgress()
            state = Default
            isCanceledButton = false
        }
    }

    private suspend fun wait() {
        state = States.Waiting
        isCanceledButton = true
        animationState = Waiting
        isProgressFull = true
        delay(MainResources.waitingDuration.toLong())

        resetProgress()
    }

    private suspend fun spam() {
        val interval = model.convertIntervalString(intervalString)

        if (spamText != "") {
            val maxTime = model.convertMaxTimesString(maxTimesString)

            model.copy(spamText)
            animationState = Spamming
            isProgressFull = true
            model.spam(interval, maxTime)

            resetProgress()
        } else {
            isUnlimitedSpamming = true
            model.spam(interval)
        }
    }

    fun start() {
        workingJob = mainScope.launch {
            if (!textInput()) {
                return@launch
            }

            wait()
            spam()
        }
    }

    fun interval() = model.convertIntervalString(intervalString)

    fun maxTimes() = model.convertMaxTimesString(maxTimesString)
}