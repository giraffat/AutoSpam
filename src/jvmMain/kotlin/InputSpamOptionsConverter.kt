object InputSpamOptionsConverter {
    class EmptyInputException : Exception()

    fun interval(inputInterval: String): Long {
        if (inputInterval == "") {
            throw EmptyInputException()
        }

        val interval: Long

        try {
            interval = (inputInterval.toFloat() * 1000).toLong()
        } catch (e: NumberFormatException) {
            throw NumberFormatException("非法输入")
        }

        when {
            interval < 0L -> {
                throw NumberFormatException("间隔不能小于0")
            }
            interval == 0L -> {
                throw NumberFormatException("间隔不能等于0")
            }
            else -> {
                return interval
            }
        }
    }

    fun maxTimes(inputMaxTimes: String): Int {
        if (inputMaxTimes == "") {
            throw EmptyInputException()
        }

        val maxTimes: Int
        try {
            maxTimes = inputMaxTimes.toInt()
        } catch (e: NumberFormatException) {
            throw NumberFormatException("非法输入")
        }

        when {
            maxTimes < 0 -> {
                throw NumberFormatException("最大次数不能小于0")
            }
            maxTimes == 0 -> {
                throw NumberFormatException("最大次数不能等于0")
            }
            else -> {
                return maxTimes
            }
        }
    }

    fun spamText(inputSpamText: String): String =
        if (inputSpamText == "") throw EmptyInputException() else inputSpamText
}