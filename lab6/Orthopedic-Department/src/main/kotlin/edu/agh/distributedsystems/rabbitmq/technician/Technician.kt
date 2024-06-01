package edu.agh.distributedsystems.rabbitmq.technician

import com.rabbitmq.client.Connection
import com.rabbitmq.client.Envelope
import edu.agh.distributedsystems.rabbitmq.common.model.Injury
import edu.agh.distributedsystems.rabbitmq.common.model.MedicalPersonnel
import edu.agh.distributedsystems.rabbitmq.common.model.Patient
import edu.agh.distributedsystems.rabbitmq.common.model.message.*
import edu.agh.distributedsystems.rabbitmq.common.rabbitmq.Exchange
import edu.agh.distributedsystems.rabbitmq.common.rabbitmq.Queue
import kotlin.random.Random


class Technician(connection: Connection, knownInjuries: Pair<Injury, Injury>) : MedicalPersonnel() {
    private val channel = connection.createChannel()
    private val knownInjuriesSet = knownInjuries.toList().toSet()

    private val awaitExaminationRequestCallback = { examinationRequest: ExaminationRequest, envelope: Envelope ->
        val patient = examinationRequest.patient
        val injury = examinationRequest.injury

        println("Received ${injury.code} examination request for ${patient.surname}")

        if (!knownInjuriesSet.contains(injury)) {
            println("Cannot handle such examination\n")
            channel.basicNack(envelope.deliveryTag, false, true)
        } else {
            println("Starting examination")
            channel.basicAck(envelope.deliveryTag, false)

            val examinationResult = ExaminationResult(
                patient,
                injury,
                verdict = examinePatient(patient, injury),
            )

            println("Finished examination, sending results back to doctor\n")
            sendExaminationResult(examinationResult)

            // Notify administration
            Queue.ReportToAdministration.publish(
                channel,
                message = AdministrationReport.ExaminationConducted(
                    technicianId = personnelId,
                    examinationResult = examinationResult,
                ),
            )
        }
    }

    private val awaitNotificationFromAdministrationCallback = { notification: PersonnelNotification ->
        println("Received message from administration: '${notification.message}'")
        print("> ")
    }

    init {
        require(knownInjuriesSet.size == 2) {
            "The injuries must not be equal to each other, but were both '${knownInjuries.first}'"
        }
        channel.basicQos(1)

        Queue.RequestExamination.declare(channel)
        Queue.RequestExamination.consume(channel, awaitExaminationRequestCallback)

        Queue.SendExaminationResult.declare(channel)

        Exchange.InfoFromAdministration.declareFanout(channel)

        val notifyMedicalPersonnelQueue = Queue.NotifyMedicalPersonnel(personnelId)
        notifyMedicalPersonnelQueue.declare(channel)
        notifyMedicalPersonnelQueue.bindToExchange(channel, Exchange.InfoFromAdministration)

        notifyMedicalPersonnelQueue.declare(channel)
        notifyMedicalPersonnelQueue.consumeAndAck(channel, awaitNotificationFromAdministrationCallback)

        Queue.ReportToAdministration.declare(channel)

        println(
            "Technician #${personnelId.value} " +
                    "specializing in ${knownInjuries.first.code} & ${knownInjuries.second.code}\n"
        )
    }

    private fun examinePatient(patient: Patient, injury: Injury): ExaminationVerdict {
        Thread.sleep(Random.nextInt(500, 3000).toLong())
        val isPatientOk = (patient.hashCode() + injury.hashCode()) % 2 == 0

        return if (isPatientOk)
            ExaminationVerdict.OK
        else
            ExaminationVerdict.OPERATION_REQUIRED
    }

    private fun sendExaminationResult(examinationResult: ExaminationResult) {
        Queue.SendExaminationResult.publish(
            channel,
            message = examinationResult,
        )
    }
}
