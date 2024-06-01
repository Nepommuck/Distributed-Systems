package edu.agh.distributedsystems.rabbitmq.common

abstract class AbstractRepl {
    protected abstract val welcomeMessage: String

    protected abstract fun handleUserInput(userInput: List<String>)

    fun run() {
        println("$welcomeMessage\n")

        readUserInput()
    }

    private fun readUserInput() {
        print("> ")
        val userInput = parseUserInput(readlnOrNull().orEmpty())

        if (userInput.isNotEmpty()) {
            handleUserInput(userInput)
        }
        readUserInput()
    }

    private fun parseUserInput(rawUserInput: String): List<String> =
        rawUserInput.split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
}
