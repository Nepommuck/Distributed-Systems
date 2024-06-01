package edu.agh.distributedsystems.rabbitmq.common.rabbitmq

import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Envelope
import edu.agh.distributedsystems.rabbitmq.common.model.PersonnelId
import edu.agh.distributedsystems.rabbitmq.common.model.message.AdministrationReport
import edu.agh.distributedsystems.rabbitmq.common.model.message.ExaminationRequest
import edu.agh.distributedsystems.rabbitmq.common.model.message.ExaminationResult
import edu.agh.distributedsystems.rabbitmq.common.model.message.PersonnelNotification
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor

abstract class Queue<Message>(val name: String, private val serializer: KSerializer<Message>) {

    fun declare(channel: Channel) {
        channel.queueDeclare(name, false, false, false, null)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun publish(channel: Channel, message: Message) {
        channel.basicPublish(
            "",
            name,
            null,
            Cbor.encodeToByteArray(serializer, message),
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun consume(channel: Channel, callback: (Message, Envelope) -> Unit, autoAck: Boolean = false) {
        val rabbitMqCallback = DeliverCallback { _, delivery ->
            val message = Cbor.decodeFromByteArray(serializer, delivery.body)

            callback(message, delivery.envelope)
        }

        channel.basicConsume(name, autoAck, rabbitMqCallback) { _ -> }
    }

    fun consumeAndAck(channel: Channel, callback: (Message) -> Unit) {
        this.consume(
            channel,
            callback = { message: Message, _: Envelope -> callback(message) },
            autoAck = true,
        )
    }

    fun bindToExchange(channel: Channel, exchange: Exchange<Message>) {
        channel.queueBind(name, exchange.name, "")
    }

    data object RequestExamination :
        Queue<ExaminationRequest>("request-examination-queue", ExaminationRequest.serializer())

    data object SendExaminationResult :
        Queue<ExaminationResult>("examination-result-queue", ExaminationResult.serializer())

    data class NotifyMedicalPersonnel(
        val personnelId: PersonnelId,
    ) : Queue<PersonnelNotification>("notify-personnel-queue-${personnelId.value}", PersonnelNotification.serializer())

    data object ReportToAdministration :
        Queue<AdministrationReport>("report-to-administration-queue", AdministrationReport.serializer())
}
