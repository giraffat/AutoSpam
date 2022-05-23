package dataClasses

data class UiStates(
    val state: States = States.Default,
    val animationState: AnimationStates = AnimationStates.Waiting,
    val isCanceledButton: Boolean = false,
    val isProgressFull: Boolean = false
)
