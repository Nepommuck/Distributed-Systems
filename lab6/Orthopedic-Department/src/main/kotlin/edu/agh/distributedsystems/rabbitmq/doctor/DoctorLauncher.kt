package edu.agh.distributedsystems.rabbitmq.doctor

import edu.agh.distributedsystems.rabbitmq.common.LauncherUtil

fun main() {
    DoctorRepl(
        Doctor(connection = LauncherUtil.establishConnection()),
    ).run()
}
