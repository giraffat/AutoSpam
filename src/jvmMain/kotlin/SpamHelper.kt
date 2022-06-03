import kotlinx.coroutines.*
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

object SpamHelper {
    private val robot = Robot()

    fun copy(text: String) = Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)

    private fun spamOnce() {
        robot.keyPress(KeyEvent.VK_CONTROL)
        robot.keyPress(KeyEvent.VK_V)
        robot.keyRelease(KeyEvent.VK_V)
        robot.keyRelease(KeyEvent.VK_CONTROL)
        robot.keyPress(KeyEvent.VK_ENTER)
        robot.keyRelease(KeyEvent.VK_ENTER)
    }

    suspend fun spam(interval: Long, maxTimes: Int, completedTimesChanged: (Int) -> Unit) =
        withContext(Dispatchers.Default)
        {
            for (i in 1..maxTimes) {
                if (!isActive) {
                    throw CancellationException()
                } else {
                    delay(interval)
                }

                spamOnce()
                completedTimesChanged(i)
            }
        }

    suspend fun spam(interval: Long, completedTimesChanged: (Int) -> Unit) = withContext(Dispatchers.Default)
    {
        var completedTimes = 0

        while (true) {
            if (!isActive) {
                throw CancellationException()
            } else {
                delay(interval)
            }

            spamOnce()
            completedTimes++
            completedTimesChanged(completedTimes)
        }
    }
}