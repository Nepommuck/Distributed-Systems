package edu.agh.distributedsystems.rabbitmq.common.rabbitmq

import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel
import edu.agh.distributedsystems.rabbitmq.common.model.message.PersonnelNotification
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor

abstract class Exchange<Message>(val name: String, private val serializer: KSerializer<Message>) {
    fun declareFanout(channel: Channel) {
        channel.exchangeDeclare(name, BuiltinExchangeType.FANOUT)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun publish(channel: Channel, message: Message) {
        channel.basicPublish(
            name,
            "",
            null,
            Cbor.encodeToByteArray(serializer, message),
        )
    }

    data object InfoFromAdministration : Exchange<PersonnelNotification>(
        name = "info-from-administration-exchange",
        serializer = PersonnelNotification.serializer(),
    )
}
