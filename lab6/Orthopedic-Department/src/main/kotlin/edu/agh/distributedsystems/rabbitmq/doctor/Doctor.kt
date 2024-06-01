package edu.agh.distributedsystems.rabbitmq.doctor

import com.rabbitmq.client.Connection
import edu.agh.distributedsystems.rabbitmq.common.model.Injury
import edu.agh.distributedsystems.rabbitmq.common.model.MedicalPersonnel
import edu.agh.distributedsystems.rabbitmq.common.model.Patient
import edu.agh.distributedsystems.rabbitmq.common.model.message.AdministrationReport
import edu.agh.distributedsystems.rabbitmq.common.model.message.ExaminationRequest
import edu.agh.distributedsystems.rabbitmq.common.model.message.ExaminationResult
import edu.agh.distributedsystems.rabbitmq.common.model.message.PersonnelNotification
import edu.agh.distributedsystems.rabbitmq.common.rabbitmq.Exchange
import edu.agh.distributedsystems.rabbitmq.common.rabbitmq.Queue


class Doctor(connection: Connection) : MedicalPersonnel() {
    private val channel = connection.createChannel()

    private val awaitExaminationResultsCallback = { examinationResult: ExaminationResult ->
        println(
            "Received verdict for ${examinationResult.patient.surname}'s ${examinationResult.injury.code} injury: " +
                    "${examinationResult.verdict}"
        )
        print("> ")

        // Notify administration
        Queue.ReportToAdministration.publish(
            channel,
            message = AdministrationReport.ExaminationResultReceived(
                receivingDoctorId = personnelId,
                examinationResult = examinationResult,
            ),
        )
    }

    private val awaitNotificationFromAdministrationCallback = { notification: PersonnelNotification ->
        println("Received message from administration: '${notification.message}'")
        print("> ")
    }

    init {
        channel.basicQos(1)
        Queue.RequestExamination.declare(channel)

        Queue.SendExaminationResult.declare(channel)
        Queue.SendExaminationResult.consumeAndAck(channel, awaitExaminationResultsCallback)

        Exchange.InfoFromAdministration.declareFanout(channel)
        val notifyMedicalPersonnelQueue = Queue.NotifyMedicalPersonnel(personnelId)

        notifyMedicalPersonnelQueue.declare(channel)
        notifyMedicalPersonnelQueue.bindToExchange(channel, Exchange.InfoFromAdministration)
        notifyMedicalPersonnelQueue.consumeAndAck(channel, awaitNotificationFromAdministrationCallback)

        Queue.ReportToAdministration.declare(channel)
    }

    fun requestExamination(patient: Patient, injury: Injury) {
        val examinationRequest = ExaminationRequest(patient, injury)

        // Request examination
        Queue.RequestExamination.publish(
            channel,
            message = examinationRequest,
        )

        // Notify administration
        Queue.ReportToAdministration.publish(
            channel,
            message = AdministrationReport.ExaminationRequested(
                doctorId = personnelId,
                examinationRequest = examinationRequest,
            ),
        )
    }
}
