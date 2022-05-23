import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

class MainViewModel(private val waitDelay: Long, private val completeDelay: Long, private val cancelDelay: Long) {
    private lateinit var workingJob: Job;

    var workingState by mutableStateOf(WorkingStates.Default)
        private set
    var finishTimes by mutableStateOf(0)
        private set

    fun textInput(options: InputSpamOptions) = InputSpamOptionsWarnings(
        intervalError = try {
            (options.interval.toFloat() * 1000).toLong() < 0
        } catch (e: NumberFormatException) {
            true
        },
        maxTimesError = try {
            options.maxTimes.toInt() < -1 || options.maxTimes.toInt() == 0
        } catch (e: NumberFormatException) {
            true
        },
        unlimitedSpamWarning = options.maxTimes == "",
        emptySpamContentWarning = options.spamText == ""
    )

    private suspend fun beginWait() {
        workingState = WorkingStates.OnWaiting
        delay(waitDelay)
        workingState = WorkingStates.OnWaitedCompleted
        delay(completeDelay)
    }

    private suspend fun beginSpam(options: InputSpamOptions) {
        workingState = WorkingStates.OnSpamming

        val interval = (options.interval.toFloat() * 1000).toLong()
        val maxTimes = options.maxTimes.toInt()
        val robot = Robot()

        if (options.spamText != "") {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(options.spamText), null)
        }

        withContext(Dispatchers.Default) {
            while (finishTimes != maxTimes) {
                if (interval == 0L && !isActive) {
                    return@withContext
                } else {
                    delay(interval)
                }

                robot.keyPress(KeyEvent.VK_CONTROL)
                robot.keyPress(KeyEvent.VK_V)
                robot.keyRelease(KeyEvent.VK_V)
                robot.keyRelease(KeyEvent.VK_CONTROL)
                robot.keyPress(KeyEvent.VK_ENTER)
                robot.keyRelease(KeyEvent.VK_ENTER)

                finishTimes++
            }

            workingState = WorkingStates.OnSpammedCompleted
            delay(completeDelay)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun work(options: InputSpamOptions) {
        workingJob = GlobalScope.launch {
            beginWait()
            beginSpam(options)
            workingState = WorkingStates.Default
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun cancel() {
        GlobalScope.launch {
            workingState = WorkingStates.OnCancelling
            workingJob.cancel()
            delay(cancelDelay)
            workingState = WorkingStates.Default
        }
    }
}
