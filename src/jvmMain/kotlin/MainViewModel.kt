import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dataClasses.*
import dataClasses.AnimationStates.*
import dataClasses.States.UnlimitedSpamming
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI

class MainViewModel {
    private val model = MainModel()
    private lateinit var workingJob: Job
    private val mainScope = MainScope()

    val completedTimes get() = model.completedTimes

    var spamOptionsString by mutableStateOf(SpamOptionsString())
    var uiStates by mutableStateOf(UiStates())
        private set
    var errors by mutableStateOf(Errors())
        private set

    private fun interval() = model.convertIntervalString(spamOptionsString.intervalString)

    private fun maxTimes() = model.convertMaxTimesString(spamOptionsString.maxTimesString)

    fun spamOptions() = SpamOptions(interval(), maxTimes())

    private fun textInput() {
        val isIntervalError = try {
            interval()
            false
        } catch (e: NumberFormatException) {
            true
        }
        val isMaxTimesError =
            if (spamOptionsString.maxTimesString == "")
                false else
                try {
                    maxTimes()
                    false
                } catch (e: NumberFormatException) {
                    true
                }

        errors = errors.copy(isIntervalError = isIntervalError, isMaxTimesError = isMaxTimesError)
    }

    private suspend fun resetProgress() {
        uiStates = uiStates.copy(animationState = ResettingProgress, isProgressFull = false)
        delay(MainResources.resettingProgressDuration.toLong())
    }

    fun cancel() {
        mainScope.launch {
            workingJob.cancel()
            uiStates = UiStates(animationState = ResettingProgress)
        }
    }

    private suspend fun wait() {
        uiStates = uiStates.copy(
            state = States.Waiting,
            isCanceledButton = true,
            animationState = Waiting,
            isProgressFull = true
        )
        delay(MainResources.waitingDuration.toLong())
        resetProgress()
    }

    private suspend fun spam() {
        val interval = interval()

        if (spamOptionsString.spamText != "") {
            model.copy(spamOptionsString.spamText)
        }
        if (spamOptionsString.maxTimesString != "") {
            val maxTime = model.convertMaxTimesString(spamOptionsString.maxTimesString)

            uiStates = uiStates.copy(state = States.Spamming, animationState = Spamming, isProgressFull = true)
            model.spam(interval, maxTime)

            uiStates = UiStates(animationState = ResettingProgress, isTextFieldsEnabled = true)
        } else {
            uiStates = uiStates.copy(state = UnlimitedSpamming, isUnlimitedSpamming = true)
            model.spam(interval)
        }
    }

    fun start() {
        workingJob = mainScope.launch {
            textInput()
            if (errors.isIntervalError || errors.isMaxTimesError) {
                return@launch
            }

            uiStates = uiStates.copy(isTextFieldsEnabled = false)

            wait()
            spam()
        }
        mainScope.launch {
            model.checkMousePosition(cancel = ::cancel)
        }
    }

    fun openSummaryInBrowser(){
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(MainResources.summaryUrl))
        }
    }
}