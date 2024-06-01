package edu.agh.distributedsystems.rabbitmq.administration

import edu.agh.distributedsystems.rabbitmq.common.AbstractRepl
import kotlin.system.exitProcess

class AdministrationRepl(private val administration: Administration) : AbstractRepl() {
    override val welcomeMessage = "Administration REPL\n\n" +
            "Type any message to send to all medical personnel or 'exit' to exit"

    override fun handleUserInput(userInput: List<String>) =
        if (userInput.isNotEmpty() && userInput.first() == "exit") {
            exitProcess(0)
        } else {
            administration.notifyMedicalPersonnel(
                message = userInput.joinToString(" "),
            )
        }
}
