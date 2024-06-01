package edu.agh.distributedsystems.rabbitmq.doctor

import edu.agh.distributedsystems.rabbitmq.common.AbstractRepl
import edu.agh.distributedsystems.rabbitmq.common.model.Injury
import edu.agh.distributedsystems.rabbitmq.common.model.Patient
import kotlin.system.exitProcess

class DoctorRepl(private val doctor: Doctor) : AbstractRepl() {
    private val usageMessage =
        "Usage: (${Injury.Knee.code} | ${Injury.Hip.code} | ${Injury.Elbow.code}) patient-surname\n" +
                "Type 'exit' to exit"

    override val welcomeMessage = "Doctor #${doctor.personnelId.value} REPL\n\n$usageMessage"

    override fun handleUserInput(userInput: List<String>) =
        if (userInput.isNotEmpty() && userInput.first() == "exit") {
            exitProcess(0)
        } else if (userInput.size != 2) {
            println(usageMessage)
        } else
            when (val parsedInjury = Injury.parse(userInput.first())) {
                null -> {
                    println(usageMessage)
                }

                else -> {
                    val surname = userInput.last()
                    println("Requesting examination of ${parsedInjury.code} for $surname...")
                    doctor.requestExamination(Patient(surname), parsedInjury)
                }
            }
}
