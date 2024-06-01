package edu.agh.distributedsystems.rabbitmq.common

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import edu.agh.distributedsystems.rabbitmq.Config

object LauncherUtil {
    fun establishConnection(): Connection =
        ConnectionFactory().apply {
            host = Config.HOST
        }.newConnection()
}
