import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.awt.MouseInfo
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

    suspend fun spam(interval: ULong, maxTimes: ULong) = withContext(Dispatchers.Default)
    {
        completedTimes = 0

        for (i in 1..maxTimes.toInt()) {
            if (interval == 0UL && !isActive) {
                return@withContext
            } else {
                delay(interval.toLong())
            }

            spamOnce()
            completedTimes = i.toLong()
        }
    }

    suspend fun spam(interval: ULong) = withContext(Dispatchers.Default)
    {
        completedTimes = 0

        while (true) {
            if (interval == 0UL && !isActive) {
                return@withContext
            } else {
                delay(interval.toLong())
            }

            spamOnce()
            completedTimes++
        }
    }

    suspend fun checkMousePosition(cancel: () -> Unit) {
        while (true) {
            val mousePosition = MouseInfo.getPointerInfo().location
            if (mousePosition.x == 0 && mousePosition.y == 0) {
                cancel()
                return
            }
            delay(500L)
        }
    }

    fun convertIntervalString(intervalString: String): ULong {
        val interval = (intervalString.toFloat() * 1000).toULong()
        if (interval == 0UL) {
            throw java.lang.NumberFormatException()
        }
        return interval
    }

    fun convertMaxTimesString(maxTimesString: String): ULong {
        val maxTimes = maxTimesString.toULong()
        if (maxTimes == 0UL) {
            throw java.lang.NumberFormatException()
        }
        return maxTimes
    }

    fun copy(text: String) = Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}