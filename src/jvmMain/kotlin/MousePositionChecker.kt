import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.awt.MouseInfo

object MousePositionChecker {
    private fun mouseIsOnLeftTop(): Boolean {
        val mousePosition = MouseInfo.getPointerInfo().location
        return mousePosition.x == 0 && mousePosition.y == 0
    }

    suspend fun checkMousePosition(mouseIsOnLeftTop: () -> Unit) {
        while (true) {
            if (this.mouseIsOnLeftTop()) {
                mouseIsOnLeftTop()
                throw CancellationException()
            }
            delay(500L)
        }
    }
}