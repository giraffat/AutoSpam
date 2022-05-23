data class InputSpamOptionsWarnings(
    val intervalError: Boolean = false,
    val maxTimesError: Boolean = false,
    val spamTextError: Boolean = false,
    val unlimitedSpamWarning: Boolean = false,
    val emptySpamContentWarning: Boolean = false,
)
