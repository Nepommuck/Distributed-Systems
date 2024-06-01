package edu.agh.distributedsystems.rabbitmq.administration

import com.rabbitmq.client.Connection
import edu.agh.distributedsystems.rabbitmq.common.model.message.AdministrationReport
import edu.agh.distributedsystems.rabbitmq.common.model.message.PersonnelNotification
import edu.agh.distributedsystems.rabbitmq.common.rabbitmq.Exchange
import edu.agh.distributedsystems.rabbitmq.common.rabbitmq.Queue

class Administration(connection: Connection) {
    private val channel = connection.createChannel()

    private val awaitReceiveReportCallback = { report: AdministrationReport ->
        val parsedReport = when (report) {
            is AdministrationReport.ExaminationRequested ->
                "Doctor #${report.doctorId.value} has just requested ${report.examinationRequest.injury.code} " +
                        "examination for patient ${report.examinationRequest.patient.surname}"

            is AdministrationReport.ExaminationConducted ->
                "Technician #${report.technicianId.value} has just conducted ${report.examinationResult.injury.code} " +
                        "examination for patient ${report.examinationResult.patient.surname}. " +
                        "Examination verdict: ${report.examinationResult.verdict.name}"

            is AdministrationReport.ExaminationResultReceived ->
                "Doctor #${report.receivingDoctorId.value} has just received examination result regarding " +
                        "${report.examinationResult.patient.surname}'s ${report.examinationResult.injury.code} " +
                        "injury. Examination verdict: ${report.examinationResult.verdict.name}"
        }

        println(parsedReport)
        print("> ")
    }

    init {
        channel.basicQos(1)
        Exchange.InfoFromAdministration.declareFanout(channel)

        Queue.ReportToAdministration.declare(channel)
        Queue.ReportToAdministration.consumeAndAck(channel, awaitReceiveReportCallback)
    }

    fun notifyMedicalPersonnel(message: String) {
        Exchange.InfoFromAdministration.publish(channel, PersonnelNotification(message))
    }
}
