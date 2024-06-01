package edu.agh.distributedsystems.rabbitmq.common.model.message

import edu.agh.distributedsystems.rabbitmq.common.model.PersonnelId
import kotlinx.serialization.Serializable


@Serializable
sealed class AdministrationReport {
    @Serializable
    data class ExaminationRequested(
        val doctorId: PersonnelId,
        val examinationRequest: ExaminationRequest,
    ) : AdministrationReport()

    @Serializable
    data class ExaminationConducted(
        val technicianId: PersonnelId,
        val examinationResult: ExaminationResult,
    ) : AdministrationReport()

    @Serializable
    data class ExaminationResultReceived(
        val receivingDoctorId: PersonnelId,
        val examinationResult: ExaminationResult,
    ) : AdministrationReport()
}
