package edu.agh.distributedsystems.rabbitmq.administration

import edu.agh.distributedsystems.rabbitmq.common.LauncherUtil

fun main() {
    AdministrationRepl(
        Administration(connection = LauncherUtil.establishConnection()),
    ).run()
}