import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

class MainModel {
    private val robot = Robot()

    var completedTimes by mutableStateOf(0L)
        private set

    private fun spamOnce() {
        robot.keyPress(KeyEvent.VK_CONTROL)
        robot.keyPress(KeyEvent.VK_V)
        robot.keyRelease(KeyEvent.VK_V)
        robot.keyRelease(KeyEvent.VK_CONTROL)
        robot.keyPress(KeyEvent.VK_ENTER)
        robot.keyRelease(KeyEvent.VK_ENTER)
    }

    suspend fun spam(interval: Long, maxTimes: Long) = withContext(Dispatchers.Default)
    {
        completedTimes = 0

        for (i in 1..maxTimes) {
            if (interval == 0L && !isActive) {
                return@withContext
            } else {
                delay(interval)
            }

            spamOnce()
            completedTimes = i
        }
    }

    suspend fun spam(interval: Long) = withContext(Dispatchers.Default)
    {
        completedTimes = 0

        while (true) {
            if (interval == 0L && !isActive) {
                return@withContext
            } else {
                delay(interval)
            }

            spamOnce()
            completedTimes++
        }
    }

    fun convertIntervalString(interval: String) = (interval.toFloat() * 1000).toLong()

    fun convertMaxTimesString(maxTimes: String) = maxTimes.toLong()

    fun copy(text: String) = Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}