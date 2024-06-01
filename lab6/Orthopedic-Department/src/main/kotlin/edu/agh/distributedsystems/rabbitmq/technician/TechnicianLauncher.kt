package edu.agh.distributedsystems.rabbitmq.technician

import edu.agh.distributedsystems.rabbitmq.common.LauncherUtil
import edu.agh.distributedsystems.rabbitmq.common.model.Injury

fun parseArguments(args: List<String>): Pair<Injury, Injury>? {
    if (args.size != 2) {
        return null
    }

    val parsedInjuries = args.map {
        when (val parsedInjury = Injury.parse(it)) {
            null -> {
                return null
            }

            else -> parsedInjury
        }
    }

    return Pair(parsedInjuries.first(), parsedInjuries.last())
}

fun main(args: Array<String>) {
    when (val injuryTypes = parseArguments(args.toList())) {
        null -> {
            println(
                "Expected 2 different injuries among: (${Injury.Knee.code} | ${Injury.Hip.code} | " +
                        "${Injury.Elbow.code}) as program arguments, but received '${args.toList().joinToString()}'"
            )
        }

        else -> Technician(
            connection = LauncherUtil.establishConnection(),
            knownInjuries = injuryTypes,
        )
    }
}
